package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.*;
import com.musiguessr.backend.dto.playlist.*;
import com.musiguessr.backend.model.Music;
import com.musiguessr.backend.model.Playlist;
import com.musiguessr.backend.model.PlaylistItem;
import com.musiguessr.backend.model.PlaylistItemId;
import com.musiguessr.backend.repository.MusicRepository;
import com.musiguessr.backend.repository.PlaylistItemRepository;
import com.musiguessr.backend.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final MusicRepository musicRepository;

    @Transactional(readOnly = true)
    public List<PlaylistResponseDTO> getPlaylists(Long ownerId, Boolean isCurated, String q, Integer limit, Integer offset) {
        // Keep this simple (similar to others). If you MUST support filters, keep these few lines.
        Stream<Playlist> stream = playlistRepository.findAll().stream();

        if (ownerId != null) stream = stream.filter(p -> Objects.equals(p.getOwnerId(), ownerId));
        if (isCurated != null) stream = stream.filter(p -> Objects.equals(p.getIsCurated(), isCurated));
        if (StringUtils.hasText(q)) {
            String needle = q.trim().toLowerCase();
            stream = stream.filter(p -> p.getName() != null && p.getName().toLowerCase().contains(needle));
        }

        // Optional: keep pagination, but it’s extra compared to others.
        int safeOffset = (offset == null || offset < 0) ? 0 : offset;
        int safeLimit = (limit == null || limit < 0) ? 50 : limit;

        return stream.skip(safeOffset).limit(safeLimit)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaylistResponseDTO getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        return mapToDTO(playlist);
    }

    @Transactional
    public PlaylistResponseDTO createPlaylist(PlaylistRequestDTO request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setOwnerId(request.getOwner_id());
        playlist.setIsCurated(request.getIs_curated() != null ? request.getIs_curated() : false);

        Playlist saved = playlistRepository.save(playlist);
        return mapToDTO("Playlist created", saved);
    }

    @Transactional
    public PlaylistResponseDTO updatePlaylist(Long id, PlaylistUpdateRequestDTO request) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        if (StringUtils.hasText(request.getName())) playlist.setName(request.getName());
        if (request.getIs_curated() != null) playlist.setIsCurated(request.getIs_curated());

        Playlist updated = playlistRepository.save(playlist);
        return mapToDTO("Playlist updated", updated);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        // keep it explicit and simple
        playlistItemRepository.deleteAll(playlistItemRepository.findByIdPlaylistIdOrderByPositionAsc(id));
        playlistRepository.delete(playlist);
    }

    @Transactional(readOnly = true)
    public List<MusicResponseDTO> getPlaylistSongs(Long playlistId) {
        ensurePlaylistExists(playlistId);

        return playlistItemRepository.findByIdPlaylistIdOrderByPositionAsc(playlistId).stream()
                .map(PlaylistItem::getSong)
                .filter(Objects::nonNull)
                .map(this::mapMusicToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, PlaylistAddSongRequestDTO request) {
        ensurePlaylistExists(playlistId);

        Music song = musicRepository.findById(request.getSong_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        if (playlistItemRepository.existsByIdPlaylistIdAndIdSongId(playlistId, song.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Song already in playlist");
        }

        Integer pos = request.getPosition();
        if (pos == null) {
            // max+1
            List<PlaylistItem> items = playlistItemRepository.findByIdPlaylistIdOrderByPositionAsc(playlistId);
            int max = items.stream()
                    .map(PlaylistItem::getPosition)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(0);
            pos = max + 1;
        }

        PlaylistItem item = new PlaylistItem();
        item.setId(new PlaylistItemId(playlistId, song.getId()));
        item.setSong(song);
        item.setPosition(pos);

        playlistItemRepository.save(item);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        ensurePlaylistExists(playlistId);

        PlaylistItem item = playlistItemRepository.findByIdPlaylistIdAndIdSongId(playlistId, songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not in playlist"));

        playlistItemRepository.delete(item);
    }

    @Transactional
    public void reorder(Long playlistId, PlaylistReorderRequestDTO request) {
        ensurePlaylistExists(playlistId);

        // simplest: apply positions exactly as given
        for (PlaylistReorderItemDTO it : request.getItems()) {
            PlaylistItem item = playlistItemRepository.findByIdPlaylistIdAndIdSongId(playlistId, it.getSong_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song not in playlist: " + it.getSong_id()));

            item.setPosition(it.getPosition());
            playlistItemRepository.save(item);
        }
    }

    private void ensurePlaylistExists(Long playlistId) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }
    }

    private PlaylistResponseDTO mapToDTO(Playlist playlist) {
        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getName(),
                playlist.getOwnerId(),
                playlist.getIsCurated(),
                playlist.getCreatedAt()
        );
    }

    private PlaylistResponseDTO mapToDTO(String message, Playlist playlist) {
        return new PlaylistResponseDTO(
                message,
                playlist.getId(),
                playlist.getName(),
                playlist.getOwnerId(),
                playlist.getIsCurated(),
                playlist.getCreatedAt()
        );
    }

    private MusicResponseDTO mapMusicToDTO(Music music) {
        GenreResponseDTO genreDTO = (music.getGenre() != null)
                ? new GenreResponseDTO(music.getGenre().getId(), music.getGenre().getName())
                : null;

        ArtistResponseDTO artistDTO = (music.getArtist() != null)
                ? new ArtistResponseDTO(music.getArtist().getId(), music.getArtist().getName())
                : null;

        return new MusicResponseDTO(
                music.getId(),
                music.getName(),
                music.getUrl(),
                genreDTO,
                artistDTO
        );
    }
}

package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.ArtistResponseDTO;
import com.musiguessr.backend.dto.GenreResponseDTO;
import com.musiguessr.backend.dto.MusicResponseDTO;
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

import java.util.*;
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
        Stream<Playlist> stream = playlistRepository.findAll().stream();

        if (ownerId != null) stream = stream.filter(p -> Objects.equals(p.getUserId(), ownerId));
        if (StringUtils.hasText(q)) {
            String needle = q.trim().toLowerCase();
            stream = stream.filter(p -> p.getName() != null && p.getName().toLowerCase().contains(needle));
        }

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
        playlist.setUserId(request.getOwner_id());

        try {
            Playlist saved = playlistRepository.save(playlist);
            return mapToDTO("Playlist created", saved);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Playlist name already exists for this user");
        }
    }

    @Transactional
    public PlaylistResponseDTO updatePlaylist(Long id, PlaylistUpdateRequestDTO request) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        if (StringUtils.hasText(request.getName())) {
            playlist.setName(request.getName());
        }

        try {
            Playlist updated = playlistRepository.save(playlist);
            return mapToDTO("Playlist updated", updated);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Playlist name already exists for this user");
        }
    }

    @Transactional
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        playlistRepository.delete(playlist);
    }

    @Transactional(readOnly = true)
    public List<MusicResponseDTO> getPlaylistSongs(Long playlistId) {
        ensurePlaylistExists(playlistId);

        return playlistItemRepository.findByIdPlaylistIdOrderByIdPositionAsc(playlistId).stream()
                .map(PlaylistItem::getMusic)
                .map(this::mapMusicToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, PlaylistAddSongRequestDTO request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        Music music = musicRepository.findById(request.getSong_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Music not found"));

        if (playlistItemRepository.existsByIdPlaylistIdAndMusicId(playlistId, music.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Song already in playlist");
        }

        int position;
        if (request.getPosition() != null) {
            if (request.getPosition() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position must be > 0");
            }
            boolean positionTaken = playlistItemRepository.existsById(new PlaylistItemId(playlistId, request.getPosition()));
            if (positionTaken) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Position already taken");
            }
            position = request.getPosition();
        } else {
            // max+1
            Integer max = playlistItemRepository.findByIdPlaylistIdOrderByIdPositionAsc(playlistId).stream()
                    .map(it -> it.getId().getPosition())
                    .max(Integer::compareTo)
                    .orElse(0);
            position = max + 1;
        }

        PlaylistItem item = new PlaylistItem();
        item.setId(new PlaylistItemId(playlistId, position));
        item.setPlaylist(playlist);
        item.setMusic(music);

        playlistItemRepository.save(item);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        ensurePlaylistExists(playlistId);

        PlaylistItem item = playlistItemRepository.findByIdPlaylistIdAndMusicId(playlistId, songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not in playlist"));

        playlistItemRepository.delete(item);
    }

    @Transactional
    public void reorder(Long playlistId, PlaylistReorderRequestDTO request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        List<PlaylistItem> current = playlistItemRepository.findByIdPlaylistIdOrderByIdPositionAsc(playlistId);
        Map<Long, Music> currentMusicById = current.stream()
                .map(PlaylistItem::getMusic)
                .collect(Collectors.toMap(Music::getId, m -> m));

        for (PlaylistReorderItemDTO it : request.getItems()) {
            if (it.getPosition() == null || it.getPosition() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position must be > 0");
            }
            if (!currentMusicById.containsKey(it.getSong_id())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song not in playlist: " + it.getSong_id());
            }
        }

        playlistItemRepository.deleteAll(current);

        List<PlaylistItem> recreated = new ArrayList<>();
        for (PlaylistReorderItemDTO it : request.getItems()) {
            PlaylistItem pi = new PlaylistItem();
            pi.setId(new PlaylistItemId(playlistId, it.getPosition()));
            pi.setPlaylist(playlist);
            pi.setMusic(currentMusicById.get(it.getSong_id()));
            recreated.add(pi);
        }

        playlistItemRepository.saveAll(recreated);
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
                playlist.getUserId(),
                playlist.getCreatedAt()
        );
    }

    private PlaylistResponseDTO mapToDTO(String message, Playlist playlist) {
        return new PlaylistResponseDTO(
                message,
                playlist.getId(),
                playlist.getName(),
                playlist.getUserId(),
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

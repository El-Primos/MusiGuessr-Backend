package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.artist.ArtistResponseDTO;
import com.musiguessr.backend.dto.genre.GenreResponseDTO;
import com.musiguessr.backend.dto.music.MusicResponseDTO;
import com.musiguessr.backend.dto.playlist.*;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.MusicRepository;
import com.musiguessr.backend.repository.PlaylistItemRepository;
import com.musiguessr.backend.repository.PlaylistRepository;
import com.musiguessr.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final MusicRepository musicRepository;

    @Transactional(readOnly = true)
    public List<PlaylistResponseDTO> getPlaylists(Long ownerId, String name, Integer limit, Integer offset) {
        Stream<Playlist> stream = playlistRepository.findAll().stream();

        if (ownerId != null) stream = stream.filter(p -> Objects.equals(p.getOwnerId(), ownerId));
        if (StringUtils.hasText(name)) {
            String needle = name.trim().toLowerCase();
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
        playlist.setOwnerId(request.getOwnerId());

        try {
            Playlist saved = playlistRepository.save(playlist);
            return mapToDTO("Playlist created", saved);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Playlist name already exists for this user");
        }
    }

    @Transactional
    public PlaylistResponseDTO createRandomPlaylist(PlaylistRandomRequestDTO request) {
        var criteria = request.getCriteria();

        List<Long> genres = (criteria != null && criteria.getGenres() != null)
                ? criteria.getGenres()
                : Collections.emptyList();

        List<Long> artists = (criteria != null && criteria.getArtists() != null)
                ? criteria.getArtists()
                : Collections.emptyList();

        boolean filterGenres = !genres.isEmpty();
        boolean filterArtists = !artists.isEmpty();

        List<MusicRepository.ProfileProjection> musics = musicRepository.findRandomMusics(
                request.getLength(),
                genres,
                filterGenres,
                artists,
                filterArtists
        );
        if (musics.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No music available");
        }

        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setOwnerId(getOwnerId());

        Playlist savedPlaylist = playlistRepository.save(playlist);
        Long playlistId = savedPlaylist.getId();


        List<PlaylistItemRequestDTO> items = IntStream.range(0, musics.size())
                .mapToObj(i -> {
                    MusicRepository.ProfileProjection music = musics.get(i);

                    PlaylistItemRequestDTO dto = new PlaylistItemRequestDTO();
                    dto.setSongId(music.getId());
                    dto.setPosition(i + 1);
                    return dto;
                })
                .toList();

        PlaylistBatchItemRequestDTO batchRequest = new PlaylistBatchItemRequestDTO();
        batchRequest.setItems(items);

        addSongsToPlaylist(playlistId, batchRequest);

        return mapToDTO(savedPlaylist);
    }

    @Transactional
    public Long createRandomPlaylist(String name, Integer playlistLength) {
        List<MusicRepository.ProfileProjection> musics = musicRepository.findRandomMusics(
                playlistLength,
                null,
                false,
                null,
                false
        );
        if (musics.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No music available");
        }

        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setOwnerId(getOwnerId());

        Playlist savedPlaylist = playlistRepository.save(playlist);
        Long playlistId = savedPlaylist.getId();

        List<PlaylistItemRequestDTO> items = IntStream.range(0, musics.size())
                .mapToObj(i -> {
                    MusicRepository.ProfileProjection music = musics.get(i);

                    PlaylistItemRequestDTO dto = new PlaylistItemRequestDTO();
                    dto.setSongId(music.getId());
                    dto.setPosition(i + 1);
                    return dto;
                })
                .toList();

        PlaylistBatchItemRequestDTO batchRequest = new PlaylistBatchItemRequestDTO();
        batchRequest.setItems(items);

        addSongsToPlaylist(playlistId, batchRequest);

        return playlistId;
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
    public void addSongToPlaylist(Long playlistId, PlaylistItemRequestDTO request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        Music music = musicRepository.findById(request.getSongId())
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
            Integer max = playlistItemRepository.findMaxPositionByPlaylistId(playlistId);
            position = max + 1;
        }

        PlaylistItem item = new PlaylistItem();
        item.setId(new PlaylistItemId(playlistId, position));
        item.setPlaylist(playlist);
        item.setMusicId(music.getId());

        playlistItemRepository.save(item);
    }

    @Transactional
    public void addSongsToPlaylist(Long playlistId, PlaylistBatchItemRequestDTO request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        List<Long> requestedMusicIds =
                request.getItems().stream().map(PlaylistItemRequestDTO::getSongId).distinct().toList();
        if (requestedMusicIds.size() < request.getItems().size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request contains duplicate items");
        }

        List<Music> foundMusics = musicRepository.findAllById(requestedMusicIds);

        if (foundMusics.size() != requestedMusicIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some songs not found");
        }

        Set<Long> existingMusicIdsInPlaylist = playlistItemRepository.findMusicIdsByPlaylistId(playlistId);
        Set<Integer> positions = playlistItemRepository.findPositionsByPlaylistId(playlistId);

        Integer dbMaxPosition = playlistItemRepository.findMaxPositionByPlaylistId(playlistId);
        int currentMaxPosition = (dbMaxPosition != null) ? dbMaxPosition : 0;

        List<PlaylistItem> itemsToSave = new ArrayList<>();

        Set<Integer> processedPositionsInBatch = new HashSet<>();

        for (PlaylistItemRequestDTO req : request.getItems()) {
            if (existingMusicIdsInPlaylist.contains(req.getSongId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Song " + req.getSongId() + " already in " +
                        "playlist");
            }

            int finalPosition;
            if (req.getPosition() != null) {
                if (req.getPosition() < 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position must be > 0");
                }
                if (positions.contains(req.getPosition()) || processedPositionsInBatch.contains(req.getPosition())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Position " + req.getPosition() + " is already taken");
                }
                finalPosition = req.getPosition();
            } else {
                do {
                    currentMaxPosition++;
                } while (positions.contains(currentMaxPosition) || processedPositionsInBatch.contains(currentMaxPosition));
                finalPosition = currentMaxPosition;
            }

            processedPositionsInBatch.add(finalPosition);

            PlaylistItem item = new PlaylistItem();
            item.setId(new PlaylistItemId(playlistId, finalPosition));
            item.setPlaylist(playlist);
            item.setMusicId(req.getSongId());

            itemsToSave.add(item);
        }

        playlistItemRepository.saveAll(itemsToSave);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        ensurePlaylistExists(playlistId);

        PlaylistItem item = playlistItemRepository.findByIdPlaylistIdAndMusicId(playlistId, songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not in playlist"));

        playlistItemRepository.delete(item);
    }

    @Transactional
    public void reorder(Long playlistId, PlaylistBatchItemRequestDTO request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        List<PlaylistItem> current = playlistItemRepository.findByIdPlaylistIdOrderByIdPositionAsc(playlistId);
        Map<Long, Music> currentMusicById = current.stream()
                .map(PlaylistItem::getMusic)
                .collect(Collectors.toMap(Music::getId, m -> m));

        for (PlaylistItemRequestDTO it : request.getItems()) {
            if (it.getPosition() == null || it.getPosition() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position must be > 0");
            }
            if (!currentMusicById.containsKey(it.getSongId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song not in playlist");
            }
        }

        playlistItemRepository.deleteAll(current);

        List<PlaylistItem> recreated = new ArrayList<>();
        for (PlaylistItemRequestDTO it : request.getItems()) {
            PlaylistItem pi = new PlaylistItem();
            pi.setId(new PlaylistItemId(playlistId, it.getPosition()));
            pi.setPlaylist(playlist);
            pi.setMusic(currentMusicById.get(it.getSongId()));
            recreated.add(pi);
        }

        playlistItemRepository.saveAll(recreated);
    }

    private void ensurePlaylistExists(Long playlistId) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
        }
    }

    private static Long getOwnerId() {
        Long ownerId = 0L;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.user();

            if (Objects.equals(user.getRole(), UserRole.BANNED)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Your account is disabled. Please contact support.");
            }

            if (user.getRole() == UserRole.ADMIN) {
                ownerId = user.getId();
            }
        }

        return ownerId;
    }

    private PlaylistResponseDTO mapToDTO(Playlist playlist) {
        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getName(),
                playlist.getOwnerId()
        );
    }

    private PlaylistResponseDTO mapToDTO(String message, Playlist playlist) {
        return new PlaylistResponseDTO(
                message,
                playlist.getId(),
                playlist.getName(),
                playlist.getOwnerId()
        );
    }

    private MusicResponseDTO mapMusicToDTO(Music music) {
        GenreResponseDTO genreDTO = (music.getGenre() != null)
                ? new GenreResponseDTO(music.getGenreId(), music.getGenre().getName())
                : null;

        ArtistResponseDTO artistDTO = (music.getArtist() != null)
                ? new ArtistResponseDTO(music.getArtistId(), music.getArtist().getName())
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

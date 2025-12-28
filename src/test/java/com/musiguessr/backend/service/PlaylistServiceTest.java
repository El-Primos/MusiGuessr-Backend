package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.playlist.*;
import com.musiguessr.backend.model.*;
import com.musiguessr.backend.repository.MusicRepository;
import com.musiguessr.backend.repository.PlaylistItemRepository;
import com.musiguessr.backend.repository.PlaylistRepository;
import com.musiguessr.backend.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private PlaylistItemRepository playlistItemRepository;
    @Mock
    private MusicRepository musicRepository;

    @InjectMocks
    private PlaylistService playlistService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.ADMIN);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPlaylists_ShouldFilterAndPaginate() {
        Playlist p1 = new Playlist();
        p1.setName("Rock Hits");
        p1.setOwnerId(1L);
        Playlist p2 = new Playlist();
        p2.setName("Pop Hits");
        p2.setOwnerId(2L);

        when(playlistRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PlaylistResponseDTO> result = playlistService.getPlaylists(null, "rock", 10, 0);

        assertEquals(1, result.size());
        assertEquals("Rock Hits", result.getFirst().getName());
    }

    @Test
    void createPlaylist_ShouldReturnDTO() {
        PlaylistRequestDTO request = new PlaylistRequestDTO();
        request.setName("New Playlist");
        request.setOwnerId(1L);

        Playlist saved = new Playlist();
        saved.setId(10L);
        saved.setName("New Playlist");
        saved.setOwnerId(1L);

        when(playlistRepository.save(any(Playlist.class))).thenReturn(saved);

        PlaylistResponseDTO response = playlistService.createPlaylist(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    void createRandomPlaylist_ShouldCreateAndAddSongs() {
        PlaylistRandomRequestDTO request = new PlaylistRandomRequestDTO();
        request.setName("Random Pl");
        request.setLength(2);

        MusicRepository.ProfileProjection m1 = mock(MusicRepository.ProfileProjection.class);
        when(m1.getId()).thenReturn(101L);
        MusicRepository.ProfileProjection m2 = mock(MusicRepository.ProfileProjection.class);
        when(m2.getId()).thenReturn(102L);

        when(musicRepository.findRandomMusics(anyInt(), any(), anyBoolean(), any(), anyBoolean()))
                .thenReturn(List.of(m1, m2));

        Playlist savedPlaylist = new Playlist();
        savedPlaylist.setId(5L);
        savedPlaylist.setName("Random Pl");

        when(playlistRepository.save(any(Playlist.class))).thenReturn(savedPlaylist);

        when(playlistRepository.findById(5L)).thenReturn(Optional.of(savedPlaylist));
        when(musicRepository.findAllById(anyList())).thenReturn(List.of(new Music(), new Music())); // Size check

        PlaylistResponseDTO response = playlistService.createRandomPlaylist(request);

        assertEquals(5L, response.getId());
        verify(playlistItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void addSongToPlaylist_ShouldSaveItem() {
        Long playlistId = 1L;
        PlaylistItemRequestDTO request = new PlaylistItemRequestDTO();
        request.setSongId(100L);

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);
        Music music = new Music();
        music.setId(100L);

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(musicRepository.findById(100L)).thenReturn(Optional.of(music));
        when(playlistItemRepository.existsByIdPlaylistIdAndMusicId(playlistId, 100L)).thenReturn(false);
        when(playlistItemRepository.findMaxPositionByPlaylistId(playlistId)).thenReturn(5);

        playlistService.addSongToPlaylist(playlistId, request);

        verify(playlistItemRepository).save(argThat(item ->
                item.getId().getPosition() == 6 && item.getMusicId().equals(100L)
        ));
    }

    @Test
    void addSongToPlaylist_ShouldThrow_WhenDuplicate() {
        Long playlistId = 1L;
        PlaylistItemRequestDTO request = new PlaylistItemRequestDTO();
        request.setSongId(100L);

        Music music = new Music();
        music.setId(100L);

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(new Playlist()));
        when(musicRepository.findById(100L)).thenReturn(Optional.of(music));
        when(playlistItemRepository.existsByIdPlaylistIdAndMusicId(playlistId, 100L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                playlistService.addSongToPlaylist(playlistId, request)
        );
    }

    @Test
    void addSongsToPlaylist_Batch_ShouldSaveAll() {
        Long playlistId = 1L;
        PlaylistBatchItemRequestDTO batch = new PlaylistBatchItemRequestDTO();
        PlaylistItemRequestDTO item1 = new PlaylistItemRequestDTO();
        item1.setSongId(10L);
        PlaylistItemRequestDTO item2 = new PlaylistItemRequestDTO();
        item2.setSongId(20L);
        batch.setItems(List.of(item1, item2));

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(new Playlist()));
        when(musicRepository.findAllById(anyList())).thenReturn(List.of(new Music(), new Music())); // Found checks
        when(playlistItemRepository.findMusicIdsByPlaylistId(playlistId)).thenReturn(new HashSet<>());
        when(playlistItemRepository.findPositionsByPlaylistId(playlistId)).thenReturn(new HashSet<>());
        when(playlistItemRepository.findMaxPositionByPlaylistId(playlistId)).thenReturn(0);

        playlistService.addSongsToPlaylist(playlistId, batch);

        verify(playlistItemRepository).saveAll(argThat(list -> {
            List<PlaylistItem> items = (List<PlaylistItem>) list;
            return items.size() == 2 && items.getFirst().getId().getPosition() == 1;
        }));
    }

    @Test
    void removeSongFromPlaylist_ShouldDelete() {
        Long playlistId = 1L;
        Long songId = 10L;
        PlaylistItem item = new PlaylistItem();

        when(playlistRepository.existsById(playlistId)).thenReturn(true);
        when(playlistItemRepository.findByIdPlaylistIdAndMusicId(playlistId, songId))
                .thenReturn(Optional.of(item));

        playlistService.removeSongFromPlaylist(playlistId, songId);

        verify(playlistItemRepository).delete(item);
    }

    @Test
    void reorder_ShouldDeleteOldAndSaveNew() {
        Long playlistId = 1L;
        PlaylistBatchItemRequestDTO request = new PlaylistBatchItemRequestDTO();
        PlaylistItemRequestDTO item1 = new PlaylistItemRequestDTO();
        item1.setSongId(10L);
        item1.setPosition(2);
        PlaylistItemRequestDTO item2 = new PlaylistItemRequestDTO();
        item2.setSongId(20L);
        item2.setPosition(1);
        request.setItems(List.of(item1, item2));

        Music m1 = new Music();
        m1.setId(10L);
        Music m2 = new Music();
        m2.setId(20L);

        PlaylistItem pi1 = new PlaylistItem();
        pi1.setMusic(m1);
        PlaylistItem pi2 = new PlaylistItem();
        pi2.setMusic(m2);

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(new Playlist()));
        when(playlistItemRepository.findByIdPlaylistIdOrderByIdPositionAsc(playlistId))
                .thenReturn(List.of(pi1, pi2));

        playlistService.reorder(playlistId, request);

        verify(playlistItemRepository).deleteAll(anyList());
        verify(playlistItemRepository).saveAll(argThat(list -> {
            List<PlaylistItem> items = (List<PlaylistItem>) list;

            return items.stream().anyMatch(i -> i.getMusic().getId().equals(20L) && i.getId().getPosition() == 1);
        }));
    }
}
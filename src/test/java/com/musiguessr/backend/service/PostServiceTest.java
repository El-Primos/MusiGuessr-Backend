package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.model.Game;
import com.musiguessr.backend.model.GameHistory;
import com.musiguessr.backend.model.GameRound;
import com.musiguessr.backend.model.Post;
import com.musiguessr.backend.repository.GameHistoryRepository;
import com.musiguessr.backend.repository.GameRoundRepository;
import com.musiguessr.backend.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private GameHistoryRepository gameHistoryRepository;
    @Mock
    private GameRoundRepository gameRoundRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void shareGameHistory_ShouldCreatePost_WhenValid() {
        Long userId = 1L;
        Long historyId = 10L;
        PostShareRequestDTO request = new PostShareRequestDTO();
        request.setGameHistoryId(historyId);

        GameHistory history = new GameHistory();
        history.setId(historyId);
        history.setUserId(userId);
        history.setScore(500);

        Post savedPost = new Post();
        savedPost.setId(100L);
        savedPost.setUserId(userId);
        savedPost.setGameHistoryId(historyId);
        savedPost.setPostedAt(OffsetDateTime.now());

        GameRound round1 = new GameRound();
        round1.setSong("Song A");
        round1.setGuessedSong("Song A");
        round1.setRound(1);

        GameRound round2 = new GameRound();
        round2.setSong("Song B");
        round2.setGuessedSong("Song C");
        round2.setRound(2);

        when(gameHistoryRepository.findById(historyId)).thenReturn(Optional.of(history));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(gameRoundRepository.findByGameHistoryIdOrderByRoundAsc(historyId))
                .thenReturn(List.of(round1, round2));

        PostShareResponseDTO response = postService.shareGameHistory(userId, request);

        assertNotNull(response);
        assertEquals(100L, response.getPostId());
        assertEquals(2, response.getPredictions().size());
        assertTrue(response.getPredictions().get(0));
        assertFalse(response.getPredictions().get(1));

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void shareGameHistory_ShouldThrow_WhenNotOwner() {
        Long userId = 1L;
        Long otherUserId = 2L;
        PostShareRequestDTO request = new PostShareRequestDTO();
        request.setGameHistoryId(10L);

        GameHistory history = new GameHistory();
        history.setUserId(otherUserId);

        when(gameHistoryRepository.findById(10L)).thenReturn(Optional.of(history));

        assertThrows(ResponseStatusException.class, () ->
                postService.shareGameHistory(userId, request)
        );
    }

    @Test
    void getPost_ShouldReturnDetails() {
        Long postId = 100L;
        Long historyId = 10L;

        Post post = new Post();
        post.setId(postId);
        post.setGameHistoryId(historyId);

        GameHistory history = new GameHistory();
        history.setId(historyId);

        Game game = new Game();
        game.setCreatedAt(OffsetDateTime.now().minusDays(1));
        history.setGame(game);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(gameHistoryRepository.findById(historyId)).thenReturn(Optional.of(history));
        when(gameRoundRepository.findByGameHistoryIdOrderByRoundAsc(historyId)).thenReturn(List.of());

        PostShareResponseDTO response = postService.getPost(postId);

        assertNotNull(response);
        assertEquals(postId, response.getPostId());
        assertEquals(game.getCreatedAt(), response.getPlayedAt());
    }

    @Test
    void getUserPosts_ShouldReturnList() {
        Long userId = 1L;
        Post post = new Post();
        post.setGameHistoryId(10L);

        when(postRepository.findByUserIdOrderByPostedAtDesc(userId)).thenReturn(List.of(post));
        when(gameHistoryRepository.findById(10L)).thenReturn(Optional.of(new GameHistory()));
        when(gameRoundRepository.findByGameHistoryIdOrderByRoundAsc(any())).thenReturn(List.of());

        List<PostShareResponseDTO> results = postService.getUserPosts(userId);

        assertEquals(1, results.size());
    }

    @Test
    void deletePost_ShouldDelete_WhenOwner() {
        Long userId = 1L;
        Long postId = 100L;

        Post post = new Post();
        post.setId(postId);
        post.setUserId(userId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(userId, postId);

        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_ShouldThrow_WhenForbidden() {
        Long userId = 1L;
        Long postId = 100L;

        Post post = new Post();
        post.setId(postId);
        post.setUserId(2L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ResponseStatusException.class, () ->
                postService.deletePost(userId, postId)
        );
    }
}
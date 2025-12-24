package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.dto.post.RoundSummaryDTO;
import com.musiguessr.backend.model.Game;
import com.musiguessr.backend.model.GameHistory;
import com.musiguessr.backend.model.GameRound;
import com.musiguessr.backend.model.Post;
import com.musiguessr.backend.repository.GameHistoryRepository;
import com.musiguessr.backend.repository.GameRepository;
import com.musiguessr.backend.repository.GameRoundRepository;
import com.musiguessr.backend.repository.PostRepository;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final GameRoundRepository gameRoundRepository;
    private final GameRepository gameRepository;

    @Transactional
    public PostShareResponseDTO shareGameHistory(Long authUserId, PostShareRequestDTO request) {
        if (request == null || request.getGameHistoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameHistoryId is required");
        }

        GameHistory history = gameHistoryRepository.findById(request.getGameHistoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game history not found"));

        if (!Objects.equals(history.getUserId(), authUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot share another user's game history");
        }

        Post post = new Post();
        post.setUserId(authUserId);
        post.setGameHistoryId(history.getId());

        Post saved = postRepository.save(post);
        return mapToResponse(saved, history);
    }

    @Transactional(readOnly = true)
    public PostShareResponseDTO getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        GameHistory history = gameHistoryRepository.findById(post.getGameHistoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game history not found"));

        return mapToResponse(post, history);
    }

    @Transactional(readOnly = true)
    public List<PostShareResponseDTO> getUserPosts(Long userId) {
        return postRepository.findByUserIdOrderByPostedAtDesc(userId).stream()
                .map(p -> {
                    GameHistory history = gameHistoryRepository.findById(p.getGameHistoryId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game history not found"));
                    return mapToResponse(p, history);
                })
                .toList();
    }

    // ------------- helpers -------------

    private PostShareResponseDTO mapToResponse(Post post, GameHistory history) {
        List<GameRound> rounds = gameRoundRepository.findByGameHistoryIdOrderByRoundAsc(history.getId());

        Map<String, RoundSummaryDTO> roundMap = new LinkedHashMap<>();
        List<Boolean> predictions = new java.util.ArrayList<>();

        int index = 1;
        for (GameRound round : rounds) {
            boolean correct = round.getGuessedSong() != null
                    && round.getGuessedSong().equalsIgnoreCase(round.getSong());
            predictions.add(correct);

            roundMap.put(
                    "song" + index,
                    new RoundSummaryDTO(
                            correct,
                            round.getSong(),
                            round.getGuessedSong(),
                            round.getGuessTime(),
                            round.getScoreEarned(),
                            round.getRound()
                    )
            );
            index++;
        }

        OffsetDateTime playedAt = resolvePlayedAt(history, post);

        return new PostShareResponseDTO(
                post.getId(),
                post.getUserId(),
                history.getId(),
                history.getScore(),
                playedAt,
                predictions,
                roundMap
        );
    }

    private OffsetDateTime resolvePlayedAt(GameHistory history, Post post) {
        Game game = history.getGame();
        if (game == null && history.getGameId() != null) {
            game = gameRepository.findById(history.getGameId()).orElse(null);
        }
        if (game != null && game.getCreatedAt() != null) {
            return game.getCreatedAt();
        }
        return post.getPostedAt();
    }
}

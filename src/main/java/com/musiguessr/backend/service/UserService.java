package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.dto.MeProfileDTO;
import com.musiguessr.backend.dto.GameHistoryDTO;
import com.musiguessr.backend.dto.TournamentHistoryDTO;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeProfileDTO getProfile(Long userId) {
        UserRepository.ProfileProjection projection = userRepository.findProfileByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new MeProfileDTO(
                projection.getId(),
                projection.getName(),
                projection.getUsername(),
                projection.getEmail(),
                projection.getTotalScore(),
                projection.getGamesPlayed(),
                projection.getLastPlayedAt());
    }

    @Transactional(readOnly = true)
    public List<GameHistoryDTO> getGameHistory(Long userId) {
        return userRepository.findGameHistoryByUserId(userId).stream()
                .map(p -> new GameHistoryDTO(
                        p.getGameId(),
                        p.getPlaylistId(),
                        p.getTotalScore(),
                        p.getPlayedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TournamentHistoryDTO> getTournamentHistory(Long userId) {
        return userRepository.findTournamentHistoryByUserId(userId).stream()
                .map(p -> new TournamentHistoryDTO(
                        p.getTournamentId(),
                        p.getUserScore(),
                        p.getStatus(),
                        p.getStartsAt(),
                        p.getEndsAt()))
                .toList();
    }

    private UserResponseDTO toDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getScore()
        );
    }
}

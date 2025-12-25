package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.UserResponseDTO;
import com.musiguessr.backend.dto.MeProfileDTO;
import com.musiguessr.backend.dto.GameHistoryDTO;
import com.musiguessr.backend.dto.TournamentHistoryDTO;
import com.musiguessr.backend.dto.user.ProfilePicturePresignResponseDTO;
import com.musiguessr.backend.dto.user.UserUpdateRequestDTO;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.model.UserRole;
import com.musiguessr.backend.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Map<String, String> VALID_IMAGE_FORMATS = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;
    public UserRepository getUserRepository() { return userRepository; }

    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long userId, UserUpdateRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setRole(role);
        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public ProfilePicturePresignResponseDTO presignProfilePicture(Long userId, String fileName, String contentType) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have an extension");
        }

        String normalizedExt = extension.toLowerCase();
        if (!VALID_IMAGE_FORMATS.containsKey(normalizedExt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extension '." + normalizedExt + "' is not supported. Use jpg, png, gif, or webp");
        }

        String expectedType = VALID_IMAGE_FORMATS.get(normalizedExt);
        if (!expectedType.equals(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
                    "Expected '%s' for .%s extension, but got '%s'",
                    expectedType, normalizedExt, contentType
            ));
        }

        String uniqueKey = "profile-pictures/" + userId + "/" + UUID.randomUUID() + "." + normalizedExt;
        String uploadUrl = s3Service.createPresignedUploadUrl(uniqueKey, contentType);

        return new ProfilePicturePresignResponseDTO("Presign upload url created", uniqueKey, uploadUrl);
    }

    @Transactional
    public UserResponseDTO confirmProfilePicture(Long userId, String key) {
        if (!s3Service.doesFileExist(key)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in S3");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Delete old profile picture if exists
        if (user.getProfilePictureUrl() != null && user.getProfilePictureUrl().contains("profile-pictures/")) {
            String oldKey = user.getProfilePictureUrl().substring(user.getProfilePictureUrl().indexOf("profile-pictures/"));
            s3Service.deleteFile(oldKey);
        }

        String url = s3Service.getUrl(key);
        user.setProfilePictureUrl(url);
        User savedUser = userRepository.save(user);

        return toDto(savedUser);
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
                projection.getLastPlayedAt() != null
                        ? projection.getLastPlayedAt().atOffset(java.time.ZoneOffset.UTC)
                        : null,
                projection.getTournamentsAttended(),
                projection.getProfilePictureUrl()
        );
    }

    @Transactional(readOnly = true)
    public List<GameHistoryDTO> getGameHistory(Long userId) {
        return userRepository.findGameHistoryByUserId(userId).stream()
                .map(p -> new GameHistoryDTO(
                        p.getGameHistoryId(),
                        p.getPlaylistId(),
                        p.getTotalScore(),
                        p.getPlayedAt() != null
                                ? p.getPlayedAt().atOffset(java.time.ZoneOffset.UTC)
                                : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TournamentHistoryDTO> getTournamentHistory(Long userId) {
        return userRepository.findTournamentHistoryByUserId(userId).stream()
                .map(p -> new TournamentHistoryDTO(
                        p.getTournamentId(),
                        p.getUserScore(),
                        p.getStatus(),
                        p.getStartsAt() != null
                                ? p.getStartsAt().atOffset(java.time.ZoneOffset.UTC)
                                : null,
                        p.getEndsAt() != null
                                ? p.getEndsAt().atOffset(java.time.ZoneOffset.UTC)
                                : null))
                .toList();
    }

    private UserResponseDTO toDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getScore(),
                user.getRole().name(),
                user.getProfilePictureUrl()
        );
    }
}

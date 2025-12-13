package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.UserResponseDTO;
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

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private UserResponseDTO toDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUserName(),
                user.getEmail(),
                user.getScore(),
                user.getCreatedAt());
    }
}

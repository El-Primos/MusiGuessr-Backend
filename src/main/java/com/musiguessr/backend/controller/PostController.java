package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.UserRepository;
import com.musiguessr.backend.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PostShareResponseDTO> shareGameHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PostShareRequestDTO request
    ) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.shareGameHistory(userId, request));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostShareResponseDTO> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostShareResponseDTO>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PostShareResponseDTO>> getMyPosts(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId
    ) {
        Long userId = getUserIdFromUserDetails(userDetails);
        postService.deletePost(userId, postId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }
}

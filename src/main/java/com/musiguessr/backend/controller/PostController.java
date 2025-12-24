package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.service.PostService;
import com.musiguessr.backend.util.AuthUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostShareResponseDTO> shareGameHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PostShareRequestDTO request
    ) {
        Long userId = AuthUtil.requireUserId(userDetails, postService.getUserRepository());
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
        Long userId = AuthUtil.requireUserId(userDetails, postService.getUserRepository());
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId
    ) {
        Long userId = AuthUtil.requireUserId(userDetails, postService.getUserRepository());
        postService.deletePost(userId, postId);
        return ResponseEntity.ok("Post deleted successfully");
    }
}

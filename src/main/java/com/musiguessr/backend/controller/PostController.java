package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostShareResponseDTO> shareGameHistory(
            @RequestParam Long userId,
            @Valid @RequestBody PostShareRequestDTO request
    ) {
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
}

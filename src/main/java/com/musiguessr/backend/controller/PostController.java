package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.post.PostShareRequestDTO;
import com.musiguessr.backend.dto.post.PostShareResponseDTO;
import com.musiguessr.backend.service.PostService;
import com.musiguessr.backend.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AuthUtil authUtil;

    @PostMapping
    public ResponseEntity<PostShareResponseDTO> shareGameHistory(@Valid @RequestBody PostShareRequestDTO request) {
        Long userId = authUtil.getCurrentUserId();
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
    public ResponseEntity<List<PostShareResponseDTO>> getMyPosts() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        Long userId = authUtil.getCurrentUserId();
        postService.deletePost(userId, postId);
        return ResponseEntity.ok("Post deleted successfully");
    }
}

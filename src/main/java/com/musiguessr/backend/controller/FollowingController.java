package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.following.FollowFriendDTO;
import com.musiguessr.backend.dto.following.FollowRequestDTO;
import com.musiguessr.backend.service.FollowingService;
import com.musiguessr.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/followings")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;
    private final AuthUtil authUtil;

    @PostMapping("/request")
    public ResponseEntity<String> sendFollowRequest(@RequestParam Long targetUserId) {
        Long userId = authUtil.getCurrentUserId();
        followingService.sendFollowRequest(userId, targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Follow request sent");
    }

    @PostMapping("/inbox/seen")
    public ResponseEntity<String> markInboxSeen() {
        Long userId = authUtil.getCurrentUserId();
        followingService.markInboxSeen(userId);
        return ResponseEntity.ok("Inbox marked as seen");
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptFollowRequest(@RequestParam Long requesterId) {
        Long userId = authUtil.getCurrentUserId();
        followingService.acceptRequest(userId, requesterId);
        return ResponseEntity.ok("Request accepted");
    }

    @DeleteMapping("/discard")
    public ResponseEntity<String> discardFollowRequest(@RequestParam Long requesterId) {
        Long userId = authUtil.getCurrentUserId();
        followingService.discardRequest(userId, requesterId);
        return ResponseEntity.ok("Request discarded");
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<FollowRequestDTO>> incomingRequests() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(followingService.listIncomingRequests(userId));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FollowFriendDTO>> acceptedFollowing() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(followingService.listAcceptedFollowing(userId));
    }

    @DeleteMapping("/unfriend")
    public ResponseEntity<String> unfriend(@RequestParam Long friendId) {
        Long userId = authUtil.getCurrentUserId();
        followingService.unfriend(userId, friendId);
        return ResponseEntity.ok("Friendship removed");
    }
}

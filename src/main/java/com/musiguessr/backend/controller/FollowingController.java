package com.musiguessr.backend.controller;

import com.musiguessr.backend.dto.following.FollowFriendDTO;
import com.musiguessr.backend.dto.following.FollowRequestDTO;
import com.musiguessr.backend.service.FollowingService;
import com.musiguessr.backend.util.AuthUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/followings")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;

    @PostMapping("/request")
    public ResponseEntity<String> sendFollowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long targetUserId
    ) {
        Long userId = AuthUtil.requireUserId(userDetails, followingService.getUserRepository());
        followingService.sendFollowRequest(userId, targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Follow request sent");
    }

    @PostMapping("/inbox/seen")
    public ResponseEntity<String> markInboxSeen(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = AuthUtil.requireUserId(userDetails, followingService.getUserRepository());
        followingService.markInboxSeen(userId);
        return ResponseEntity.ok("Inbox marked as seen");
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptFollowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long requesterId
    ) {
        Long userId = AuthUtil.requireUserId(userDetails, followingService.getUserRepository());
        followingService.acceptRequest(userId, requesterId);
        return ResponseEntity.ok("Request accepted");
    }

    @DeleteMapping("/discard")
    public ResponseEntity<String> discardFollowRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long requesterId
    ) {
        Long userId = AuthUtil.requireUserId(userDetails, followingService.getUserRepository());
        followingService.discardRequest(userId, requesterId);
        return ResponseEntity.ok("Request discarded");
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<FollowRequestDTO>> incomingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = AuthUtil.requireUserId(userDetails, followingService.getUserRepository());
        return ResponseEntity.ok(followingService.listIncomingRequests(userId));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FollowFriendDTO>> acceptedFollowing(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = AuthUtil.requireUserId(userDetails, followingService.getUserRepository());
        return ResponseEntity.ok(followingService.listAcceptedFollowing(userId));
    }
}

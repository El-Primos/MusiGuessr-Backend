package com.musiguessr.backend.service;

import com.musiguessr.backend.dto.following.FollowFriendDTO;
import com.musiguessr.backend.dto.following.FollowRequestDTO;
import com.musiguessr.backend.model.Following;
import com.musiguessr.backend.model.FollowingId;
import com.musiguessr.backend.repository.FollowingRepository;
import com.musiguessr.backend.repository.UserRepository;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private final FollowingRepository followingRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendFollowRequest(Long requesterId, Long targetId) {
        validateUsers(requesterId, targetId);
        if (Objects.equals(requesterId, targetId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }
        FollowingId id = new FollowingId(requesterId, targetId);
        Optional<Following> existing = followingRepository.findById(id);
        if (existing.isPresent()) {
            Following f = existing.get();
            String msg = Boolean.TRUE.equals(f.getAccepted())
                    ? "You already follow this user"
                    : "Follow request already sent and pending";
            throw new ResponseStatusException(HttpStatus.CONFLICT, msg);
        }

        // Check reverse direction: the target already requested the requester
        Optional<Following> reverse = followingRepository.findById(new FollowingId(targetId, requesterId));
        if (reverse.isPresent()) {
            Following f = reverse.get();
            String msg = Boolean.TRUE.equals(f.getAccepted())
                    ? "You are already friends with this user"
                    : "User already sent you a request; please accept instead";
            throw new ResponseStatusException(HttpStatus.CONFLICT, msg);
        }

        Following f = new Following();
        f.setId(id);
        f.setUser(userRepository.getReferenceById(requesterId));
        f.setFollowing(userRepository.getReferenceById(targetId));
        f.setPending(false);      // unseen for target
        f.setAccepted(false);     // awaiting decision
        followingRepository.save(f);
    }

    @Transactional
    public void markInboxSeen(Long userId) {
        ensureUserExists(userId);
        // pending=false means unseen in the frontend inbox; when user opens the box we flip to true to clear the “new requests” warning
        List<Following> incoming = followingRepository.findByIdFollowingId(userId);
        incoming.stream()
                .filter(f -> !Boolean.TRUE.equals(f.getPending()))
                .forEach(f -> f.setPending(true));
        followingRepository.saveAll(incoming);
    }

    @Transactional
    public void acceptRequest(Long userId, Long requesterId) {
        ensureUserExists(userId);
        Following f = followingRepository.findById(new FollowingId(requesterId, userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (Boolean.TRUE.equals(f.getAccepted())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already friends");
        }
        f.setAccepted(true);
        f.setPending(true);
        followingRepository.save(f);
    }

    @Transactional
    public void discardRequest(Long userId, Long requesterId) {
        ensureUserExists(userId);
        FollowingId id = new FollowingId(requesterId, userId);
        Following f = followingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (Boolean.TRUE.equals(f.getAccepted())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already friends");
        }
        followingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FollowRequestDTO> listIncomingRequests(Long userId) {
        ensureUserExists(userId);
        return followingRepository.findByIdFollowingIdAndAcceptedFalse(userId)
                .stream()
                .map(this::mapToRequestDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowFriendDTO> listAcceptedFollowing(Long userId) {
        ensureUserExists(userId);
        List<FollowFriendDTO> outgoing = followingRepository.findByIdUserIdAndAcceptedTrue(userId)
                .stream()
                .map(this::mapToFriendDTOOutgoing)
                .toList();

        List<FollowFriendDTO> incoming = followingRepository.findByIdFollowingIdAndAcceptedTrue(userId)
                .stream()
                .map(this::mapToFriendDTOIncoming)
                .toList();

        Map<Long, FollowFriendDTO> dedup = new LinkedHashMap<>();
        java.util.stream.Stream.concat(outgoing.stream(), incoming.stream())
                .filter(dto -> dto.getUserId() != null)
                .forEach(dto -> dedup.put(dto.getUserId(), dto));

        return new java.util.ArrayList<>(dedup.values());
    }

    @Transactional
    public void unfriend(Long userId, Long friendId) {
        ensureUserExists(userId);
        ensureUserExists(friendId);

        // Check both directions since friendship can be established either way
        FollowingId idOutgoing = new FollowingId(userId, friendId);
        FollowingId idIncoming = new FollowingId(friendId, userId);

        Optional<Following> outgoing = followingRepository.findById(idOutgoing);
        Optional<Following> incoming = followingRepository.findById(idIncoming);

        // Find the accepted friendship
        Following friendship = null;
        FollowingId friendshipId = null;

        if (outgoing.isPresent() && Boolean.TRUE.equals(outgoing.get().getAccepted())) {
            friendship = outgoing.get();
            friendshipId = idOutgoing;
        } else if (incoming.isPresent() && Boolean.TRUE.equals(incoming.get().getAccepted())) {
            friendship = incoming.get();
            friendshipId = idIncoming;
        }

        if (friendship == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found");
        }

        followingRepository.deleteById(friendshipId);
    }

    private void validateUsers(Long requesterId, Long targetId) {
        ensureUserExists(requesterId);
        ensureUserExists(targetId);
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    // ------------- mapping helpers -------------
    private FollowRequestDTO mapToRequestDTO(Following f) {
        return new FollowRequestDTO(
                f.getId().getUserId(),
                f.getUser() != null ? f.getUser().getUsername() : null,
                f.getPending(),
                f.getAccepted()
        );
    }

    private FollowFriendDTO mapToFriendDTOOutgoing(Following f) {
        return new FollowFriendDTO(
                f.getId().getFollowingId(),
                f.getFollowing() != null ? f.getFollowing().getUsername() : null
        );
    }

    private FollowFriendDTO mapToFriendDTOIncoming(Following f) {
        return new FollowFriendDTO(
                f.getId().getUserId(),
                f.getUser() != null ? f.getUser().getUsername() : null
        );
    }
}

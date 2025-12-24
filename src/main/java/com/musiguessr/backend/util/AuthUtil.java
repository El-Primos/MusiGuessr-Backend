package com.musiguessr.backend.util;

import com.musiguessr.backend.model.User;
import com.musiguessr.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

public final class AuthUtil {

    private AuthUtil() {}

    /**
     * Resolve the authenticated user's id from the provided UserDetails.
     * Throws 401 if unauthenticated or the user cannot be found.
     */
    public static Long requireUserId(UserDetails userDetails, UserRepository userRepository) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }
}

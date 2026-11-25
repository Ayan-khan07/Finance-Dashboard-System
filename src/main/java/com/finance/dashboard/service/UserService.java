package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.CreateUserRequest;
import com.finance.dashboard.dto.request.UpdateUserRequest;
import com.finance.dashboard.dto.response.PageResponse;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.UserStatus;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ConflictException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.mapper.UserMapper;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.PageableSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final List<String> SORTABLE_FIELDS =
            List.of("id", "username", "email", "role", "status", "createdAt");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        Pageable safe = PageableSanitizer.sanitize(pageable, SORTABLE_FIELDS, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(userRepository.findAll(safe).map(UserMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email '" + email + "' is already registered");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(request.status() != null ? request.status() : UserStatus.ACTIVE)
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    /**
     * Updates role and/or status. An administrator may not change their own role or status:
     * that rules out accidentally locking the last admin out of the system.
     * (Users are deactivated rather than deleted because financial records reference them.)
     */
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, Long actingUserId) {
        if (request.role() == null && request.status() == null) {
            throw new BadRequestException("Provide at least one of: role, status");
        }
        if (id.equals(actingUserId)) {
            throw new BadRequestException("You cannot change your own role or status");
        }

        User user = findOrThrow(id);
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return UserMapper.toResponse(user);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}

package com.worklyn.modules.auth.dto;
import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.model.User;
import java.time.Instant;
import java.util.Set;
public record UserResponse(String id, String email, Set<Role> roles, String employeeId, Instant createdAt) {
    public static UserResponse from(User u){return new UserResponse(u.getId(),u.getEmail(),Set.copyOf(u.getRoles()),u.getEmployeeId(),u.getCreatedAt());}
}

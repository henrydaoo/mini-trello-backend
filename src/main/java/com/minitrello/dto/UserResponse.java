package com.minitrello.dto;

import com.minitrello.entity.NotificationPreference;
import com.minitrello.entity.Role;
import com.minitrello.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private NotificationPreference notificationPreference;

    public static UserResponse fromEntity(User user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .notificationPreference(user.getNotificationPreference())
                .build();
    }
}

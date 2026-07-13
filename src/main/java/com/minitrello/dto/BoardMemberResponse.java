package com.minitrello.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoardMemberResponse {
    private Long userId;
    private String username;
    private String email;
}

package com.naman.redis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String username;
    private boolean isPremium;
}

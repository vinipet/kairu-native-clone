package com.agents.demo.dto.member;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberResponseDTO {

    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    // getters e setters
}

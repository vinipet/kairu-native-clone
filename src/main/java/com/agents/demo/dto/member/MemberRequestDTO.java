package com.agents.demo.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MemberRequestDTO {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    // getters e setters
}

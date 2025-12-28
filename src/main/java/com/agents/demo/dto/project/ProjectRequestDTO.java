package com.agents.demo.dto.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectRequestDTO {

    @NotBlank
    private String name;

    private String description;

    private LocalDateTime deadline;
}

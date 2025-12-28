package com.agents.demo.dto.task;

import com.agents.demo.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequestDTO {

    @NotBlank
    private String title;

    private String description;

    private Priority priority;

    @NotNull
    private Long projectId;

    private Long assignedToId;
}

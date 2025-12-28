package com.agents.demo.dto.task;

import com.agents.demo.enums.Priority;
import com.agents.demo.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;

    private Long projectId;
    private String projectName;

    private Long assignedToId;
    private String assignedToName;

    private LocalDateTime createdAt;
    private LocalDateTime startTime;

}

package com.agents.demo.dto.comment;

import java.time.LocalDateTime;

public class CommentResponseDTO {

    private Long id;
    private String content;
    private Long taskId;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;

    // getters e setters
}

package com.agents.demo.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDTO {

    @NotBlank
    private String content;

    @NotNull
    private Long taskId;

    @NotNull
    private Long authorId;

    // getters e setters
}

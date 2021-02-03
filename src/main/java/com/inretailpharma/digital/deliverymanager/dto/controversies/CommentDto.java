package com.inretailpharma.digital.deliverymanager.dto.controversies;
import lombok.Data;

@Data
public class CommentDto {
    private String date;
    private String text;
    private AuthorDto author;
}

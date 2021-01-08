package com.inretailpharma.digital.deliverymanager.dto.controversies;

import com.inretailpharma.digital.sellercenter.dto.controversies.AuthorDto;
import lombok.Data;

@Data
public class CommentDto {
    private String date;
    private String text;
    private AuthorDto author;
}

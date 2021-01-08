package com.inretailpharma.digital.deliverymanager.dto.controversies;

import java.util.List;

import com.inretailpharma.digital.sellercenter.dto.controversies.AuthorDto;
import com.inretailpharma.digital.sellercenter.dto.controversies.CommentDto;
import lombok.Data;

@Data
public class ControversyDto {
    private Integer id;
    private String date;
    private String text;
    private Boolean active;
    private String type;
    private List<CommentDto> comments;
    private String orderId;
    private AuthorDto author;
    private String accountName;
}

package com.inretailpharma.digital.deliverymanager.dto.controversies;

import java.util.List;
import lombok.Data;

@Data
public class ControversyRequestDto {
    private String date;
    private String text;
    private String type;
    private Double monto;
    private String controversyStatus;
    private List<CommentRequestDto> comments;
}

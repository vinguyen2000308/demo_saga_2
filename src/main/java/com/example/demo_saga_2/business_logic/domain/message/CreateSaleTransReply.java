package com.example.demo_saga_2.business_logic.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateSaleTransReply implements ReplyMessage {

    private String code;
    private String message;
    private Long saleTransId;

}

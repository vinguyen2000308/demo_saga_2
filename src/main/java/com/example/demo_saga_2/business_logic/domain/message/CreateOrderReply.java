package com.example.demo_saga_2.business_logic.domain.message;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class CreateOrderReply implements ReplyMessage {

    private String code;
    private String message;
    private Long orderId;

}

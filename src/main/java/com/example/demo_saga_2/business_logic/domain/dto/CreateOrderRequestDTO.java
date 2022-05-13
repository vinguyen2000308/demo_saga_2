package com.example.demo_saga_2.business_logic.domain.dto;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class CreateOrderRequestDTO {

    private Long customerId;
    private Boolean isNewCustomer;
    private List<OrderItem> orderItemList;
}

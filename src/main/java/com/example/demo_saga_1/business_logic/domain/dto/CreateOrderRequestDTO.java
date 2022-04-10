package com.example.demo_saga_1.business_logic.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateOrderRequestDTO {

    private Long customerId;
    private Boolean isNewCustomer;
    private List<OrderItem> orderItemList;
}

package com.example.demo_saga_2.business_logic.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderItem {

    private String name;
    private Integer total;
    private Long productId;
    private Double price;
}

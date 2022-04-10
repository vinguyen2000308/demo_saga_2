package com.example.demo_saga_1.business_logic.domain.command;

import com.example.demo_saga_1.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_1.domain.Command;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CreateOrderCommand implements Command {

    private Long customerId;
    private Boolean isNewCustomer;
    private List<OrderItem> orderItemList;
}

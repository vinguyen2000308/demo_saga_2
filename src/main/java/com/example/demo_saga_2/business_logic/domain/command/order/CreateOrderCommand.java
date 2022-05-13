package com.example.demo_saga_2.business_logic.domain.command.order;

import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_2.domain.Command;
import com.example.demo_saga_2.domain.CommandWithDestination;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CreateOrderCommand implements Command, CommandWithDestination {

    private Long customerId;
    private Boolean isNewCustomer;
    private List<OrderItem> orderItemList;

    @Override
    public String getDestination() {
        return Const.ORDER_SERVICE;
    }
}

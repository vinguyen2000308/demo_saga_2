package com.example.demo_saga_2.business_logic.domain.command;


import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.domain.Command;
import com.example.demo_saga_2.domain.CommandWithDestination;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ValidateCustomerCommand implements CommandWithDestination {
    private Long customerId;
    private Long orderId;

    @Override
    public String getDestination() {
        return Const.CUSTOMER_SERVICE;
    }
}

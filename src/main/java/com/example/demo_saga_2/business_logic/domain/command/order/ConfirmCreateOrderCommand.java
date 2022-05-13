package com.example.demo_saga_2.business_logic.domain.command.order;

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
public class ConfirmCreateOrderCommand implements CommandWithDestination {

    private Long orderId;

    @Override
    public String getDestination() {
        return Const.ORDER_SERVICE;
    }
}

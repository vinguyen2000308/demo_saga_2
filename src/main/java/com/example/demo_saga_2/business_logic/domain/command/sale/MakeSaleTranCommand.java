package com.example.demo_saga_2.business_logic.domain.command.sale;

import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_2.domain.Command;
import com.example.demo_saga_2.domain.CommandWithDestination;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class MakeSaleTranCommand implements  CommandWithDestination {

        private Long customerId;
        private Long orderId;
        private List<OrderItem> orderItemList;
        private Double tax;

        @Override
        public String getDestination() {
                return Const.SALE_TRAN_SERVICE;
        }
}

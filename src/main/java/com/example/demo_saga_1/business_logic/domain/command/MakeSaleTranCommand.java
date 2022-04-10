package com.example.demo_saga_1.business_logic.domain.command;

import com.example.demo_saga_1.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_1.domain.Command;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class MakeSaleTranCommand implements Command {

        private Long customerId;
        private List<OrderItem> orderItemList;

}

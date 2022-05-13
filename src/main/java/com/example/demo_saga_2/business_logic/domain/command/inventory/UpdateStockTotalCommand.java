package com.example.demo_saga_2.business_logic.domain.command.inventory;

import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.config.CustomLocalDateTimeSerializer;
import com.example.demo_saga_2.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_2.domain.CommandWithDestination;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UpdateStockTotalCommand implements CommandWithDestination {

    private List<OrderItem> orderItemList;
    private String actionUser;
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime updatedDate;

    @Override
    public String getDestination() {
        return Const.INVENTORY_SERVICE;
    }
}

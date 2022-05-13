package com.example.demo_saga_2.business_logic.domain.command.inventory;

import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.domain.dto.UpdateStockTotalItemDTO;
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
public class CancelUpdateStockTotalCommand implements CommandWithDestination {
    private List<UpdateStockTotalItemDTO> stockTotalItems;

    @Override
    public String getDestination() {
        return Const.INVENTORY_SERVICE;
    }
}

package com.example.demo_saga_2.business_logic.domain.message;

import com.example.demo_saga_2.business_logic.domain.dto.UpdateStockTotalItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateStockTotalReply implements ReplyMessage {
    private String code;
    private String message;
//    One product -> one stock total id -> one quanity
    private List<UpdateStockTotalItemDTO> stockTotalItems;


}

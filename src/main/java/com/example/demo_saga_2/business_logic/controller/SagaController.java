package com.example.demo_saga_2.business_logic.controller;

import com.example.demo_saga_2.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_2.business_logic.service.SagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saga")
public class SagaController {


    @Autowired
    private SagaService sagaService;

    @PostMapping(value = "/create-order")
    public String createOrder(@RequestBody CreateOrderRequestDTO createOrderRequestDTO) throws Exception {
        return sagaService.createOrder(createOrderRequestDTO);
    }

    @PostMapping(value = "/create-order-inventory")
    public String createOrderInventory(@RequestBody CreateOrderRequestDTO createOrderRequestDTO) throws Exception {
        return sagaService.createOrderWithInventory(createOrderRequestDTO);
    }


}

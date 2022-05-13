package com.example.demo_saga_2.business_logic.service;

import com.example.demo_saga_2.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_2.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_2.business_logic.saga.MessageRepo;
import com.example.demo_saga_2.business_logic.saga.create_order_inventory_saga.CreateOrderInventSagaData;
import com.example.demo_saga_2.business_logic.saga.create_order_inventory_saga.CreateOrderInventorySaga;
import com.example.demo_saga_2.business_logic.saga.create_order_saga.CreateOrderSaga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class SagaService {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private MessageRepo messageRepo;

    public String createOrder(CreateOrderRequestDTO createOrderRequestDTO) throws ExecutionException, InterruptedException, TimeoutException {
        CreateOrderSaga createOrderSaga = new CreateOrderSaga(createOrderRequestDTO, kafkaProducer, messageRepo);
        Future<String> createOrderTask = executorService.submit(createOrderSaga);
        return createOrderTask.get(180,TimeUnit.SECONDS);
    }
    public String createOrderWithInventory(CreateOrderRequestDTO createOrderRequestDTO) throws ExecutionException, InterruptedException, TimeoutException {
        CreateOrderInventorySaga createOrderInventorySaga = new CreateOrderInventorySaga(createOrderRequestDTO, kafkaProducer, messageRepo);
        Future<String> createOrderInventoryTask = executorService.submit(createOrderInventorySaga);
        return createOrderInventoryTask.get(30,TimeUnit.SECONDS);
    }


}

package com.example.demo_saga_1.business_logic.service;

import com.example.demo_saga_1.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_1.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_1.business_logic.saga.MessageRepo;
import com.example.demo_saga_1.business_logic.saga.create_order_saga.CreateOrderSaga;
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

    public String createOrder(CreateOrderRequestDTO createOrderRequestDTO) throws ExecutionException, InterruptedException {
        CreateOrderSaga createOrderSaga = new CreateOrderSaga(createOrderRequestDTO, kafkaProducer, messageRepo);
        Future<String> createOrderTask = executorService.submit(createOrderSaga);
        return createOrderTask.get();

    }


}

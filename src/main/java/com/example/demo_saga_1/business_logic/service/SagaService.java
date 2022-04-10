package com.example.demo_saga_1.business_logic.service;

import com.example.demo_saga_1.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_1.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_1.business_logic.saga.create_order_saga.CreateOrderSaga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class SagaService {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private ExecutorService executorService;


    public String createOrder(CreateOrderRequestDTO createOrderRequestDTO) throws ExecutionException, InterruptedException {
        CreateOrderSaga createOrderSaga = new CreateOrderSaga(createOrderRequestDTO, kafkaProducer);
        Future<String> createOrderTask = executorService.submit(createOrderSaga);
        return createOrderTask.get();
    }


}

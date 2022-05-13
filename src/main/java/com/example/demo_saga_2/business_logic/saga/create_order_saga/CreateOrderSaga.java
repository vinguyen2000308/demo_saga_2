package com.example.demo_saga_2.business_logic.saga.create_order_saga;

import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_2.business_logic.handler.SagaHandlerReplyContext;
import com.example.demo_saga_2.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_2.business_logic.saga.MessageRepo;
import com.example.demo_saga_2.business_logic.saga.Saga;
import com.example.demo_saga_2.domain.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

@Slf4j
public class CreateOrderSaga implements Saga, Callable<String> {

    private final KafkaProducer kafkaProducer;
    private final CreateOrderRequestDTO createOrderRequestDTO;
    private final MessageRepo messageRepo;
    public Map<String, Data> messageApplicationContext = SagaHandlerReplyContext.getApplicationContext();
    private CreateOrderSagaData createOrderSagaData;
    private String sagaId;

    public CreateOrderSaga(CreateOrderRequestDTO createOrderRequestDTO, KafkaProducer kafkaProducer, MessageRepo messageRepo) {
        this.kafkaProducer = kafkaProducer;
        this.createOrderRequestDTO = createOrderRequestDTO;
        this.messageRepo = messageRepo;
    }

    @Override
    public String call() {
        this.createOrderSagaData = new CreateOrderSagaData(createOrderRequestDTO, kafkaProducer,this, messageRepo);
        this.sagaId = this.createOrderSagaData.getSagaId();

        log.info(Const.START_SAGA_INFO, this.getClass().getSimpleName(), sagaId);
        createOrderSagaData.makeCreateOrderCommand();
        try {
            while (true) {
                Data data = messageApplicationContext.get(sagaId);
                if (Objects.isNull(data))
                    continue;
                // ORDER SERVICE
                if (createOrderSagaData.handleReplyFromOrder(data))
                    continue;
                // CUSTOMER SERVICE
                if (createOrderSagaData.handleReplyFromCustomer(data))
                    continue;
                // SALE TRAN SERVICE
                if (Objects.nonNull(data))
                    return createOrderSagaData.handleReplyFromSale(data);
                else
                    continue;
            }
        }catch (Exception e)
        {
            return e.getMessage();
        }
    }


}

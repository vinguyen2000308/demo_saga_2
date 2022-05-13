package com.example.demo_saga_2.business_logic.saga.create_order_inventory_saga;

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
public class CreateOrderInventorySaga implements Saga, Callable<String> {

    private final KafkaProducer kafkaProducer;
    private final CreateOrderRequestDTO createOrderRequestDTO;
    private final MessageRepo messageRepo;
    public Map<String, Data> messageApplicationContext = SagaHandlerReplyContext.getApplicationContext();
    private CreateOrderInventSagaData createOrderInventSagaData;
    private String sagaId;

    public CreateOrderInventorySaga(CreateOrderRequestDTO createOrderRequestDTO, KafkaProducer kafkaProducer, MessageRepo messageRepo) {
        this.kafkaProducer = kafkaProducer;
        this.createOrderRequestDTO = createOrderRequestDTO;
        this.messageRepo = messageRepo;
    }

    @Override
    public String call() throws Exception {

        this.createOrderInventSagaData = new CreateOrderInventSagaData(createOrderRequestDTO, kafkaProducer, this, messageRepo);
        this.sagaId = this.createOrderInventSagaData.getSagaId();

        log.info(Const.START_SAGA_INFO, this.getClass().getSimpleName(), sagaId);
        createOrderInventSagaData.makeCreateOrderCommand();
        try {
            while (true) {
                Data data = messageApplicationContext.get(sagaId);
                if (Objects.isNull(data))
                    continue;
                // ORDER SERVICE
                if (createOrderInventSagaData.handleReplyFromOrder(data))
                    continue;
                // CUSTOMER SERVICE
                if (createOrderInventSagaData.handleReplyFromCustomer(data))
                    continue;
//                INVENTORY
                if (createOrderInventSagaData.handleReplyFromInventory(data))
                    continue;
                // SALE TRAN SERVICE
                if (Objects.nonNull(data))
                    return createOrderInventSagaData.handleReplyFromSale(data);
                else
                    continue;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }


}

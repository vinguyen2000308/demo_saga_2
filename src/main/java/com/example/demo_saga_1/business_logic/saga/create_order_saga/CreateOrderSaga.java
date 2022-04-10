package com.example.demo_saga_1.business_logic.saga.create_order_saga;

import com.example.demo_saga_1.business_logic.common.Const;
import com.example.demo_saga_1.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_1.business_logic.handler.SagaHandlerReply;
import com.example.demo_saga_1.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_1.business_logic.saga.Saga;
import com.example.demo_saga_1.business_logic.saga.SagaInstance;
import com.example.demo_saga_1.business_logic.saga.SagaInstanceRepo;
import com.example.demo_saga_1.domain.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

@Slf4j
public class CreateOrderSaga implements Saga, Callable<String> {

    private final KafkaProducer kafkaProducer;
    private final CreateOrderRequestDTO createOrderRequestDTO;
    public Map<String, Data> messageApplicationContext = SagaHandlerReply.result;
    private CreateOrderSagaData createOrderSagaData;
    private String sagaId;

    @Autowired
    public SagaInstanceRepo sagaInstanceRepo;

    public CreateOrderSaga(CreateOrderRequestDTO createOrderRequestDTO, KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.createOrderRequestDTO = createOrderRequestDTO;
    }

    @Override
    public String call() throws Exception {
        this.createOrderSagaData = new CreateOrderSagaData(createOrderRequestDTO, kafkaProducer, this);
        this.sagaId = this.createOrderSagaData.getSagaId();
        sagaInstanceRepo.save(SagaInstance.builder()
                        .sagaId(this.sagaId)
                .build());
        log.info(Const.START_SAGA_INFO, this.getClass().getSimpleName(), sagaId);
        createOrderSagaData.makeCreateOrderCommand();
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
            if (Objects.nonNull(createOrderSagaData.handleReplyFromSale(data)))
                return createOrderSagaData.handleReplyFromSale(data);
        }
    }


}

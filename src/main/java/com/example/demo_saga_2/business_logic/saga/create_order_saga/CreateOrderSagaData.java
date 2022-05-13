package com.example.demo_saga_2.business_logic.saga.create_order_saga;


import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.domain.command.*;
import com.example.demo_saga_2.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_2.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_2.business_logic.domain.message.CreateOrderReply;
import com.example.demo_saga_2.business_logic.domain.message.CreateSaleTransReply;
import com.example.demo_saga_2.business_logic.domain.message.ValidateCustomerReply;
import com.example.demo_saga_2.business_logic.handler.SagaHandlerReplyContext;
import com.example.demo_saga_2.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_2.business_logic.saga.MessageRepo;
import com.example.demo_saga_2.business_logic.saga.Saga;
import com.example.demo_saga_2.business_logic.saga.SagaData;
import com.example.demo_saga_2.domain.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

import static com.example.demo_saga_2.business_logic.saga.MessageUtil.*;


@Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CreateOrderSagaData implements SagaData {

    private String sagaId;
    private Long customerId;
    private boolean isNewCustomer;
    private List<OrderItem> orderItemList;
    private CreateOrderReply replyFromOrder;
    private CreateOrderRequestDTO createOrderRequestDTO;
    @ToString.Exclude private CreateOrderSaga saga;
    @ToString.Exclude private KafkaProducer kafkaProducer;
    @ToString.Exclude private MessageRepo messageRepo;

    public CreateOrderSagaData(CreateOrderRequestDTO createOrderRequestDTO, KafkaProducer kafkaProducer, Saga saga, MessageRepo messageRepo) {
        this.createOrderRequestDTO = createOrderRequestDTO;
        this.kafkaProducer = kafkaProducer;
        this.sagaId = UUID.randomUUID().toString();
        this.saga = (CreateOrderSaga) saga;
        this.messageRepo = messageRepo;
    }

    @Override
    public String getSagaId() {
        return sagaId;
    }

    //   ORDER
    public CreateOrderCommand makeCreateOrderCommand() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(this.createOrderRequestDTO.getCustomerId())
                .isNewCustomer(this.createOrderRequestDTO.getIsNewCustomer())
                .orderItemList(this.createOrderRequestDTO.getOrderItemList())
                .build();
        kafkaProducer.sendFirstMessage(saga, command, Const.ORDER_SERVICE, this);
        return command;
    }

    private ConfirmCreateOrderCommand makeConfirmCreateOrderCommand() {
        ConfirmCreateOrderCommand command = ConfirmCreateOrderCommand.builder()
                .orderId(replyFromOrder.getOrderId())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.ORDER_SERVICE, this);
        return command;
    }

    public boolean handleReplyFromOrder(Data data) throws Exception {
        if (checkReplyType(getReplyType(data), CreateOrderReply.class)) {
            CreateOrderReply createOrderReply = (CreateOrderReply) SagaHandlerReplyContext.getReplyMessage(data);
            if (checkReply(createOrderReply)) {
                SagaHandlerReplyContext.remove(sagaId);
                this.replyFromOrder = createOrderReply;
                saveReplyMessage(data, messageRepo);
                makeValidateCustomerCommand();
                return setSuccess();
            } else {
                SagaHandlerReplyContext.remove(sagaId);
                throw new Exception(Const.ERROR_ORDER + createOrderReply.getMessage());
            }
        }
        return setFailure();
    }

    private CancelCreateOrderCommand makeCancelCreateOrderCommand() {
        CancelCreateOrderCommand command = CancelCreateOrderCommand.builder()
                .orderId(replyFromOrder.getOrderId())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.ORDER_SERVICE, this);
        return command;
    }

    //  CUSTOMER
    private ValidateCustomerCommand makeValidateCustomerCommand() {
        ValidateCustomerCommand command = ValidateCustomerCommand.builder()
                .customerId(createOrderRequestDTO.getCustomerId())
                .orderId(replyFromOrder.getOrderId())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.CUSTOMER_SERVICE, this);
        return command;
    }

    public boolean handleReplyFromCustomer(Data data) throws Exception {
        if (checkReplyType(getReplyType(data), ValidateCustomerReply.class)) {
            ValidateCustomerReply validateCustomerReply = (ValidateCustomerReply) SagaHandlerReplyContext.getReplyMessage(data);
            if (checkReply(validateCustomerReply)) {
                SagaHandlerReplyContext.remove(sagaId);
                saveReplyMessage(data, messageRepo);
                makeSaleTranCommand();
                return setSuccess();
            } else {
                makeCancelCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                throw new Exception(Const.ERROR_CUSTOMER + validateCustomerReply.getMessage());
            }
        }
        return setFailure();
    }

    //    SALE TRANS
    private MakeSaleTranCommand makeSaleTranCommand() {
        MakeSaleTranCommand command = MakeSaleTranCommand.builder()
                .customerId(createOrderRequestDTO.getCustomerId())
                .orderId(replyFromOrder.getOrderId())
                .orderItemList(createOrderRequestDTO.getOrderItemList())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.SALE_TRAN_SERVICE, this);
        return command;
    }

    public String handleReplyFromSale(Data data) {
        if (checkReplyType(getReplyType(data), CreateSaleTransReply.class)) {
            CreateSaleTransReply createSaleTransReply = (CreateSaleTransReply) SagaHandlerReplyContext.getReplyMessage(data);
            if (checkReply(createSaleTransReply)) {
                saveReplyMessage(data, messageRepo);
                makeConfirmCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                return Const.SAGA_SUCCESS + createSaleTransReply.getSaleTransId() + " saga id " + sagaId;
            } else {
                // send rollback order
                makeCancelCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                return Const.ERROR_SALE_TRANS + createSaleTransReply.getMessage();
            }
        }
        return null;

    }


}




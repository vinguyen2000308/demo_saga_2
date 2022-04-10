package com.example.demo_saga_1.business_logic.saga.create_order_saga;


import com.example.demo_saga_1.business_logic.common.Const;
import com.example.demo_saga_1.business_logic.domain.command.*;
import com.example.demo_saga_1.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_1.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_1.business_logic.domain.message.CreateOrderReply;
import com.example.demo_saga_1.business_logic.domain.message.CreateSaleTransReply;
import com.example.demo_saga_1.business_logic.domain.message.ValidateCustomerReply;
import com.example.demo_saga_1.business_logic.handler.SagaHandlerReply;
import com.example.demo_saga_1.business_logic.kafka.KafkaProducer;
import com.example.demo_saga_1.business_logic.saga.Saga;
import com.example.demo_saga_1.business_logic.saga.SagaData;
import com.example.demo_saga_1.domain.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

import static com.example.demo_saga_1.business_logic.saga.MessageUtil.*;


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
    private Saga saga;
    private KafkaProducer kafkaProducer;

    public CreateOrderSagaData(CreateOrderRequestDTO createOrderRequestDTO, KafkaProducer kafkaProducer, Saga saga) {
        this.createOrderRequestDTO = createOrderRequestDTO;
        this.kafkaProducer = kafkaProducer;
        this.sagaId = UUID.randomUUID().toString();
        this.saga = saga;
    }

    //   ORDER
    public CreateOrderCommand makeCreateOrderCommand() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(this.createOrderRequestDTO.getCustomerId())
                .isNewCustomer(this.createOrderRequestDTO.getIsNewCustomer())
                .orderItemList(this.createOrderRequestDTO.getOrderItemList())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.ORDER_SERVICE, sagaId);
        return command;
    }
    private ConfirmCreateOrderCommand makeConfirmCreateOrderCommand(Long orderId) {
        return ConfirmCreateOrderCommand.builder()
                .orderId(orderId)
                .build();
    }
    public boolean handleReplyFromOrder(Data data) throws Exception {
        if (checkReplyType(getReplyType(data), CreateOrderReply.class)) {
            CreateOrderReply createOrderReply = (CreateOrderReply) SagaHandlerReply.getReplyMessage(data);
            if (checkReply(createOrderReply)) {
                SagaHandlerReply.result.remove(sagaId);
                makeValidateCustomerCommand();
                return setSuccess();
            } else {
                SagaHandlerReply.result.remove(sagaId);
                throw new Exception("Error in Order" + createOrderReply.getMessage());
            }
        }
        return setFailure();

    }
    private CancelCreateOrderCommand makeCancelCreateOrderCommand() {
        CancelCreateOrderCommand command = CancelCreateOrderCommand.builder()
                .orderId(replyFromOrder.getOrderId())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.ORDER_SERVICE, sagaId);
        return command;
    }

    //  CUSTOMER
    private ValidateCustomerCommand makeValidateCustomerCommand() {
        ValidateCustomerCommand command = ValidateCustomerCommand.builder()
                .customerId(createOrderRequestDTO.getCustomerId())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.CUSTOMER_SERVICE, sagaId);
        return command;
    }
    public boolean handleReplyFromCustomer(Data data) throws Exception {
        if (checkReplyType(getReplyType(data), ValidateCustomerReply.class)) {
            ValidateCustomerReply validateCustomerReply = (ValidateCustomerReply) SagaHandlerReply.getReplyMessage(data);
            if (checkReply(validateCustomerReply)) {
                SagaHandlerReply.result.remove(sagaId);
                makeSaleTranCommand();
                return setSuccess();
            } else {
                makeCancelCreateOrderCommand();
                SagaHandlerReply.result.remove(sagaId);
                throw new Exception("Error in Customer" + validateCustomerReply.getMessage());

            }
        }
        return setFailure();
    }

    //    SALE TRANS
    private MakeSaleTranCommand makeSaleTranCommand() {
        MakeSaleTranCommand command = MakeSaleTranCommand.builder()
                .customerId(createOrderRequestDTO.getCustomerId())
                .orderItemList(createOrderRequestDTO.getOrderItemList())
                .build();
        kafkaProducer.sendMessage(saga, command, Const.SALE_TRAN_SERVICE, sagaId);
        return command;
    }
    public String handleReplyFromSale(Data data) {
        if (checkReplyType(getReplyType(data), CreateSaleTransReply.class)) {
            CreateSaleTransReply createSaleTransReply = (CreateSaleTransReply) SagaHandlerReply.getReplyMessage(data);
            if (checkReply(createSaleTransReply)) {
                SagaHandlerReply.result.remove(sagaId);
                kafkaProducer.sendMessage(saga, makeConfirmCreateOrderCommand(replyFromOrder.getOrderId()), Const.ORDER_SERVICE, sagaId);
                return "Create Order Success " + createSaleTransReply.getSaleTransId() + " saga id " + sagaId;
            } else {
                // send rollback order
                makeCancelCreateOrderCommand();
                SagaHandlerReply.result.remove(sagaId);
                return "Error in make Sale Trans" + createSaleTransReply.getMessage();
            }
        }
        return null;

    }


}




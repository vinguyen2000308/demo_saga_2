package com.example.demo_saga_2.business_logic.saga.create_order_inventory_saga;

import com.example.demo_saga_2.business_logic.common.Const;
import com.example.demo_saga_2.business_logic.domain.command.customer.ValidateCustomerCommand;
import com.example.demo_saga_2.business_logic.domain.command.inventory.CancelUpdateStockTotalCommand;
import com.example.demo_saga_2.business_logic.domain.command.inventory.ConfirmUpdateStockTotalCommand;
import com.example.demo_saga_2.business_logic.domain.command.inventory.UpdateStockTotalCommand;
import com.example.demo_saga_2.business_logic.domain.command.order.CancelCreateOrderCommand;
import com.example.demo_saga_2.business_logic.domain.command.order.ConfirmCreateOrderCommand;
import com.example.demo_saga_2.business_logic.domain.command.order.CreateOrderCommand;
import com.example.demo_saga_2.business_logic.domain.command.sale.MakeSaleTranCommand;
import com.example.demo_saga_2.business_logic.domain.dto.CreateOrderRequestDTO;
import com.example.demo_saga_2.business_logic.domain.dto.OrderItem;
import com.example.demo_saga_2.business_logic.domain.dto.UpdateStockTotalItemDTO;
import com.example.demo_saga_2.business_logic.domain.message.CreateOrderReply;
import com.example.demo_saga_2.business_logic.domain.message.CreateSaleTransReply;
import com.example.demo_saga_2.business_logic.domain.message.UpdateStockTotalReply;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.demo_saga_2.business_logic.saga.MessageUtil.*;

@Builder
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CreateOrderInventSagaData implements SagaData {


    @ToString.Exclude
    private KafkaProducer kafkaProducer;
    @ToString.Exclude
    private MessageRepo messageRepo;
    @ToString.Exclude
    private CreateOrderInventorySaga saga;

    private String sagaId;
    private CreateOrderReply replyFromOrder;
    private Double tax;
    private List<UpdateStockTotalItemDTO> stockTotalItems;

    @Autowired
    private CreateOrderRequestDTO createOrderRequestDTO;

    public CreateOrderInventSagaData(CreateOrderRequestDTO createOrderRequestDTO, KafkaProducer kafkaProducer, Saga saga, MessageRepo messageRepo) {
        this.createOrderRequestDTO = createOrderRequestDTO;
        this.kafkaProducer = kafkaProducer;
        this.sagaId = UUID.randomUUID().toString();
        this.saga = (CreateOrderInventorySaga) saga;
        this.messageRepo = messageRepo;
    }

    //   ORDER
    public CreateOrderCommand makeCreateOrderCommand() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(this.createOrderRequestDTO.getCustomerId())
                .isNewCustomer(this.createOrderRequestDTO.getIsNewCustomer())
                .orderItemList(this.createOrderRequestDTO.getOrderItemList())
                .build();
        kafkaProducer.sendFirstMessage(saga, command, this);
        return command;
    }

    private ConfirmCreateOrderCommand makeConfirmCreateOrderCommand() {
        ConfirmCreateOrderCommand command = ConfirmCreateOrderCommand.builder()
                .orderId(replyFromOrder.getOrderId())
                .build();
        kafkaProducer.sendMessage(saga, command, this);
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
        kafkaProducer.sendMessage(saga, command, this);
        return command;
    }

    //    CUSTOMER
    private ValidateCustomerCommand makeValidateCustomerCommand() {
        ValidateCustomerCommand command = ValidateCustomerCommand.builder()
                .customerId(createOrderRequestDTO.getCustomerId())
                .orderId(replyFromOrder.getOrderId())
                .build();
        kafkaProducer.sendMessage(saga, command, this);
        return command;
    }

    public boolean handleReplyFromCustomer(Data data) throws Exception {
        if (checkReplyType(getReplyType(data), ValidateCustomerReply.class)) {
            ValidateCustomerReply validateCustomerReply = (ValidateCustomerReply) SagaHandlerReplyContext.getReplyMessage(data);
            if (checkReply(validateCustomerReply)) {
                SagaHandlerReplyContext.remove(sagaId);
                this.tax = validateCustomerReply.getTax();
                saveReplyMessage(data, messageRepo);
                makeInventoryCommand();
                return setSuccess();
            } else {
                makeCancelCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                throw new Exception(Const.ERROR_CUSTOMER + validateCustomerReply.getMessage());
            }
        }
        return setFailure();
    }

    //    INVENTORY
    private void makeInventoryCommand() {
        UpdateStockTotalCommand command = UpdateStockTotalCommand.builder()
                .actionUser("Nguyen Vi")
                .orderItemList(this.createOrderRequestDTO.getOrderItemList())
                .updatedDate(LocalDateTime.now())
                .build();
        kafkaProducer.sendMessage(saga, command, this);
    }

    private void makeCancelUpdateStockInventoryCommand() {
        CancelUpdateStockTotalCommand command = CancelUpdateStockTotalCommand.builder()
                .stockTotalItems(this.stockTotalItems)
                .build();
        kafkaProducer.sendMessage(saga, command, this);
    }

    private void makeConfirmUpdateInventoryCommand() {
        ConfirmUpdateStockTotalCommand command = ConfirmUpdateStockTotalCommand.builder()
                .stockTotalItems(this.stockTotalItems)
                .build();
        kafkaProducer.sendMessage(saga, command, this);
    }


    public boolean handleReplyFromInventory(Data data) throws Exception {
        if (checkReplyType(getReplyType(data), UpdateStockTotalReply.class)) {
            UpdateStockTotalReply updateStockTotalReply = (UpdateStockTotalReply) SagaHandlerReplyContext.getReplyMessage(data);
            if (checkReply(updateStockTotalReply)) {
                SagaHandlerReplyContext.remove(sagaId);
                this.stockTotalItems = updateStockTotalReply.getStockTotalItems();
                saveReplyMessage(data, messageRepo);
//                Send command to sale trans
                makeSaleTranCommand();
                return setSuccess();
            } else {
                makeCancelCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                throw new Exception(Const.ERROR_INVENTORY + updateStockTotalReply.getMessage());
            }
        }
        return setFailure();
    }

    //    SALE TRANS
    private MakeSaleTranCommand makeSaleTranCommand() {
        List<OrderItem> orderItemList = createOrderRequestDTO.getOrderItemList();
        for (OrderItem item : orderItemList) {
            for (UpdateStockTotalItemDTO dto : this.stockTotalItems) {
                if (item.getName().equals(dto.getName()))
                    item.setPrice(dto.getPrice());
            }
        }

        MakeSaleTranCommand command = MakeSaleTranCommand.builder()
                .customerId(createOrderRequestDTO.getCustomerId())
                .orderId(replyFromOrder.getOrderId())
                .tax(this.tax)
                .orderItemList(orderItemList)
                .build();
        kafkaProducer.sendMessage(saga, command, this);
        return command;
    }

    public String handleReplyFromSale(Data data) {
        if (checkReplyType(getReplyType(data), CreateSaleTransReply.class)) {
            CreateSaleTransReply createSaleTransReply = (CreateSaleTransReply) SagaHandlerReplyContext.getReplyMessage(data);
            if (checkReply(createSaleTransReply)) {
                saveReplyMessage(data, messageRepo);
//                Make confirm inventory
                makeConfirmUpdateInventoryCommand();
//                Make confirm order
                makeConfirmCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                return Const.SAGA_SUCCESS + createSaleTransReply.getSaleTransId() + " saga id " + sagaId;
            } else {
                // send rollback invent
                makeCancelUpdateStockInventoryCommand();
                // send rollback order
                makeCancelCreateOrderCommand();
                SagaHandlerReplyContext.remove(sagaId);
                return Const.ERROR_SALE_TRANS + createSaleTransReply.getMessage();
            }
        }
        return null;

    }


    @Override
    public String getSagaId() {
        return this.sagaId;
    }
}

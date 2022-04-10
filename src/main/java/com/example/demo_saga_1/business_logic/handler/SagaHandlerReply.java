package com.example.demo_saga_1.business_logic.handler;

import com.example.demo_saga_1.business_logic.domain.message.CreateOrderReply;
import com.example.demo_saga_1.business_logic.domain.message.CreateSaleTransReply;
import com.example.demo_saga_1.business_logic.domain.message.ReplyMessage;
import com.example.demo_saga_1.business_logic.domain.message.ValidateCustomerReply;
import com.example.demo_saga_1.domain.Data;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.example.demo_saga_1.business_logic.saga.MessageUtil.checkReplyType;

@Component
public class SagaHandlerReply {

    public static Map<String,Data> result = new HashMap<>();

    @KafkaListener(topics = "CreateOrderSaga-reply", groupId = "app")
    public void pollingMessage(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("Received Message: " + message);
        Data data = objectMapper.readValue(message, Data.class);
        result.put(data.getHeader().get("saga_id"),data);
        System.out.println("Add " + data.getHeader().get("saga_id") + " to the map [" + result.size() + "]");
    }

    public static ReplyMessage getReplyMessage(Data data) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> header = data.getHeader();
        String reply_type = header.get("type");
        try {
            if (checkReplyType(reply_type, CreateOrderReply.class))
                return objectMapper.readValue(data.getPayload(), CreateOrderReply.class);
            if (checkReplyType(reply_type, ValidateCustomerReply.class))
                return objectMapper.readValue(data.getPayload(), ValidateCustomerReply.class);
            if (checkReplyType(reply_type, CreateSaleTransReply.class))
                return objectMapper.readValue(data.getPayload(), CreateSaleTransReply.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Not support type");
    }
}

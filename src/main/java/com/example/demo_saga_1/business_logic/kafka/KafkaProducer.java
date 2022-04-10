package com.example.demo_saga_1.business_logic.kafka;

import com.example.demo_saga_1.business_logic.saga.*;
import com.example.demo_saga_1.domain.Command;
import com.example.demo_saga_1.domain.Data;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
public class KafkaProducer {


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SagaInstanceRepo sagaInstanceRepo;

    @Autowired
    private MessageRepo messageRepo;


    public Map<String, String> getHeader(Command command, Saga saga, String sagaId) {
        Map<String, String> header = new HashMap<>();
        header.put("message_id", UUID.randomUUID().toString());
        header.put("command_type", command.getClass().getSimpleName());
        header.put("saga_type", saga.getClass().getSimpleName());
        header.put("reply_topic", saga.getClass().getSimpleName().concat("-reply"));
        header.put("saga_id", sagaId);
        return header;
    }

    public Map<String, String> sendMessage(Saga saga, Command command, String topicName, String sagaId) {
        ObjectMapper objectMapper = new ObjectMapper();
        ListenableFuture<SendResult<String, String>> future;
        Data data;
        Map<String, String> header = getHeader(command, saga, sagaId);
        messageRepo.save(Message.builder()
                        .messageId(header.get("message_id"))
                .build());
        try {
            data = Data.builder()
                    .header(header)
                    .payload(objectMapper.writeValueAsString(command))
                    .build();
            String message = objectMapper.writeValueAsString(data);
            future = kafkaTemplate.send(topicName, message);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
                }

                @Override
                public void onFailure(Throwable ex) {
                    System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
                }
            });
            return header;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;

    }


}

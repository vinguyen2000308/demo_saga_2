package com.example.demo_saga_2.business_logic.kafka;

import com.example.demo_saga_2.business_logic.saga.*;
import com.example.demo_saga_2.domain.Command;
import com.example.demo_saga_2.domain.CommandWithDestination;
import com.example.demo_saga_2.domain.Data;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.transaction.Transactional;
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

    public Map<String, String> sendFirstMessage(Saga saga, CommandWithDestination command, SagaData sagaData) {
        ObjectMapper objectMapper = new ObjectMapper();
        ListenableFuture<SendResult<String, String>> future;
        Data data;
        Map<String, String> header = getHeader(command, saga, sagaData.getSagaId());
        try {
            String payload = objectMapper.writeValueAsString(command);
            sagaInstanceRepo.save(SagaInstance.builder()
                    .sagaId(sagaData.getSagaId())
                    .sagaType(saga.getClass().getSimpleName())
                    .endState(0)
                    .sagaDataJson(sagaData.toString())
                    .compensating(0)
                    .build());
            messageRepo.save(Message.builder()
                    .messageId(header.get("message_id"))
                    .header(header.toString())
                    .destination(command.getDestination())
                    .payload(payload)
                    .build());
            data = Data.builder()
                    .header(header)
                    .payload(payload)
                    .build();
            String message = objectMapper.writeValueAsString(data);
            future = kafkaTemplate.send(command.getDestination(), message);
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

    @Transactional
    public Map<String, String> sendMessage(Saga saga, CommandWithDestination command, SagaData sagaData) {
        ObjectMapper objectMapper = new ObjectMapper();
        ListenableFuture<SendResult<String, String>> future;
        Data data;
        Map<String, String> header = getHeader(command, saga, sagaData.getSagaId());
        try {
            String payload = objectMapper.writeValueAsString(command);
            sagaInstanceRepo.updateSagaData(sagaData.toString(), sagaData.getSagaId());
            messageRepo.save(Message.builder()
                    .messageId(header.get("message_id"))
                    .header(header.toString())
                    .destination(command.getDestination())
                    .payload(payload)
                    .build());
            data = Data.builder()
                    .header(header)
                    .payload(payload)
                    .build();
            String message = objectMapper.writeValueAsString(data);
            future = kafkaTemplate.send(command.getDestination(), message);
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

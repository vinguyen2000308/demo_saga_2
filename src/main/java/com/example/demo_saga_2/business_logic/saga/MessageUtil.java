package com.example.demo_saga_2.business_logic.saga;

import com.example.demo_saga_2.business_logic.domain.message.ReplyMessage;
import com.example.demo_saga_2.domain.Data;

import java.util.Map;

public class MessageUtil {

    public static boolean checkReply(ReplyMessage replyMessage) {
        return replyMessage.getCode().equals("200");

    }

    public static boolean setSuccess() {
        return true;
    }

    public static boolean setFailure() {
        return false;
    }

    public static String getReplyType(Data data) {
        Map<String, String> header = data.getHeader();
        String reply_type = header.get("type");
        return reply_type;
    }

    public static boolean checkReplyType(String replyType, Class<? extends ReplyMessage> replyMessageClass) {
        return replyType.equals(replyMessageClass.getSimpleName());
    }

    public static void saveReplyMessage(Data data, MessageRepo messageRepo) {
        Map<String, String> header = data.getHeader();
        messageRepo.save(Message.builder()
                .messageId(header.get("message_id"))
                .header(header.toString())
                .destination(header.get("reply_topic"))
                .payload(data.getPayload())
                .build());
    }

}

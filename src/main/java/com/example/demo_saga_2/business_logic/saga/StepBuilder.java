package com.example.demo_saga_2.business_logic.saga;

import com.example.demo_saga_2.business_logic.domain.message.ReplyMessage;
import com.example.demo_saga_2.domain.Command;

public interface StepBuilder {

    void execute(Command command);

    void rollback(Command command);

    void confirm(Command command);

    void handleSuccess(ReplyMessage replyMessage);

    void handleFailure(ReplyMessage replyMessage);
}

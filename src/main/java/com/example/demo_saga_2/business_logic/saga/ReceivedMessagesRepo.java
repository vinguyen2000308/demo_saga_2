package com.example.demo_saga_2.business_logic.saga;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivedMessagesRepo extends JpaRepository<ReceivedMessages,String> {
}

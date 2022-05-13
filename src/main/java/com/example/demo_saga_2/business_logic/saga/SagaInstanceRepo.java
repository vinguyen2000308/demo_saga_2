package com.example.demo_saga_2.business_logic.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SagaInstanceRepo extends JpaRepository<SagaInstance,String> {
    @Modifying
    @Query(value = "update saga_instance s set s.saga_data_json = ?1  where saga_id = ?2 ", nativeQuery = true)
    void updateSagaData(String sagaData,String sagaId);
}

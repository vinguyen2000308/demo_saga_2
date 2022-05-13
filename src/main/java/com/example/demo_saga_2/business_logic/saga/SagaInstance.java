package com.example.demo_saga_2.business_logic.saga;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "saga_instance")
public class SagaInstance {

    @Id
    @Column(name = "saga_id")
    private String sagaId;

    @Column(name = "saga_type")
    private String sagaType;

    @Column(name = "saga_data_json")
    private String sagaDataJson;

    @Column(name = "state_name")
    private String stateName;

    @Column(name = "end_state")
    private Integer endState;

    @Column(name = "compensating")
    private Integer compensating;

}

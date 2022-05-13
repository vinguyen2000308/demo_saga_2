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
@Table(name = "message")
public class Message {

    @Id
    @Column(name = "id")
    private String messageId;

    @Column(name = "destination")
    private String destination;

    @Column(name = "headers")
    private String header;

    @Column(name = "payload")
    private String payload;

}

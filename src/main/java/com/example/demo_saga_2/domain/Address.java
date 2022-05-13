package com.example.demo_saga_2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address implements Command {
    private String address1;
    private String address2;
    private Long zipCode;

}

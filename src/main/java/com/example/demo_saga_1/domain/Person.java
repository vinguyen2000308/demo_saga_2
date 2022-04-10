package com.example.demo_saga_1.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person implements Command {
    private String name;
    private Integer age;
    private Address address;


}

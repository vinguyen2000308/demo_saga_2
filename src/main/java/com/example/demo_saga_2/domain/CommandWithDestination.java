package com.example.demo_saga_2.domain;

public interface CommandWithDestination extends Command {
    String getDestination();
}

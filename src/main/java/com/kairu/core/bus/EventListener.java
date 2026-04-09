package com.kairu.core.bus;
import  com.kairu.core.event.Event;

@FunctionalInterface
public interface EventListener<E extends Event> {
    void onEvent(E event);
}


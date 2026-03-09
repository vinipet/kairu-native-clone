package com.kairu.core.bus;
import  com.kairu.core.event.Event;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}


package com.kairu.core.bus;
import com.kairu.core.event.Event;

public interface EventBus {

    <T extends Event> void publish(T event);

    <T extends Event> void subscribe(
        Class<T> eventType,
        EventListener<T> listener
    );
}

package com.kairu.core.bus;
import com.kairu.core.event.Event;

public interface EventBus {

    <T extends Event> void publishEvent(T event);

    <T extends Event> void subscribeListener(
        Class<T> eventType,
        EventListener<T> listener
    );
}

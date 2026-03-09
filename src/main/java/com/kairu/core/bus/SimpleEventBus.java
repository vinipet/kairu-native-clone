package com.kairu.core.bus;
import com.kairu.core.event.*;
import java.util.*;

public class SimpleEventBus implements EventBus {

    private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        Class<? extends Event> eventType = event.getClass();

        List <EventListener<? extends Event>> eventListeners =
                listeners.getOrDefault(eventType, List.of());

        for (EventListener <? extends Event> listener : eventListeners) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }

    @Override
    public <T extends Event> void subscribe(
            Class<T> eventType,
            EventListener<T> listener
    ) {
        listeners
            .computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(listener);
    }
}


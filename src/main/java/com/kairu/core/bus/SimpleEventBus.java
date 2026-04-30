package com.kairu.core.bus;
import com.kairu.core.event.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class SimpleEventBus implements EventBus {

    private final Map<Class<? extends Event>, Set<EventListener<? extends Event>>> listeners = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void publishEvent(T event) {
        Class<? extends Event> eventType = event.getClass();

        Set <EventListener<? extends Event>> eventListeners =
                listeners.getOrDefault(eventType, Set.of());

        for (EventListener <? extends Event> listener : eventListeners) {
              ((EventListener<T>) listener).onEvent(event);
        }
    }

    @Override
    public <T extends Event> void subscribeListener(
            Class<T> eventType,
            EventListener<? super T> listener
    ) {
        listeners
            .computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>())
            .add(listener);
    }

    public <T extends Event> void unsubscribeListener(EventListener<? super T> listener) {
      for (Set<EventListener<? extends Event>> eventSet : listeners.values()) {
            eventSet.remove(listener);
      }  
      listeners.entrySet().removeIf(entry -> entry.getValue().isEmpty()); 

    }

    @Override
    public <T extends Event> void unsubscribeListener(EventListener<? super T> listener, Class<T>eventType) {
      Set<EventListener<? extends Event>> eventSet = listeners.get(eventType);
        
      if (eventSet != null) {
        eventSet.remove(listener);    
        if (eventSet.isEmpty()) {
          listeners.remove(eventType);
        }
      }
    }
}

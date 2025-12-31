package com.kairu.core.event;

public abstract class BaseEvent implements Event {
    protected String message;
    protected long clock;

    public BaseEvent(String message, long clock) {
        this.message = message;
        this.clock = clock; 
    }

    public String getMessage() {
      return this.message;
    }

    public long getClock() {
      return this.clock;
    }

}

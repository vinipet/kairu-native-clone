package com.kairu.core.bus;

import java.util.ArrayList;
import java.util.List;
import com.kairu.core.event.Event;

public class SimpleListener<E extends Event> implements EventListener<E>{
  List<E> EventList = new ArrayList<E>(); 

  public void onEvent(E event){
    EventList.add(event);
  }
  
  public List<E> getEvents(){
    return EventList;
  }
}

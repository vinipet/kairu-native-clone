package com.kairu.core.persistence.dto;

import java.util.UUID;
import java.util.List;


public class SessionDTO {

  private UUID id;
  private List<IntervalDTO> intervals;

  public SessionDTO(UUID id, List<IntervalDTO> intervals){
    this.id = id;
    this.intervals = intervals;
  }  

  public UUID getId() {
      return id;
  }

  public List<IntervalDTO> getIntervals() {
      return intervals;
  }

}

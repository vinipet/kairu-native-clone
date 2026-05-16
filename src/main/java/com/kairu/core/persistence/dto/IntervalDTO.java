package com.kairu.core.persistence.dto;

import java.time.Instant;

public class IntervalDTO {

  private Instant start;
  private Instant end;

  public IntervalDTO(Instant start, Instant end){
    this.start = start;
    this.end = end;
  }  

  public Instant getEnd() {
    return end;
  }

  public Instant getStart() {
      return start;
  }
}

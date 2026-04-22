package com.kairu.core.time;

import java.time.Duration;
import java.time.Instant;

public  class Interval{
  public Instant start;
  public Instant end;


  public Interval(Instant start, Instant end){
    this.start = start;
    this.end = end;

    if(Duration.between(start, end).isZero() || Duration.between(start, end).isNegative()){
      throw new IllegalArgumentException("the interval must be gratter than 0");
    }
  }

  public Duration getDuration(){
    return Duration.between(start, end);
  }
}

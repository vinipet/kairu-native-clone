package com.kairu.core.persistence.dto;

import java.util.UUID;

import com.kairu.core.session.Tag;

import java.util.List;


public class SessionDTO {

  private UUID id;
  private List<IntervalDTO> intervals;
  private String tag;

  public SessionDTO(UUID id, List<IntervalDTO> intervals, Tag tag){
    this.id = id;
    this.intervals = intervals;
    this.tag = tag.getName();
  }  

  public UUID getId() {
      return id;
  }

  public List<IntervalDTO> getIntervals() {
      return intervals;
  }

  public String getTag() {
      return tag;
  }

}

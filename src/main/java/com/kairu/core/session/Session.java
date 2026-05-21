package com.kairu.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.kairu.core.time.Interval;

public class Session {
  public final UUID sessionId;
  private final Instant startedAt;
  private final Instant endedAt;
  private final List<Interval> intervals;
  private final Tag tag;


  public Duration getTotalDuration(){
      return intervals.stream()
        .map(Interval::getDuration)
        .reduce(Duration.ZERO, Duration::plus);
  }

  public Instant getStartedAt() {
      return startedAt;
  }

  public Instant getEndedAt() {
      return endedAt;
  }

  public List<Interval> getIntervals() {
      return intervals;
  }

  public Tag getTag() {
      return tag;
  }

  public Session(UUID sessionId,
               List<Interval> intervals, Tag tag) {

      if (intervals == null || intervals.isEmpty()) {
          throw new IllegalArgumentException("Session must have at least one interval");
      }

      this.intervals = List.copyOf(intervals);
      this.sessionId = sessionId;
      this.startedAt = intervals.getFirst().start;
      this.endedAt = intervals.getLast().end;
      this.tag = tag;

  }

  @Override
  public boolean equals(Object obj){
    if(obj instanceof Session session){
      if  (session.sessionId.equals(this.sessionId)){
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode(){
    return Objects.hash(this.sessionId);
  }
}

package com.kairu.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import com.kairu.core.time.Interval;

public class Session {
  public final long sessionId;
  private final Instant startedAt;
  private final Instant endedAt;
  private final List<Interval> intervals;


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

  public Session(long sessionId,
               List<Interval> intervals) {

      if (intervals == null || intervals.isEmpty()) {
          throw new IllegalArgumentException("Session must have at least one interval");
      }

      this.intervals = List.copyOf(intervals);

      Duration total = getTotalDuration();

      if (total.compareTo(Duration.ofMinutes(5)) < 0) {
        throw new IllegalArgumentException("Session must be at least 5 minutes");
      }

      this.sessionId = sessionId;
      this.startedAt = intervals.getFirst().start;
      this.endedAt = intervals.getLast().end;

  }

  
}

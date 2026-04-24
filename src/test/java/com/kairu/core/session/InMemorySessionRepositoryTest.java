package com.kairu.core.session;

import org.junit.jupiter.api.Test;

import com.kairu.core.time.Interval;
import com.kairu.core.time.ManualClock;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class  InMemorySessionRepositoryTest{
  
  @Test
  public void CanSaveSessionInList(){
    final List<Interval> intervals = new ArrayList<>();
    ManualClock clock = new ManualClock(Instant.now());
    Instant inst1 = clock.now();
    clock.advanceSeconds(300);
    Instant inst2 = clock.now();
    Interval interval = new Interval(inst1, inst2);
    intervals.add(interval);
    Session session = new Session(1, intervals);
    InMemorySessionRepository sessionRepository = new InMemorySessionRepository();

    sessionRepository.save(session);
    
    assertEquals(1, sessionRepository.findAll().size());
  }
  
  @Test
  public void CanSaveMultipleSessionInList(){
    final List<Interval> intervals = new ArrayList<>();
    ManualClock clock = new ManualClock(Instant.now());
    Instant inst1 = clock.now();
    clock.advanceSeconds(300);
    Instant inst2 = clock.now();
    Interval interval = new Interval(inst1, inst2);
    intervals.add(interval);

    Session session = new Session(1, intervals);
    Session session2 = new Session(1, intervals);
    InMemorySessionRepository sessionRepository = new InMemorySessionRepository();

    sessionRepository.save(session);
    sessionRepository.save(session2);
    
    assertEquals(2, sessionRepository.findAll().size());
  }

  @Test
  public void CanSearchById(){
    final List<Interval> intervals = new ArrayList<>();
    ManualClock clock = new ManualClock(Instant.now());
    Instant inst1 = clock.now();
    clock.advanceSeconds(300);
    Instant inst2 = clock.now();
    Interval interval = new Interval(inst1, inst2);
    intervals.add(interval);
    InMemorySessionRepository sessionRepository = new InMemorySessionRepository();

    Session session = new Session(1,intervals);
    sessionRepository.save(session);

    assertEquals(session, sessionRepository.findById(1).get());
  }


}

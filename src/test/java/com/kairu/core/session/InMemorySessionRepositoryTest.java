package com.kairu.core.session;

import org.junit.jupiter.api.Test;

import com.kairu.core.time.Interval;
import com.kairu.core.time.ManualClock;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    UUID id = UUID.randomUUID();
    Session session = new Session(id , intervals,new Tag("teste"));
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
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Session session = new Session(id1, intervals,new Tag("teste"));
    Session session2 = new Session(id2, intervals,new Tag("teste"));
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
    UUID id1 = UUID.randomUUID();

    Session session = new Session(id1,intervals,new Tag("teste"));
    sessionRepository.save(session);

    assertEquals(session, sessionRepository.findById(id1).get());
  }


}

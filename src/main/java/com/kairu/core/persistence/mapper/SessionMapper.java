package com.kairu.core.persistence.mapper;

import com.kairu.core.session.Session;
import com.kairu.core.time.Interval;
import com.kairu.core.persistence.dto.SessionDTO;
import com.kairu.core.persistence.dto.IntervalDTO;

import java.util.ArrayList;
import java.util.List;

public class SessionMapper {

  public static SessionDTO toDTO(Session session) {
     
    List<Interval> sessionIntervals = session.getIntervals();
    List<IntervalDTO> intervals = new ArrayList<>();

    for (Interval interval : sessionIntervals) {
      IntervalDTO dto = new IntervalDTO(interval.start, interval.end);
      intervals.add(dto);
    }

    return new SessionDTO(
      session.sessionId,
      intervals
    );
  }

  public static Session toEntity(SessionDTO dto) {
    List<Interval> intervals = dto.getIntervals().stream()
        .map(i -> new Interval(i.getStart(), i.getEnd()))
        .toList();

    // Cria a entidade usando os dados do DTO
    Session session = new Session(dto.getId(), intervals);
    
    return session;
  }
}

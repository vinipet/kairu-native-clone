package com.kairu.core.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kairu.core.persistence.dto.IntervalDTO;
import com.kairu.core.persistence.dto.SessionDTO;
import com.kairu.core.session.Session;
import com.kairu.core.session.Tag;
import com.kairu.core.time.Interval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class SessionMapperTest {
    
  UUID idEsperado;
  Session session;
  Instant start;
  Instant end;
  Tag tag;

  @BeforeEach
  void setup(){
    this.idEsperado = UUID.randomUUID();
    this.start = Instant.now();
    this.end = start.plusSeconds(3600); // 1 hora depois
    this.tag = new Tag("test");
    List<Interval> intervals = new ArrayList<>();
    intervals.add(new Interval(start, end));
    this.session = new Session(idEsperado, intervals, tag);
    

  }  

  @Test
  void deveConverterSessionParaSessionDTOComSucesso() {
    
    SessionDTO dto = SessionMapper.toDTO(session);

    // 3. Validação: O DTO é o que esperávamos?
    assertEquals(idEsperado, dto.getId(), "O ID no DTO deve ser igual ao da Session");
    assertEquals(1, dto.getIntervals().size(), "O DTO deve ter exatamente 1 intervalo");
    assertEquals(start, dto.getIntervals().get(0).getStart(), "O início do intervalo deve bater");
    assertEquals(end, dto.getIntervals().get(0).getEnd(), "O fim do intervalo deve bater");
  }

  @Test
  void deveConverterDTOParaSessionComSucesso() {
    List<IntervalDTO> intervalDTOs = new ArrayList<>();
    intervalDTOs.add(new IntervalDTO(start, end));
    
    SessionDTO dto = new SessionDTO(idEsperado, intervalDTOs, tag);

    Session sessionConvertida = SessionMapper.toEntity(dto);

    assertNotNull(sessionConvertida, "A sessão convertida não deve ser nula");
    assertEquals(idEsperado, sessionConvertida.sessionId, "O ID da entidade deve ser o do DTO");
    assertEquals(1, sessionConvertida.getIntervals().size(), "A entidade deve ter 1 intervalo");
    
    Interval primeiroIntervalo = sessionConvertida.getIntervals().get(0);
    assertEquals(start, primeiroIntervalo.start, "O início do intervalo deve ser preservado");
    assertEquals(end, primeiroIntervalo.end, "O fim do intervalo deve ser preservado");
  }
}

package com.kairu.core.Bootstrap;

import com.kairu.core.Statistics.SessionStatsService;
import com.kairu.core.bus.EventBus;
import com.kairu.core.session.SessionManager;
import com.kairu.core.session.SessionRepository;
import com.kairu.core.session.TagRepository;


public class ApplicationContext{
  private EventBus bus;
  private SessionRepository sessionRepository;
  private SessionManager manager;
  private TagRepository tagRepository;
  private SessionStatsService analytics;
  

  public ApplicationContext(EventBus bus, SessionRepository repository, SessionManager manager, TagRepository tagRepository, SessionStatsService analytics){
    this.manager = manager;
    this.bus = bus;
    this.sessionRepository = repository;
    this.tagRepository = tagRepository;
    this.analytics = analytics;
  }

  public EventBus getBus() {
      return bus;
  }

  public SessionManager getManager() {
      return manager;
  }

  public SessionRepository getSessionRepository() {
      return sessionRepository;
  }

  public TagRepository getTagRepository() {
      return tagRepository;
  }

  public SessionStatsService getAnalytics() {
      return analytics;
  }
}

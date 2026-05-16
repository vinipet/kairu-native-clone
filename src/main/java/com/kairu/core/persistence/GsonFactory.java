package com.kairu.core.persistence;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kairu.core.persistence.mapper.InstantTypeAdapter;

public class GsonFactory {

  public static Gson create() {
    return new GsonBuilder()
      .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
      .create();
    }
}

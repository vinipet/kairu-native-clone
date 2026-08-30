package com.kairu.core.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PersistencePaths {

  public static String env = resolveEnv();

  private static String resolveEnv() {
    try (InputStream is = PersistencePaths.class.getClassLoader().getResourceAsStream("kairu.properties")) {
      if (is == null) {
        System.err.println("❌ FATAL ERROR: kairu.properties not found on the classpath!");
        System.exit(1);
      }
      Properties prop = new Properties();
      prop.load(is);
      String value = prop.getProperty("kairu.env");
      if (value == null || value.trim().isEmpty() || value.contains("${")) {
        System.err.println("❌ FATAL ERROR: Property 'kairu.env' is empty, missing or unresolved in kairu.properties! Build with -P dev or -P stable.");
        System.exit(1);
      }
      return value;
    } catch (IOException e) {
      System.err.println("❌ FATAL ERROR: Failed to read kairu.properties!");
      e.printStackTrace();
      System.exit(1);
    }
    throw new IllegalStateException("unreachable");
  }

  private static final Path BASE_DIRECTORY = Paths.get(
        System.getProperty("user.home"),
        ".local",
        "share",
        "kairu",
        env
  );

  public static Path sessionsFile() {
        return BASE_DIRECTORY.resolve("sessions.jsonl");
  }
    
  public static Path tagFile() {
      return BASE_DIRECTORY.resolve("tags.json");
  }

}

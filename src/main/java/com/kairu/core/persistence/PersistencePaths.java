package com.kairu.core.persistence;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistencePaths {

    private static final Path BASE_DIRECTORY = Paths.get(
        System.getProperty("user.home"),
        ".local",
        "share",
        "kairu"
    );

    public static Path sessionsFile() {
        return BASE_DIRECTORY.resolve("sessions.jsonl");
    }

}

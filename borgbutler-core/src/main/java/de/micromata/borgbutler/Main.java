package de.micromata.borgbutler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        new Main().start(args);
    }

    private void start(String[] args) {
        log.info("Hello Borgbutler...");
    }
}
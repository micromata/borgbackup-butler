package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepo;
import de.micromata.borgbutler.config.Configuration;
import de.micromata.borgbutler.config.ConfigurationHandler;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world.");
        Configuration configuration = ConfigurationHandler.getConfiguration();
        BorgRepo repo = new BorgRepo();
        repo.setName("Hetzner-cloud").setPasswordCommand("dslfds").setRepo("sdfls");
        configuration.add(repo);
        ConfigurationHandler.getInstance().write();
    }
}
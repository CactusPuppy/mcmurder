package com.github.cactuspuppy.mcmurder.game.murder;

import org.bukkit.Location;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MansionMurder extends Murder {
    private List<Location> spawnLocations = new ArrayList<>();
    private List<Location> scrapLocations = new ArrayList<>();

    public MansionMurder() {
        //TODO: Add spawn and scrap locations
    }

    @Override
    protected Location getRandomPlayerSpawn() {
        Random randomizer = new Random();
        return spawnLocations.get(randomizer.nextInt(spawnLocations.size()));
    }

    @Override
    protected Location getRandomScrapSpawn() {
        Random random = new Random();
        return scrapLocations.get(random.nextInt(scrapLocations.size()));
    }

    @Override
    protected void lobbyToGame() {
        //TODO
    }

    @Override
    protected void backToLobby() {
        //TODO
    }
}

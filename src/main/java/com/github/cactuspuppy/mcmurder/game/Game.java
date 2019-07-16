package com.github.cactuspuppy.mcmurder.game;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public abstract class Game {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static Class<? extends EventType> events;

    public void onLoad() { }
    public void onUnload() { }

    public abstract void processEvent(Event e);

    public abstract boolean isLocked();

    public interface Event {
        EventType getType();
        String[] getArgs();
    }

    public interface EventType {
        String getName();
    }
}

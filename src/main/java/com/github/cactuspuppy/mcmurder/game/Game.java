package com.github.cactuspuppy.mcmurder.game;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public abstract class Game {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static Class<? extends Enum> events;

    public void onLoad() { }
    public void onUnload() { }

    public abstract void processEvent(Event e);

    public interface Event {
        String getType();
        Map<String, Object> getArgs();
    }
}

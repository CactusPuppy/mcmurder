package com.github.cactuspuppy.mcmurder.game;

import com.github.cactuspuppy.mcmurder.utils.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public abstract class Murder extends Game {
    private GameState state = GameState.LOBBY;
    static {
        setEvents(MurderEventTypes.class);
    }

    /**
     * <p>
     * Determines the number of murderers, either by proportion or by absolute number.<br>
     * - If murdererNumber <= 0, one murderer is chosen.<br>
     * - If murdererNumber is in range (0, 1), exclusive, that proportion of players will become murderers.<br>
     * - If murdererNumber >= 1, that number of players will become murderers.<br>
     * </p>
     * <p>
     *     Note that if both murdererNumber and {@link Murder#hunterNumber} are between 0 and 1, exclusive, then they should
     *     <b>NOT</b> add up to more than 1.0.
     * </p>
     */
    @Getter @Setter
    private double murdererNumber = 0;

    /**
     * <p>
     * Determines the number of hunters, either by proportion or by absolute number.<br>
     * - If hunterNumber <= 0, one hunter is chosen.<br>
     * - If hunterNumber is in range (0, 1), exclusive, that proportion of players will become hunters.<br>
     * - If hunterNumber >= 1, that number of players will become hunters.<br>
     * </p>
     * <p>
     *     Note that if both hunterNumber and {@link Murder#murdererNumber} are between 0 and 1, exclusive, then they should
     *     <b>NOT</b> add up to more than 1.0.
     * </p>
     */
    @Getter @Setter
    private double hunterNumber = 0;

    protected abstract Location getRandomSpawnableLocation();
    protected abstract void lobbyToGame();
    protected abstract void backToLobby();

    @Override
    public void processEvent(Event e) {
        Logger.logFineMsg(this.getClass(), "Event args: " + e.getArgs().toString(),2);
        try {
            // State-insensitive events
            // Add spectator
            if (e.getType().equals("ADD_SPECTATOR")) {
                Player p = (Player) e.getArgs().get("player");
                addSpectator(p);
            } else if (e.getType().equals("GAME_RESET")) {
                //TODO
                state = GameState.LOBBY;
            }
            // State-sensitive events
            switch (state) {
                case LOBBY:
                    handleLobbyEvent(e);
                    break;
                case STARTING:
                    handleStartingEvent(e);
                    break;
                case ACTIVE:
                    handleActiveEvent(e);
                    break;
                case PAUSED:
                    handlePausedEvent(e);
                    break;
                case RESETTING:
                    handleResettingEvent(e);
                    break;
                default:
                    Logger.logSevere(this.getClass(), String.format("Unknown event %s passed to game", e.getType()));
                    break;
            }
        } catch (Exception ex) {
            Logger.logSevere(this.getClass(), "Exception while handling event " + e.getType(), ex);
        }
    }

    private void handlePausedEvent(Event e) {
        assert state == GameState.PAUSED;
        switch (e.getType()) {
            case "RESUME":
                //TODO
                state = GameState.ACTIVE;
                break;
        }
    }

    private void handleLobbyEvent(Event e) {
        assert state == GameState.LOBBY;
        switch (e.getType()) {
            case "ADD_PLAYER":
                Player p = (Player) e.getArgs().get("player");
                addPlayer(p);
                break;
            case "GAME_START":
                //TODO
                state = GameState.STARTING;
                break;
            case "PAUSE":
                //TODO
                break;
        }
    }

    private void handleStartingEvent(Event e) {
        assert state == GameState.STARTING;
        switch (e.getType()) {
            case "ACTIVATE":
                //TODO:
                // Assign roles
                state = GameState.ACTIVE;
                break;
        }
    }

    private void handleActiveEvent(Event e) {
        assert state == GameState.ACTIVE;
        //TODO
        switch (e.getType()) {
            case "DECLARE_INNOCENT_VICTORY":
                //TODO
                state = GameState.RESETTING;
                break;
            case "DECLARE_MURDERER_VICTORY":
                //TODO
                state = GameState.RESETTING;
                break;
            case "PAUSE":
                //TODO
                break;
        }
    }

    private void handleResettingEvent(Event e) {
        assert state == GameState.RESETTING;
        //TODO
        switch (e.getType()) {
            case "RETURN_TO_LOBBY":
                //TODO
                backToLobby();
                break;
        }
    }

    private void addPlayer(Player p) {
        if (p == null) {
            return;
        }
        //TODO
    }

    private void addSpectator(Player p) {
        if (p == null) {
            return;
        }
        //TODO
    }

    private void assignRoles() {
        //TODO
    }

    public enum MurderEventTypes {
        /**
         * Hard reset to lobby, cancelling all tasks and completely resetting the game, including any round-persistent data.
         */
        GAME_RESET,
        /**
         * Moves the lobby into the game, closing the window for players to join.
         */
        GAME_START,
        /**
         * Starts the game proper, assigning roles and making death permanent.
         */
        ACTIVATE,
        DECLARE_INNOCENT_VICTORY,
        DECLARE_MURDERER_VICTORY,
        PAUSE,
        RESUME,
        /**
         * Differs from {@link MurderEventTypes#GAME_RESET} in that round-persistent data will not be wiped.
         */
        RETURN_TO_LOBBY,
        ADD_PLAYER,
        ADD_SPECTATOR
    }

    private enum GameState {
        LOBBY,
        STARTING,
        ACTIVE,
        PAUSED,
        RESETTING
    }
}

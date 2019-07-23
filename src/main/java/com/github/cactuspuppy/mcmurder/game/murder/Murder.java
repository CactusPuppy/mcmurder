package com.github.cactuspuppy.mcmurder.game.murder;

import com.github.cactuspuppy.mcmurder.game.Game;
import com.github.cactuspuppy.mcmurder.utils.Logger;
import com.github.cactuspuppy.mcmurder.utils.PlayerUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public abstract class Murder extends Game implements Listener {
    private GameState state = GameState.LOBBY;
    static {
        setEvents(MurderEventTypes.class);
    }

    /**
     * <p>
     * Determines the number of murderers, either by proportion or by absolute number.<br>
     * - If murdererNumber <= 0, one murderer is chosen.<br>
     * - If murdererNumber is in range (0, 1), exclusive, that proportion of players will become murderers, rounded down.
     * At least one person will become a murderer.<br>
     * - If murdererNumber >= 1, that number of players will become murderers.<br>
     * </p>
     * <p>
     *     Note that if both murdererNumber and {@link Murder#hunterNumber} are between 0 and 1, exclusive, then they should
     *     <b>NOT</b> add up to more than 1.0.
     * </p>
     */
    @Getter @Setter
    private double murdererNumber = 1;

    /**
     * <p>
     * Determines the number of hunters, either by proportion or by absolute number.<br>
     * - If hunterNumber <= 0, one hunter is chosen.<br>
     * - If hunterNumber is in range (0, 1), exclusive, that proportion of players will become hunters, rounded down.
     * At least one person will become a hunter.<br>
     * - If hunterNumber >= 1, that number of players will become hunters.<br>
     * </p>
     * <p>
     *     Note that if both hunterNumber and {@link Murder#murdererNumber} are between 0 and 1, exclusive, then they should
     *     <b>NOT</b> add up to more than 1.0.
     * </p>
     */
    @Getter @Setter
    private double hunterNumber = 1;

    protected Set<UUID> players = new HashSet<>();
    protected Set<UUID> spectators = new HashSet<>();

    protected Scoreboard gameScoreboard;
    protected Team murderers;
    protected Team innocents;
    protected Set<UUID> hunters = new HashSet<>();

    protected abstract Location getRandomPlayerSpawn();
    protected abstract Location getRandomScrapSpawn();
    protected abstract void lobbyToGame();
    protected abstract void backToLobby();

    @Override
    public void processEvent(Event e) {
        Logger.logFineMsg(this.getClass(), "Event args: " + Arrays.toString(e.getArgs()),2);
        try {
            // State-insensitive events
            // Add spectator
            if (e.getType().getName().equals("ADD_SPECTATOR")) {
                UUID p = PlayerUtils.getOnlinePlayer(e.getArgs()[0]);
                addSpectator(p);
            } else if (e.getType().getName().equals("GAME_RESET")) {
                //TODO
                backToLobby();
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
                    Logger.logSevere(this.getClass(), String.format("Invalid game state %s ", state.toString()));
                    break;
            }
        } catch (Exception ex) {
            Logger.logSevere(this.getClass(), "Exception while handling event " + e.getType(), ex);
        }
    }

    @Override
    public void onLoad() {
        gameScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        murderers = gameScoreboard.registerNewTeam("Murderers");
        murderers.setAllowFriendlyFire(true);
        murderers.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        murderers.setColor(ChatColor.RED);

        innocents = gameScoreboard.registerNewTeam("Innocents");
        innocents.setAllowFriendlyFire(true);
        innocents.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        innocents.setColor(ChatColor.BLUE);

        backToLobby();
    }

    @Override
    public boolean isLocked() {
        return state == GameState.LOBBY;
    }

    private void handlePausedEvent(Event e) {
        assert state == GameState.PAUSED;
        switch (e.getType().getName()) {
            case "RESUME":
                //TODO
                state = GameState.ACTIVE;
                break;
        }
    }

    private void handleLobbyEvent(Event e) {
        assert state == GameState.LOBBY;
        switch (e.getType().getName()) {
            case "ADD_PLAYER":
                UUID p = PlayerUtils.getOnlinePlayer(e.getArgs()[0]);
                addPlayer(p);
                break;
            case "GAME_START":
                //TODO
                lobbyToGame();
                state = GameState.STARTING;
                break;
            case "PAUSE":
                //TODO
                break;
        }
    }

    private void handleStartingEvent(Event e) {
        assert state == GameState.STARTING;
        switch (e.getType().getName()) {
            case "ACTIVATE":
                assignRoles();
                state = GameState.ACTIVE;
                break;
        }
    }

    private void handleActiveEvent(Event e) {
        assert state == GameState.ACTIVE;
        //TODO
        switch (e.getType().getName()) {
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
        switch (e.getType().getName()) {
            case "RETURN_TO_LOBBY":
                //TODO
                backToLobby();
                break;
        }
    }

    private void addPlayer(UUID p) {
        if (p == null) {
            return;
        }
        spectators.remove(p);
        players.add(p);
    }

    private void addSpectator(UUID p) {
        if (p == null) {
            return;
        }
        players.remove(p);
        spectators.add(p);
    }

    private void assignRoles() throws InvalidConfigurationException {
        //TODO
    }



    public enum MurderEventTypes implements EventType {
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
        ADD_SPECTATOR;


        @Override
        public String getName() {
            return this.name();
        }
    }

    private enum GameState {
        LOBBY,
        STARTING,
        ACTIVE,
        PAUSED,
        RESETTING
    }
}

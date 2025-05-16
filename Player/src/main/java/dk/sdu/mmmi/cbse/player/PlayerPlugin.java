package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for player system.
 */
public class PlayerPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(PlayerPlugin.class.getName());
    private Entity player;
    private final PlayerFactory playerFactory;

    /**
     * Create a new player plugin
     */
    public PlayerPlugin() {
        this.playerFactory = new PlayerFactory();

        LOGGER.log(Level.INFO, "PlayerPlugin initialized");
    }

    @Override
    public void start(GameData gameData, World world) {
        player = playerFactory.createPlayer(gameData);
        world.addEntity(player);

        LOGGER.log(Level.INFO, "Player added to world: {0}", player.getID());
    }

    @Override
    public void stop(GameData gameData, World world) {
        world.removeEntity(player);

        LOGGER.log(Level.INFO, "Player removed from world: {0}", player.getID());
    }
}
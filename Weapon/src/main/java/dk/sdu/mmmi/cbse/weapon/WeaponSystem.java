package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that processes weapons and handles shooting events.
 */
public class WeaponSystem implements IProcessingService {
    private static final Logger LOGGER = Logger.getLogger(WeaponSystem.class.getName());

    private final WeaponRegistry weaponRegistry;

    private World world;
    private GameData gameData;

    /**
     * Create a new weapon system
     */
    public WeaponSystem() {
        this.weaponRegistry = WeaponRegistry.getInstance();

        LOGGER.log(Level.INFO, "WeaponSystem initialized");
    }

    @Override
    public void process(GameData gameData, World world) {
        // ToDo
    }
}
package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for bullet functionality.
 */
public class BulletService implements IBulletSPI {
    private static final Logger LOGGER = Logger.getLogger(BulletService.class.getName());

    private final BulletFactory bulletFactory;

    /**
     * Create a new bullet service
     */
    public BulletService() {
        this.bulletFactory = new BulletFactory();
        LOGGER.log(Level.INFO, "BulletService initialized");
    }

    @Override
    public Entity createBullet(Entity shooter, GameData gameData, String bulletType) {
        LOGGER.log(Level.FINE, "Creating bullet of type {0} from shooter {1}",
                new Object[]{bulletType, shooter.getID()});

        return bulletFactory.createBullet(shooter, gameData, bulletType);
    }
}
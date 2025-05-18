package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for weapon functionality.
 */
public class WeaponService implements IWeaponSPI {
    private static final Logger LOGGER = Logger.getLogger(WeaponService.class.getName());

    private IBulletSPI bulletSPI;

    public WeaponService() {
        this.bulletSPI = ServiceLoader.load(IBulletSPI.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "WeaponService initialized with BulletSPI: {0}",
                bulletSPI != null ? bulletSPI.getClass().getName() : "not available");
    }

    @Override
    public void shoot(Entity shooter, GameData gameData, String bulletType) {
        if (bulletSPI == null) {
            bulletSPI = ServiceLoader.load(IBulletSPI.class).findFirst().orElse(null);
            if (bulletSPI == null) {
                LOGGER.log(Level.WARNING, "Cannot create bullet: BulletSPI not available");
                return;
            }
        }

        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        if (weapon == null || !weapon.canFire()) {
            return;
        }

        weapon.resetCooldown();

        switch (weapon.getFiringPattern()) {
            case SHOTGUN:
                for (int i = 0; i < weapon.getShotCount(); i++) {
                    LOGGER.log(Level.FINE, "Creating shotgun bullet {0} of {1}", new Object[]{i + 1, weapon.getShotCount()});
                    bulletSPI.createBullet(shooter, gameData, bulletType);
                }
                break;

            case BURST:
                bulletSPI.createBullet(shooter, gameData, bulletType);
                weapon.incrementBurstCount();
                if (weapon.isBurstComplete()) {
                    weapon.resetBurst();
                } else {
                    weapon.startBurstDelay();
                }
                break;
            default:
                LOGGER.log(Level.FINE, "Creating single bullet of type: {0}", bulletType);
                bulletSPI.createBullet(shooter, gameData, bulletType);
                break;
        }
    }

    @Override
    public Weapon getWeapon() {
        // ToDo: Implement this method to retrieve the desired Weapon from WeaponRegistry.
        return null;
    }
}
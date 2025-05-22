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
    public Entity shoot(Entity shooter, GameData gameData, String bulletType) {
        if (bulletSPI == null) {
            bulletSPI = ServiceLoader.load(IBulletSPI.class).findFirst().orElse(null);
            if (bulletSPI == null) {
                LOGGER.log(Level.WARNING, "Cannot create bullet: BulletSPI not available");
                return null;
            }
        }

        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        if (weapon == null || !weapon.canFire()) {
            return null;
        }

        weapon.resetCooldown();

        Entity bullet = null;
        switch (weapon.getFiringPattern()) {
            case SHOTGUN:
                // For shotguns, we'll return the last bullet (could be enhanced to return a list)
                for (int i = 0; i < weapon.getShotCount(); i++) {
                    bullet = bulletSPI.createBullet(shooter, gameData, bulletType);
                }
                break;
            case BURST:
                bullet = bulletSPI.createBullet(shooter, gameData, bulletType);
                // Existing burst logic...
                break;
            default:
                bullet = bulletSPI.createBullet(shooter, gameData, bulletType);
                break;
        }

        return bullet;
    }

    @Override
    public Weapon getWeapon(String weaponName) {
        return WeaponRegistry.getInstance().getWeapon(weaponName);
    }

    @Override
    public Weapon getWeapon() {
        return WeaponRegistry.getInstance().getWeapon("automatic");
    }
}
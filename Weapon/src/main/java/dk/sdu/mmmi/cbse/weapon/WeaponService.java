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
 * Service implementation for weapon functionality with proper firing pattern support.
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

        Entity lastBullet = null;

        switch (weapon.getFiringPattern()) {
            case SHOTGUN:
                lastBullet = handleShotgunFiring(shooter, gameData, bulletType, weapon);
                break;

            case BURST:
                lastBullet = handleBurstFiring(shooter, gameData, bulletType, weapon);
                break;

            case HEAVY:
            case AUTOMATIC:
            default:
                // Single bullet
                weapon.resetCooldown();
                lastBullet = bulletSPI.createBullet(shooter, gameData, bulletType);
                break;
        }

        return lastBullet;
    }

    /**
     * Handle shotgun firing with proper spread
     */
    private Entity handleShotgunFiring(Entity shooter, GameData gameData, String bulletType, WeaponComponent weapon) {
        weapon.resetCooldown();

        Entity lastBullet = null;
        int shotCount = weapon.getShotCount();
        float spreadAngle = weapon.getSpreadAngle();

        // Create bullets with even spread distribution
        for (int i = 0; i < shotCount; i++) {
            // Calculate spread angle for this pellet
            float angleOffset = 0;
            if (shotCount > 1) {
                float halfSpread = spreadAngle / 2.0f;
                float angleStep = spreadAngle / (shotCount - 1);
                angleOffset = -halfSpread + (angleStep * i);
            }

            // Create bullet with specific spread angle
            lastBullet = createBulletWithSpread(shooter, gameData, bulletType, angleOffset);
        }

        LOGGER.log(Level.FINE, "Fired shotgun: {0} pellets with {1}° spread",
                new Object[]{shotCount, spreadAngle});

        return lastBullet;
    }

    /**
     * Handle burst firing with proper burst mechanics
     */
    private Entity handleBurstFiring(Entity shooter, GameData gameData, String bulletType, WeaponComponent weapon) {
        // Check if we're in the middle of a burst
        if (weapon.getCurrentBurstCount() == 0) {
            // Starting new burst
            weapon.resetCooldown();
            weapon.resetBurst();
        }

        // Fire one bullet in the burst
        Entity bullet = bulletSPI.createBullet(shooter, gameData, bulletType);
        weapon.incrementBurstCount();

        if (weapon.isBurstComplete()) {
            // Burst finished, reset for next burst
            weapon.resetBurst();
            LOGGER.log(Level.FINE, "Completed burst of {0} bullets", weapon.getBurstCount());
        } else {
            // More bullets in burst, set burst delay
            weapon.startBurstDelay();
            LOGGER.log(Level.FINE, "Burst progress: {0}/{1}",
                    new Object[]{weapon.getCurrentBurstCount(), weapon.getBurstCount()});
        }

        return bullet;
    }

    /**
     * Create bullet with specific spread angle for shotgun
     */
    private Entity createBulletWithSpread(Entity shooter, GameData gameData, String bulletType, float spreadAngle) {
        // Store original rotation
        dk.sdu.mmmi.cbse.common.components.TransformComponent transform =
                shooter.getComponent(dk.sdu.mmmi.cbse.common.components.TransformComponent.class);

        if (transform != null) {
            float originalRotation = transform.getRotation();

            // Temporarily adjust rotation for spread
            transform.setRotation(originalRotation + spreadAngle);

            // Create bullet
            Entity bullet = bulletSPI.createBullet(shooter, gameData, bulletType);

            // Restore original rotation
            transform.setRotation(originalRotation);

            return bullet;
        }

        // Fallback if no transform
        return bulletSPI.createBullet(shooter, gameData, bulletType);
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
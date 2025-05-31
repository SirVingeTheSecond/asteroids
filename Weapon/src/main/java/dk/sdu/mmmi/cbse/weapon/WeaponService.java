package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public List<Entity> shoot(Entity shooter, GameData gameData, String bulletType) {
        if (bulletSPI == null) {
            bulletSPI = ServiceLoader.load(IBulletSPI.class).findFirst().orElse(null);
            if (bulletSPI == null) {
                LOGGER.log(Level.WARNING, "Cannot create bullet: BulletSPI not available");
                return Collections.emptyList();
            }
        }

        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        if (weapon == null || !weapon.canFire()) {
            return Collections.emptyList();
        }

        switch (weapon.getFiringPattern()) {
            case SHOTGUN:
                return handleShotgunFiring(shooter, gameData, bulletType, weapon);
            case BURST:
                return handleBurstFiring(shooter, gameData, bulletType, weapon);
            case HEAVY:
            case AUTOMATIC:
            default:
                return handleSingleFiring(shooter, gameData, bulletType, weapon);
        }
    }

    /**
     * Handle single bullet firing (automatic/heavy)
     */
    private List<Entity> handleSingleFiring(Entity shooter, GameData gameData, String bulletType, WeaponComponent weapon) {
        weapon.resetCooldown();
        Entity bullet = bulletSPI.createBullet(shooter, gameData, bulletType);

        return bullet != null ? List.of(bullet) : Collections.emptyList();
    }

    /**
     * Handle shotgun firing with proper spread
     */
    private List<Entity> handleShotgunFiring(Entity shooter, GameData gameData, String bulletType, WeaponComponent weapon) {
        weapon.resetCooldown();

        List<Entity> bullets = new ArrayList<>();
        int shotCount = weapon.getShotCount();
        float spreadAngle = weapon.getSpreadAngle();

        for (int i = 0; i < shotCount; i++) {
            float angleOffset = 0;
            if (shotCount > 1) {
                float halfSpread = spreadAngle / 2.0f;
                float angleStep = spreadAngle / (shotCount - 1);
                angleOffset = -halfSpread + (angleStep * i);
            }

            Entity bullet = createBulletWithSpread(shooter, gameData, bulletType, angleOffset);
            if (bullet != null) {
                bullets.add(bullet);
            }
        }

        LOGGER.log(Level.FINE, "Fired shotgun: {0} pellets with {1}° spread",
                new Object[]{bullets.size(), spreadAngle});

        return bullets;
    }

    /**
     * Handle burst firing
     */
    private List<Entity> handleBurstFiring(Entity shooter, GameData gameData, String bulletType, WeaponComponent weapon) {
        // Fire one bullet in the burst
        Entity bullet = bulletSPI.createBullet(shooter, gameData, bulletType);
        weapon.fireBurstShot();

        if (weapon.isBurstComplete()) {
            LOGGER.log(Level.FINE, "Completed burst of {0} bullets", weapon.getBurstCount());
        } else {
            LOGGER.log(Level.FINE, "Burst progress: {0}/{1}",
                    new Object[]{weapon.getCurrentBurstCount(), weapon.getBurstCount()});
        }

        return bullet != null ? List.of(bullet) : Collections.emptyList();
    }

    /**
     * Create bullet with specific spread angle for shotgun
     */
    private Entity createBulletWithSpread(Entity shooter, GameData gameData, String bulletType, float spreadAngle) {
        TransformComponent transform = shooter.getComponent(TransformComponent.class);

        if (transform != null) {
            float originalRotation = transform.getRotation();
            transform.setRotation(originalRotation + spreadAngle);
            Entity bullet = bulletSPI.createBullet(shooter, gameData, bulletType);
            transform.setRotation(originalRotation);
            return bullet;
        }

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
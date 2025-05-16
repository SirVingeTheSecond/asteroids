package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.ServiceLocator;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.commonweapon.WeaponType;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for handling weapon firing logic.
 * Implements IWeaponSPI to manage weapon behavior and bullet creation.
 */
public class WeaponFactory implements IWeaponSPI {
    private static final Logger LOGGER = Logger.getLogger(WeaponFactory.class.getName());
    private final IBulletSPI bulletSPI;
    private final WeaponRegistry weaponRegistry;

    /**
     * Create a new weapon factory
     */
    public WeaponFactory() {
        this.bulletSPI = ServiceLocator.getService(IBulletSPI.class);
        this.weaponRegistry = WeaponRegistry.getInstance();

        LOGGER.log(Level.INFO, "WeaponFactory initialized with bullet SPI: {0}", bulletSPI.getClass().getName());
    }

    @Override
    public Entity createBullet(Entity shooter, GameData gameData, String bulletType) {
        return bulletSPI.createBullet(shooter, gameData, bulletType);
    }

    @Override
    public String[] getAvailableWeaponTypes() {
        return weaponRegistry.getAvailableWeaponTypes().toArray(new String[0]);
    }

    @Override
    public void processFiring(Entity entity, GameData gameData, World world) {
        WeaponComponent weaponComponent = entity.getComponent(WeaponComponent.class);
        if (weaponComponent == null) {
            return; // No weapon component
        }

        // Update cooldown
        weaponComponent.updateCooldown();

        // Process firing based on weapon pattern
        if (weaponComponent.isFiring()) {
            switch (weaponComponent.getFiringPattern()) {
                case AUTOMATIC:
                    processAutomaticFiring(entity, weaponComponent, gameData, world);
                    break;
                case BURST:
                    processBurstFiring(entity, weaponComponent, gameData, world);
                    break;
                case HEAVY:
                    processHeavyFiring(entity, weaponComponent, gameData, world);
                    break;
                case SHOTGUN:
                    processShotgunFiring(entity, weaponComponent, gameData, world);
                    break;
            }
        } else {
            // Reset burst if not firing
            if (weaponComponent.getFiringPattern() == WeaponType.FiringPattern.BURST) {
                weaponComponent.resetBurst();
            }
        }
    }

    /**
     * Process automatic weapon firing
     */
    private void processAutomaticFiring(Entity entity, WeaponComponent weaponComponent,
                                        GameData gameData, World world) {
        if (weaponComponent.canFire()) {
            Entity bullet = createBullet(entity, gameData, weaponComponent.getBulletType());

            if (bullet != null) {
                world.addEntity(bullet);
                weaponComponent.resetCooldown();
            }
        }
    }

    /**
     * Process burst weapon firing
     */
    private void processBurstFiring(Entity entity, WeaponComponent weaponComponent,
                                    GameData gameData, World world) {
        // If not currently in a burst sequence, start one
        if (weaponComponent.getCurrentBurstCount() == 0 && weaponComponent.canFire()) {
            // Fire first bullet
            Entity bullet = createBullet(entity, gameData, weaponComponent.getBulletType());

            if (bullet != null) {
                world.addEntity(bullet);
                weaponComponent.incrementBurstCount();
                weaponComponent.startBurstDelay();

                // Only reset main cooldown when burst completes
                if (weaponComponent.isBurstComplete()) {
                    weaponComponent.resetCooldown();
                    weaponComponent.resetBurst();
                }
            }
        }
        // If in burst and delay expired, continue burst
        else if (weaponComponent.getCurrentBurstCount() > 0 &&
                !weaponComponent.isBurstComplete() &&
                weaponComponent.isBurstDelayComplete()) {

            // Fire next bullet in burst
            Entity bullet = createBullet(entity, gameData, weaponComponent.getBulletType());

            if (bullet != null) {
                world.addEntity(bullet);
                weaponComponent.incrementBurstCount();

                if (weaponComponent.isBurstComplete()) {
                    // Burst complete, reset cooldown and burst
                    weaponComponent.resetCooldown();
                    weaponComponent.resetBurst();
                } else {
                    // Not complete, start delay for next bullet
                    weaponComponent.startBurstDelay();
                }
            }
        }
    }

    /**
     * Process heavy weapon firing
     */
    private void processHeavyFiring(Entity entity, WeaponComponent weaponComponent,
                                    GameData gameData, World world) {
        // Slow, powerful shots
        if (weaponComponent.canFire()) {
            // Create heavy bullet
            Entity bullet = createBullet(entity, gameData, "heavy");

            if (bullet != null) {
                world.addEntity(bullet);
                weaponComponent.resetCooldown();
            }
        }
    }

    /**
     * Process shotgun weapon firing
     */
    private void processShotgunFiring(Entity entity, WeaponComponent weaponComponent,
                                      GameData gameData, World world) {
        // Multiple bullets in spread pattern
        if (weaponComponent.canFire()) {
            int shotCount = weaponComponent.getShotCount();
            float spreadAngle = weaponComponent.getSpreadAngle();
            float angleStep = spreadAngle / (shotCount - 1);
            float startAngle = -spreadAngle / 2;

            // Store original rotation
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            float originalRotation = transform.getRotation();

            // Create bullets in spread pattern
            for (int i = 0; i < shotCount; i++) {
                // Calculate spread angle for this bullet
                float bulletAngle = startAngle + (angleStep * i);

                // Temporarily set shooter rotation for bullet creation
                transform.setRotation(originalRotation + bulletAngle);

                // Create bullet
                Entity bullet = createBullet(entity, gameData, weaponComponent.getBulletType());

                if (bullet != null) {
                    world.addEntity(bullet);
                }
            }

            // Restore original rotation
            transform.setRotation(originalRotation);

            // Reset cooldown
            weaponComponent.resetCooldown();
        }
    }
}
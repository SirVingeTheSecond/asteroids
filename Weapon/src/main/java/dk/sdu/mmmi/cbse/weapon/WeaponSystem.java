package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that handles weapons
 */
public class WeaponSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(WeaponSystem.class.getName());

    private IWeaponSPI weaponSPI;

    public WeaponSystem() {
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "WeaponSystem initialized");
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void update(GameData gameData, World world) {
        float deltaTime = Time.getDeltaTimeF();

        for (Entity entity : world.getEntities()) {
            WeaponComponent weapon = entity.getComponent(WeaponComponent.class);
            if (weapon == null) {
                continue;
            }

            weapon.updateCooldown(deltaTime);

            if (weapon.getFiringPattern() == Weapon.FiringPattern.BURST) {
                handleAutomaticBurstContinuation(entity, weapon, gameData, world);
            }
        }
    }

    /**
     * Handle automatic continuation of burst firing
     */
    private void handleAutomaticBurstContinuation(Entity shooter, WeaponComponent weapon,
                                                  GameData gameData, World world) {
        // Continue burst automatically if:
        // 1. Burst is in progress
        // 2. Burst delay has elapsed
        // 3. Burst is not complete
        // 4. Weapon can fire
        if (weapon.isBurstInProgress() &&
                weapon.isBurstDelayComplete() &&
                !weapon.isBurstComplete() &&
                weapon.canFire()) {

            if (weaponSPI == null) {
                weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
            }

            if (weaponSPI != null) {
                List<Entity> bullets = weaponSPI.shoot(shooter, gameData, weapon.getBulletType());

                for (Entity bullet : bullets) {
                    world.addEntity(bullet);
                }

                LOGGER.log(Level.FINE, "Auto-continued burst for entity {0}, created {1} bullets",
                        new Object[]{shooter.getID(), bullets.size()});
            }
        }
    }
}
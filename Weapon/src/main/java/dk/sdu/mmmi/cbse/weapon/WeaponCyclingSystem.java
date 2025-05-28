package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.input.InputController;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for handling player weapon cycling.
 */
public class WeaponCyclingSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(WeaponCyclingSystem.class.getName());

    private final WeaponRegistry weaponRegistry;
    private final List<String> availableWeapons;
    private int currentWeaponIndex = 0;
    private boolean wasCyclePressed = false;

    public WeaponCyclingSystem() {
        this.weaponRegistry = WeaponRegistry.getInstance();
        this.availableWeapons = List.copyOf(weaponRegistry.getAvailableWeapons());

        LOGGER.log(Level.INFO, "WeaponCyclingSystem initialized with {0} available weapons: {1}",
                new Object[]{availableWeapons.size(), availableWeapons});
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public void update(GameData gameData, World world) {
        // Check for cycle key press
        boolean isCyclePressed = InputController.isButtonPressed(Button.Q);
        boolean cycleJustPressed = isCyclePressed && !wasCyclePressed;
        wasCyclePressed = isCyclePressed;

        if (cycleJustPressed) {
            cyclePlayerWeapon(world);
        }
    }

    /**
     * Cycle to the next weapon
     */
    private void cyclePlayerWeapon(World world) {
        Entity player = findPlayer(world);
        if (player == null) {
            LOGGER.log(Level.FINE, "No player found for weapon cycling");
            return;
        }

        WeaponComponent weaponComponent = player.getComponent(WeaponComponent.class);
        if (weaponComponent == null) {
            LOGGER.log(Level.WARNING, "Player has no WeaponComponent for cycling");
            return;
        }

        // Cycle to next weapon
        currentWeaponIndex = (currentWeaponIndex + 1) % availableWeapons.size();
        String newWeaponName = availableWeapons.get(currentWeaponIndex);
        Weapon newWeapon = weaponRegistry.getWeapon(newWeaponName);

        if (newWeapon == null) {
            LOGGER.log(Level.WARNING, "Failed to get weapon configuration for: {0}", newWeaponName);
            return;
        }

        weaponComponent.configureFromType(newWeapon);

        // just logging
        String weaponInfo = String.format("%s: %s damage, %s speed, %s pattern",
                newWeaponName.toUpperCase(),
                newWeapon.getDamage() == 2.0f ? "HIGH" : "STANDARD",
                newWeapon.getProjectileSpeed() >= 400 ? "FAST" :
                        newWeapon.getProjectileSpeed() >= 350 ? "MEDIUM" : "SLOW",
                newWeapon.getFiringPattern());

        LOGGER.log(Level.INFO, "Player switched to: {0}", weaponInfo);
    }

    /**
     * Find the player entity in the world
     */
    private Entity findPlayer(World world) {
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                return entity;
            }
        }
        return null;
    }
}
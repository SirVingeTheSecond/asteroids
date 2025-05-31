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
    private boolean initialWeaponDetected = false;

    public WeaponCyclingSystem() {
        this.weaponRegistry = WeaponRegistry.getInstance();
        this.availableWeapons = List.copyOf(weaponRegistry.getAvailableWeapons());

        // Set initial weapon index to "automatic" if it exists
        int automaticIndex = availableWeapons.indexOf("automatic");
        if (automaticIndex != -1) {
            this.currentWeaponIndex = automaticIndex;
            LOGGER.log(Level.INFO, "WeaponCyclingSystem initialized with automatic weapon at index {0}", automaticIndex);
        } else {
            LOGGER.log(Level.WARNING, "Automatic weapon not found in registry, starting at index 0");
        }

        LOGGER.log(Level.INFO, "WeaponCyclingSystem initialized with {0} available weapons: {1}",
                new Object[]{availableWeapons.size(), availableWeapons});
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public void update(GameData gameData, World world) {
        // Detect initial weapon if not done yet
        if (!initialWeaponDetected) {
            detectInitialWeapon(world);
        }

        // Check for cycle key press
        boolean isCyclePressed = InputController.isButtonPressed(Button.Q);
        boolean cycleJustPressed = isCyclePressed && !wasCyclePressed;
        wasCyclePressed = isCyclePressed;

        if (cycleJustPressed) {
            cyclePlayerWeapon(world);
        }
    }

    /**
     * Detect what weapon the player currently has and sync the index
     */
    private void detectInitialWeapon(World world) {
        Entity player = findPlayer(world);
        if (player == null) {
            return; // No player found yet
        }

        WeaponComponent weaponComponent = player.getComponent(WeaponComponent.class);
        if (weaponComponent == null) {
            return; // No weapon component yet
        }

        // Try to match the current weapon configuration to our registry
        String detectedWeapon = detectWeaponType(weaponComponent);
        if (detectedWeapon != null) {
            int detectedIndex = availableWeapons.indexOf(detectedWeapon);
            if (detectedIndex != -1) {
                currentWeaponIndex = detectedIndex;
                LOGGER.log(Level.INFO, "Detected initial weapon: {0} at index {1}",
                        new Object[]{detectedWeapon, detectedIndex});
            }
        }

        initialWeaponDetected = true;
    }

    /**
     * Try to detect what weapon type the player currently has
     */
    private String detectWeaponType(WeaponComponent weaponComponent) {
        Weapon.FiringPattern pattern = weaponComponent.getFiringPattern();
        float damage = weaponComponent.getDamage();
        float speed = weaponComponent.getProjectileSpeed();
        float cooldown = weaponComponent.getCooldownTime();

        // Match against known weapon configurations
        for (String weaponName : availableWeapons) {
            Weapon weapon = weaponRegistry.getWeapon(weaponName);
            if (weapon != null && weaponMatches(weapon, pattern, damage, speed, cooldown)) {
                return weaponName;
            }
        }

        // Default to automatic if we can't detect
        LOGGER.log(Level.WARNING, "Could not detect initial weapon type, defaulting to automatic");
        return "automatic";
    }

    /**
     * Check if a weapon configuration matches the current weapon component
     */
    private boolean weaponMatches(Weapon weapon, Weapon.FiringPattern pattern,
                                  float damage, float speed, float cooldown) {
        return weapon.getFiringPattern() == pattern &&
                Math.abs(weapon.getDamage() - damage) < 0.1f &&
                Math.abs(weapon.getProjectileSpeed() - speed) < 10.0f &&
                Math.abs(weapon.getCooldownTime() - cooldown) < 0.05f;
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
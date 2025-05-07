package dk.sdu.mmmi.cbse.common.data;

/**
 * Enumeration of all entity types in the game.
 * Used for type-safe entity categorization.
 */
public enum EntityType {
    PLAYER,    // Player ship
    ENEMY,     // Enemy ships
    ASTEROID,  // Asteroid objects
    BULLET,    // Projectiles
    POWERUP;   // Collectible power-ups
}
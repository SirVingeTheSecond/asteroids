package dk.sdu.mmmi.cbse.commonenemy;

/**
 * Defines different enemy behavior patterns
 */
public enum EnemyBehavior {
    PATROL,     // Moves in a fixed pattern
    AGGRESSIVE, // Actively seeks and engages player
    DEFENSIVE,  // Maintains distance while attacking
    SNIPER      // Stays still, shoots from distance
}
package dk.sdu.mmmi.cbse.tests.suites;

import dk.sdu.mmmi.cbse.tests.systems.PlayerSystemTest;
import dk.sdu.mmmi.cbse.tests.systems.CollisionSystemTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for system integration tests.
 */
@Suite
@SelectClasses({
        PlayerSystemTest.class,
        CollisionSystemTest.class
})
@DisplayName("System Integration Tests")
public class SystemTests {
    // System test suite
}
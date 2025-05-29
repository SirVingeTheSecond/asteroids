package dk.sdu.mmmi.cbse.tests.suites;

import dk.sdu.mmmi.cbse.tests.components.PlayerComponentTest;
import dk.sdu.mmmi.cbse.tests.components.WeaponComponentTest;
import dk.sdu.mmmi.cbse.tests.contracts.PhysicsSPIContractTest;
import dk.sdu.mmmi.cbse.tests.integration.GameplayIntegrationTest;
import dk.sdu.mmmi.cbse.tests.performance.CollisionPerformanceTest;
import dk.sdu.mmmi.cbse.tests.systems.CollisionSystemTest;
import dk.sdu.mmmi.cbse.tests.systems.PlayerSystemTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for all tests.
 */
@Suite
@SelectClasses({
        // Component Tests
        PlayerComponentTest.class,
        WeaponComponentTest.class,

        // System Tests
        PlayerSystemTest.class,
        CollisionSystemTest.class,

        // Contract Tests
        PhysicsSPIContractTest.class,

        // Integration Tests
        GameplayIntegrationTest.class,

        // Performance Tests
        CollisionPerformanceTest.class
})
@DisplayName("Complete Test Suite")
public class AllTests {

}
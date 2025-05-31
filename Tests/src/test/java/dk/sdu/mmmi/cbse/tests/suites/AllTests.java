package dk.sdu.mmmi.cbse.tests.suites;

import dk.sdu.mmmi.cbse.tests.components.PlayerComponentTest;
import dk.sdu.mmmi.cbse.tests.components.WeaponComponentTest;
import dk.sdu.mmmi.cbse.tests.contracts.PhysicsSPIContractTest;
import dk.sdu.mmmi.cbse.tests.contracts.EventServiceContractTest;
import dk.sdu.mmmi.cbse.tests.integration.GameplayIntegrationTest;
import dk.sdu.mmmi.cbse.tests.integration.SpringJPMSIntegrationTest;
import dk.sdu.mmmi.cbse.tests.integration.SystemInteractionTest;
import dk.sdu.mmmi.cbse.tests.performance.CollisionPerformanceTest;
import dk.sdu.mmmi.cbse.tests.systems.CollisionSystemTest;
import dk.sdu.mmmi.cbse.tests.systems.PlayerSystemTest;
import dk.sdu.mmmi.cbse.tests.systems.BoundarySystemTest;
import dk.sdu.mmmi.cbse.tests.systems.EnemySystemTest;
import dk.sdu.mmmi.cbse.tests.utils.FlickerUtilityTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for all game systems and components.
 */
@Suite
@SelectClasses({
        // Component Tests
        PlayerComponentTest.class,
        WeaponComponentTest.class,

        // System Tests
        PlayerSystemTest.class,
        CollisionSystemTest.class,
        BoundarySystemTest.class,
        EnemySystemTest.class,

        // Utility Tests
        FlickerUtilityTest.class,

        // Contract Tests (SPI compliance)
        PhysicsSPIContractTest.class,
        EventServiceContractTest.class,

        // Integration Tests
        GameplayIntegrationTest.class,
        SystemInteractionTest.class,
        SpringJPMSIntegrationTest.class,

        // Performance Tests
        CollisionPerformanceTest.class
})
@DisplayName("Test Suite")
public class AllTests {

}
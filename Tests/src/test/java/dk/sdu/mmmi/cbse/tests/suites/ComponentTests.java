package dk.sdu.mmmi.cbse.tests.suites;

import dk.sdu.mmmi.cbse.tests.components.PlayerComponentTest;
import dk.sdu.mmmi.cbse.tests.components.WeaponComponentTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for component unit tests.
 */
@Suite
@SelectClasses({
        PlayerComponentTest.class,
        WeaponComponentTest.class
})
@DisplayName("Component Unit Tests")
public class ComponentTests {

}
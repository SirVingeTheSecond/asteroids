package dk.sdu.mmmi.cbse.tests.utils;

import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

/**
 * Utility class for mocking ServiceLoader in CBSE tests.
 * Helps inject mock SPIs into systems under test.
 */
public class MockServiceLoader {

    /**
     * Setup mocked ServiceLoader to return the provided Physics SPI
     */
    public static void setupPhysicsSPI(MockedStatic<ServiceLoader> serviceLoaderMock,
                                       IPhysicsSPI mockPhysicsSPI) {
        @SuppressWarnings("unchecked")
        ServiceLoader<IPhysicsSPI> physicsLoader = mock(ServiceLoader.class);
        when(physicsLoader.findFirst()).thenReturn(Optional.of(mockPhysicsSPI));

        serviceLoaderMock.when(() -> ServiceLoader.load(IPhysicsSPI.class))
                .thenReturn(physicsLoader);
    }

    /**
     * Setup mocked ServiceLoader to return the provided Weapon SPI
     */
    public static void setupWeaponSPI(MockedStatic<ServiceLoader> serviceLoaderMock,
                                      IWeaponSPI mockWeaponSPI) {
        @SuppressWarnings("unchecked")
        ServiceLoader<IWeaponSPI> weaponLoader = mock(ServiceLoader.class);
        when(weaponLoader.findFirst()).thenReturn(Optional.of(mockWeaponSPI));

        serviceLoaderMock.when(() -> ServiceLoader.load(IWeaponSPI.class))
                .thenReturn(weaponLoader);
    }

    /**
     * Setup ServiceLoader to return empty (simulate missing SPI)
     */
    public static void setupMissingSPI(MockedStatic<ServiceLoader> serviceLoaderMock,
                                       Class<?> spiClass) {
        @SuppressWarnings("unchecked")
        ServiceLoader<Object> emptyLoader = mock(ServiceLoader.class);
        when(emptyLoader.findFirst()).thenReturn(Optional.empty());

        serviceLoaderMock.when(() -> ServiceLoader.load(spiClass))
                .thenReturn(emptyLoader);
    }
}
module Weapon {
    requires Common;
    requires CommonBullet;
    requires CommonWeapon;
    requires java.logging;

    exports dk.sdu.mmmi.cbse.weapon;

    provides dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI
            with dk.sdu.mmmi.cbse.weapon.WeaponFactory;
    provides dk.sdu.mmmi.cbse.common.services.IProcessingService
            with dk.sdu.mmmi.cbse.weapon.WeaponSystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.weapon.WeaponPlugin;
}
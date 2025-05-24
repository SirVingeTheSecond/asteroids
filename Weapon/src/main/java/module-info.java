module Weapon {
    requires Common;
    requires CommonBullet;
    requires CommonWeapon;
    requires java.logging;
    requires Core;

    uses dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;

    exports dk.sdu.mmmi.cbse.weapon;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.weapon.WeaponSystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.weapon.WeaponPlugin;
    provides dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI
            with dk.sdu.mmmi.cbse.weapon.WeaponService;
}
import dk.sdu.mmmi.cbse.common.services.IUpdate;

module Weapon {
    uses dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
    requires Common;
    requires CommonBullet;
    requires CommonWeapon;
    requires java.logging;

    exports dk.sdu.mmmi.cbse.weapon;

    provides IUpdate
            with dk.sdu.mmmi.cbse.weapon.WeaponSystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.weapon.WeaponPlugin;
}
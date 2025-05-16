module Weapon {
    requires Common;

    exports dk.sdu.mmmi.cbse.weapon;

    provides dk.sdu.mmmi.cbse.common.bullet.BulletSPI
            with dk.sdu.mmmi.cbse.weapon.BulletFactory;
    provides dk.sdu.mmmi.cbse.common.services.IProcessingService
            with dk.sdu.mmmi.cbse.weapon.WeaponSystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.weapon.WeaponPlugin;

    uses dk.sdu.mmmi.cbse.common.services.IEventService;
}
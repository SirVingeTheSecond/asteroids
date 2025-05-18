module Enemy {
    uses dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
    requires Common;
    requires CommonEnemy;
    requires CommonWeapon;
    requires CommonCollision;

    requires java.logging;
    requires javafx.graphics;
}
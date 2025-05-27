module Score {
    uses dk.sdu.mmmi.cbse.common.services.IEventService;

    requires Common;
    requires CommonEnemy;
    requires java.logging;
    requires java.net.http;

    exports dk.sdu.mmmi.cbse.scoreclient;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.scoreclient.ScoreSystem;
}
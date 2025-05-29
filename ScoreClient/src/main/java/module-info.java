module ScoreClient {
    uses dk.sdu.mmmi.cbse.common.services.IEventService;

    requires Common;
    requires CommonEnemy;
    requires CommonAsteroid;
    requires java.logging;

    requires spring.web;
    requires spring.core;
    requires spring.beans;
    requires com.fasterxml.jackson.databind;

    exports dk.sdu.mmmi.cbse.scoreclient;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.scoreclient.ScoreSystem;
}
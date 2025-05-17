package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.core.utils.ApplicationArguments;
import javafx.application.Application;

/**
 * Entry point for the game.
 */
public class Main {
	public static void main(String[] args) {
		ApplicationArguments.parse(args);

		// Launch the JavaFX application
		Application.launch(Game.class, args);
	}
}
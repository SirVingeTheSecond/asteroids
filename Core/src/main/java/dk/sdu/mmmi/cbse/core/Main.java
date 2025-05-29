package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.core.config.GameConfiguration;
import dk.sdu.mmmi.cbse.core.utils.ApplicationArguments;
import javafx.application.Application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the game using Spring.
 * Initializes Spring ApplicationContext before launching JavaFX.
 */
public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	private static ApplicationContext applicationContext;

	public static void main(String[] args) {
		try {
			LOGGER.log(Level.INFO, "Starting Asteroids game using Spring");

			// Parse command line arguments
			ApplicationArguments.parse(args);

			// Init Spring ApplicationContext
			LOGGER.log(Level.INFO, "Initializing Spring ApplicationContext");
			applicationContext = new AnnotationConfigApplicationContext(GameConfiguration.class);

			logSpringBeans();

			// Launch the JavaFX application - Spring dependencies will be injected during Game.start()
			LOGGER.log(Level.INFO, "Launching JavaFX application");
			Application.launch(Game.class, args);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to start application", e);
			System.exit(1);
		} finally {
			if (applicationContext instanceof AnnotationConfigApplicationContext) {
				LOGGER.log(Level.INFO, "Closing Spring ApplicationContext");
				((AnnotationConfigApplicationContext) applicationContext).close();
			}
		}
	}

	/**
	 * Get the Spring ApplicationContext for other components if needed
	 * @return The current ApplicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Log information about Spring beans for debugging
	 */
	private static void logSpringBeans() {
		if (applicationContext != null) {
			String[] beanNames = applicationContext.getBeanDefinitionNames();
			LOGGER.log(Level.INFO, "Spring ApplicationContext initialized with {0} beans:", beanNames.length);

			for (String beanName : beanNames) {
				Class<?> beanType = applicationContext.getType(beanName);
				LOGGER.log(Level.FINE, "  Bean: {0} -> {1}",
						new Object[]{beanName, beanType != null ? beanType.getSimpleName() : "Unknown"});
			}
		}
	}
}
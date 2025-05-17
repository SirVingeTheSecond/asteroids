package dk.sdu.mmmi.cbse.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line argument parser and container.
 * Handles parsing and storing command line arguments.
 */
public class ApplicationArguments {
    private static final Logger LOGGER = Logger.getLogger(ApplicationArguments.class.getName());
    private static final Map<String, String> arguments = new HashMap<>();

    private ApplicationArguments() {

    }

    /**
     * Parse command line arguments
     *
     * @param args Command line arguments array
     */
    public static void parse(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        LOGGER.log(Level.INFO, "Parsing command line arguments");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // Handle flags (--flag)
            if (arg.startsWith("--")) {
                String flag = arg.substring(2);

                // Check if flag has a value (--flag=value)
                if (flag.contains("=")) {
                    String[] parts = flag.split("=", 2);
                    arguments.put(parts[0], parts[1]);
                    LOGGER.log(Level.FINE, "Parsed argument: {0}={1}", new Object[]{parts[0], parts[1]});
                } else {
                    // Flag without value
                    arguments.put(flag, "true");
                    LOGGER.log(Level.FINE, "Parsed flag: {0}", flag);
                }
            }
            // Handle short flags (-f)
            else if (arg.startsWith("-") && arg.length() > 1) {
                String flag = arg.substring(1);

                // Check if next arg is a value
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    arguments.put(flag, args[i + 1]);
                    LOGGER.log(Level.FINE, "Parsed argument: {0}={1}", new Object[]{flag, args[i + 1]});
                    i++; // Skip the value
                } else {
                    // Flag without value
                    arguments.put(flag, "true");
                    LOGGER.log(Level.FINE, "Parsed flag: {0}", flag);
                }
            }
        }
    }

    /**
     * Get argument value
     *
     * @param key Argument key
     * @return Argument value or null if not found
     */
    public static String get(String key) {
        return arguments.get(key);
    }

    /**
     * Get argument value with default
     *
     * @param key Argument key
     * @param defaultValue Default value if not found
     * @return Argument value or default
     */
    public static String get(String key, String defaultValue) {
        return arguments.getOrDefault(key, defaultValue);
    }

    /**
     * Check if argument exists
     *
     * @param key Argument key
     * @return true if argument exists
     */
    public static boolean has(String key) {
        return arguments.containsKey(key);
    }

    /**
     * Get argument as boolean
     *
     * @param key Argument key
     * @param defaultValue Default value if not found
     * @return Argument as boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (!arguments.containsKey(key)) {
            return defaultValue;
        }

        String value = arguments.get(key);
        return value.equalsIgnoreCase("true") ||
                value.equals("1") ||
                value.equalsIgnoreCase("yes") ||
                value.equalsIgnoreCase("y");
    }

    /**
     * Get argument as integer
     *
     * @param key Argument key
     * @param defaultValue Default value if not found or not a number
     * @return Argument as integer
     */
    public static int getInt(String key, int defaultValue) {
        if (!arguments.containsKey(key)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(arguments.get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
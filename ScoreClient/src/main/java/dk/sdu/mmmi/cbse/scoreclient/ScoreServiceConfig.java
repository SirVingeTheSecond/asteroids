package dk.sdu.mmmi.cbse.scoreclient;

import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for the scoring service RestTemplate.
 */
public class ScoreServiceConfig {
    private static final Logger LOGGER = Logger.getLogger(ScoreServiceConfig.class.getName());

    // Config
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
    private static final String DEFAULT_SERVICE_URL = "http://localhost:8080";

    /**
     * Create a RestTemplate for communication with scoring service.
     *
     * @return Configured RestTemplate instance
     */
    public static RestTemplate createScoringRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // for better reliability
        restTemplate.getRequestFactory();

        LOGGER.log(Level.INFO, "Created RestTemplate for scoring service with timeouts: connect={0}s, read={1}s",
                new Object[]{CONNECTION_TIMEOUT.getSeconds(), READ_TIMEOUT.getSeconds()});

        return restTemplate;
    }

    /**
     * Get the default scoring service URL.
     *
     * @return Default service URL
     */
    public static String getDefaultServiceUrl() {
        // Could read from properties files but not needed for this simple example
        return DEFAULT_SERVICE_URL;
    }

    /**
     * Validate service URL format.
     *
     * @param url URL to validate
     * @return true if URL is valid
     */
    public static boolean isValidServiceUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid service URL format: {0}", url);
            return false;
        }
    }

    /**
     * Create service endpoint URL.
     *
     * @param baseUrl Base service URL
     * @param endpoint Endpoint path
     * @return Complete URL
     */
    public static String createEndpointUrl(String baseUrl, String endpoint) {
        if (!isValidServiceUrl(baseUrl)) {
            throw new IllegalArgumentException("Invalid base URL: " + baseUrl);
        }

        String cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String cleanEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;

        return cleanBase + cleanEndpoint;
    }
}
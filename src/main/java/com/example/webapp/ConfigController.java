package com.example.webapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {

    @Value("${APP_ENV:local}")
    private String appEnv;

    @Value("${WELCOME_MESSAGE:Hello World}")
    private String welcomeMessage;

    @Value("${VERSION:1.0.0}")
    private String version;

    @Value("${API_URL:http://localhost}")
    private String apiUrl;

    @Value("${APP_COLOR:blue}")
    private String appColor;

    @GetMapping("/")
    public String welcome() {
        return String.format("<html><body style='background-color: %s; color: white; padding: 20px;'>" +
                        "<h1>%s</h1>" +
                        "<p>Environment: %s</p>" +
                        "<p>Version: %s</p>" +
                        "<p>API URL: %s</p>" +
                        "</body></html>",
                appColor, welcomeMessage, appEnv, version, apiUrl);
    }

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("APP_ENV", appEnv);
        config.put("WELCOME_MESSAGE", welcomeMessage);
        config.put("VERSION", version);
        config.put("API_URL", apiUrl);
        config.put("APP_COLOR", appColor);
        return config;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
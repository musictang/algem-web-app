package net.algem.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author jm
 * @version 21/05/2021
 */
@Component
public class GemOptions {

    private final static Logger LOGGER = Logger.getLogger(GemOptions.class.getName());

    @Value("#{options}")
    private Map<String, String> options;

    public boolean withFamilyManagement() {
        try {
            return Boolean.parseBoolean(options.get("family-management"));
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }
}

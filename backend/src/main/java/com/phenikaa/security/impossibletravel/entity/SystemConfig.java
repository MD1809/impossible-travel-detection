package com.phenikaa.security.impossibletravel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "system_configs")
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 50)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(length = 255)
    private String description;

    public SystemConfig() {
    }

    public SystemConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

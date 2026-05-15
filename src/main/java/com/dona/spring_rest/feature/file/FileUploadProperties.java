package com.dona.spring_rest.feature.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.upload")
public class FileUploadProperties {

    private String baseDir = "uploads";
    private long maxSizeBytes = 5L * 1024 * 1024;
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "gif", "webp"));
    private List<String> allowedFolders = new ArrayList<>(List.of("avatars", "logos"));

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedFolders() {
        return allowedFolders;
    }

    public void setAllowedFolders(List<String> allowedFolders) {
        this.allowedFolders = allowedFolders;
    }
}
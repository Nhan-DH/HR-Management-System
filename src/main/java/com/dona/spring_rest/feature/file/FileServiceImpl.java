package com.dona.spring_rest.feature.file;

import com.dona.spring_rest.feature.file.dto.FileUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FileServiceImpl implements FileService {

    private static final Pattern INVALID_FILE_NAME_CHARS = Pattern.compile("[^A-Za-z0-9_-]");

    private final FileUploadProperties properties;

    public FileServiceImpl(FileUploadProperties properties) {
        this.properties = properties;
    }

    @Override
    public FileUploadResponse store(MultipartFile file, String folder) {
        validateFilePresence(file);

        String normalizedFolder = normalizeFolder(folder);
        String originalFileName = extractOriginalFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        validateExtension(extension);

        String sanitizedBaseName = sanitizeBaseName(stripExtension(originalFileName));
        String storedFileName = System.currentTimeMillis() + "_" + sanitizedBaseName + "." + extension;

        Path baseDirectory = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
        Path targetDirectory = baseDirectory.resolve(normalizedFolder).normalize();
        ensureInsideBaseDirectory(baseDirectory, targetDirectory);

        Path targetFile = targetDirectory.resolve(storedFileName).normalize();
        ensureInsideBaseDirectory(baseDirectory, targetFile);

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetFile);
        } catch (FileAlreadyExistsException ex) {
            throw new FileUploadException("File đã tồn tại, vui lòng thử lại", ex);
        } catch (IOException ex) {
            throw new FileUploadException("Không thể lưu file upload", ex);
        }

        return new FileUploadResponse(
                storedFileName,
                normalizedFolder,
                "/uploads/" + normalizedFolder + "/" + storedFileName,
                file.getSize(),
                Instant.now());
    }

    private void validateFilePresence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File không được để trống");
        }

        if (file.getSize() > properties.getMaxSizeBytes()) {
            long maxSizeMb = properties.getMaxSizeBytes() / (1024 * 1024);
            throw new FileUploadException("Kích thước file không được vượt quá " + maxSizeMb + "MB");
        }
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            throw new FileUploadException("Thư mục upload không được để trống");
        }

        String normalizedFolder = folder.trim().toLowerCase(Locale.ROOT);
        Set<String> allowedFolders = properties.getAllowedFolders().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        if (!allowedFolders.contains(normalizedFolder)) {
            throw new FileUploadException("Thư mục upload không hợp lệ");
        }

        return normalizedFolder;
    }

    private String extractOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new FileUploadException("Tên file không hợp lệ");
        }

        String cleaned = originalFileName.trim().replace('\\', '/');
        String fileNameOnly = cleaned.substring(cleaned.lastIndexOf('/') + 1);

        if (fileNameOnly.isBlank()) {
            throw new FileUploadException("Tên file không hợp lệ");
        }

        return fileNameOnly;
    }

    private String stripExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            throw new FileUploadException("File phải có phần mở rộng hợp lệ");
        }
        return fileName.substring(0, lastDotIndex);
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex == fileName.length() - 1) {
            throw new FileUploadException("File phải có phần mở rộng hợp lệ");
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void validateExtension(String extension) {
        Set<String> allowedExtensions = properties.getAllowedExtensions().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        if (!allowedExtensions.contains(extension)) {
            throw new FileUploadException("Định dạng file không được hỗ trợ");
        }
    }

    private String sanitizeBaseName(String baseName) {
        String sanitized = INVALID_FILE_NAME_CHARS.matcher(baseName).replaceAll("_");
        sanitized = sanitized.replaceAll("_+", "_");

        if (sanitized.isBlank()) {
            return "file";
        }

        return sanitized;
    }

    private void ensureInsideBaseDirectory(Path baseDirectory, Path targetPath) {
        if (!targetPath.normalize().startsWith(baseDirectory)) {
            throw new FileUploadException("Đường dẫn file không hợp lệ");
        }
    }
}
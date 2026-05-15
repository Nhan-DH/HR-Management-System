package com.dona.spring_rest.feature.file;

import com.dona.spring_rest.feature.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse store(MultipartFile file, String folder);
}
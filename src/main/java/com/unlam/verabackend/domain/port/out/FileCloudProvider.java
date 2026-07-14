package com.unlam.verabackend.domain.port.out;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileCloudProvider {
    String uploadImage(MultipartFile image, String folder) throws IOException;
    void deleteImage(String imageUrl) throws IOException;
}

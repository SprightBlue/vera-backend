package com.unlam.verabackend.infrastructure.provider;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.unlam.verabackend.domain.port.out.FileCloudProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryFileCloudAdapter implements FileCloudProvider {

    private final Cloudinary cloudinary;

    public CloudinaryFileCloudAdapter(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(MultipartFile image, String folder) throws IOException {
        Map<?, ?> options = ObjectUtils.asMap("folder", folder);
        Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), options);
        return result.get("secure_url").toString();
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        String publicId = extractPublicId(imageUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String extractPublicId(String imageUrl) {
        String uploadMarker = "/upload/";
        String pathWithoutVersion = getString(imageUrl, uploadMarker);
        int extensionIndex = pathWithoutVersion.lastIndexOf('.');
        if (extensionIndex != -1)
            return pathWithoutVersion.substring(0, extensionIndex);

        return pathWithoutVersion;
    }

    private static @NonNull String getString(String imageUrl, String uploadMarker) {
        int uploadIndex = imageUrl.indexOf(uploadMarker);
        if (uploadIndex == -1)
            throw new IllegalArgumentException("La URL provista no es una URL válida de Cloudinary");

        String pathWithVersion = imageUrl.substring(uploadIndex + uploadMarker.length());
        return pathWithVersion.replaceFirst("^v\\d+/", "");
    }
}
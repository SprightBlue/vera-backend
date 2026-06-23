package com.unlam.verabackend.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile image, String folder) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("folder", folder);
        Map result = cloudinary.uploader().upload(image.getBytes(), options);
        return result.get("secure_url").toString();
    }
}
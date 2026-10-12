package com.backend.services.impl;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public record ImageUploadServiceImpl(S3Client s3Client) {

    private File convertMultipartFileToFile(MultipartFile file) throws IOException, IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    public String uploadFile(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        File file = convertMultipartFileToFile(imageFile);
        String bucketName = "yoddha-catering-storage-2026";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(imageName)
                .build();

        PutObjectResponse response = s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(file));

        file.delete();

        String imageUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageName);
        return imageUrl;
    }


    public boolean deleteFile(String fileUrl) {

        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }

        try {

            String bucketName = "yoddha-catering-storage-2026";

            // Extract key from URL
            // Example URL: https://bucket.s3.amazonaws.com/filename.jpg
            String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            DeleteObjectResponse response = s3Client.deleteObject(deleteObjectRequest);

            return true;

        } catch (S3Exception e) {
            System.err.println("Error deleting file from S3: " + e.awsErrorDetails().errorMessage());
            return false;
        }
    }
}

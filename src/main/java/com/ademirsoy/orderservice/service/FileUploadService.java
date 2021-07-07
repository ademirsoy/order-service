package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    public String uploadFile(MultipartFile file) {
        if (file == null) {
            throw new BadRequestException("File is empty!");
        } else if (!"application/zip".equals(file.getContentType())) {
            throw new BadRequestException("Only zip files are allowed");
        }
        //Upload file to a storage service

        return file.getOriginalFilename();
    }
}

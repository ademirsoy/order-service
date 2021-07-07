package com.ademirsoy.orderservice.service;

import com.ademirsoy.orderservice.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @InjectMocks
    FileUploadService fileUploadService;

    @Test
    void uploadFile_shouldThrowException_whenInvalidFileExtension() {

        //GIVEN
        MockMultipartFile mockFile = new MockMultipartFile("filename", "filename", "image/png", "file".getBytes());

        //WHEN
        BadRequestException e = assertThrows(BadRequestException.class, () -> fileUploadService.uploadFile(mockFile));

        //THEN
        assertThat(e.getMessage()).isEqualTo("Only zip files are allowed");
    }

    @Test
    void uploadFile_shouldReturnFileName_whenValidRequest() {

        //GIVEN
        MockMultipartFile mockFile = new MockMultipartFile("filename", "filename", "application/zip", "file".getBytes());

        //WHEN
        String actual = fileUploadService.uploadFile(mockFile);

        //THEN
        assertThat(actual).isEqualTo("filename");
    }
}

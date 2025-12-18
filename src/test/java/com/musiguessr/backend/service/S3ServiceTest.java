package com.musiguessr.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "us-east-1");
    }

    @Test
    void createPresignedUploadUrl_ShouldReturnUrl() throws MalformedURLException {
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL("https://presigned-url.com"));

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        String url = s3Service.createPresignedUploadUrl("test-key", "image/png");

        assertEquals("https://presigned-url.com", url);
    }

    @Test
    void doesFileExist_WhenExists_ShouldReturnTrue() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(null);

        boolean exists = s3Service.doesFileExist("some-key");

        assertTrue(exists);
    }

    @Test
    void doesFileExist_WhenNotExists_ShouldReturnFalse() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        boolean exists = s3Service.doesFileExist("non-existent-key");

        assertFalse(exists);
    }

    @Test
    void deleteFile_ShouldCallS3Client() {
        s3Service.deleteFile("file-to-delete");
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void getUrl_ShouldFormatStringCorrectly() {
        String url = s3Service.getUrl("my-song.mp3");
        assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/my-song.mp3", url);
    }
}
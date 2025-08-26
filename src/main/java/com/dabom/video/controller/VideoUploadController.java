package com.dabom.video.controller;

import com.dabom.common.BaseResponse;
import com.dabom.video.model.dto.PresignedUrlRequestDto;
import com.dabom.video.model.dto.PresignedUrlResponseDto;
import com.dabom.video.service.local.VideoLocalUploadService;
import com.dabom.video.service.s3.VideoS3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoUploadController {

    private final VideoLocalUploadService videoUploadService;
    private final VideoS3UploadService videoS3UploadService;

    @PostMapping("/upload")
    public ResponseEntity<Integer> upload(@RequestPart MultipartFile file) throws IOException {
        return ResponseEntity.ok(videoUploadService.upload(file));
    }

    @PostMapping("/presigned")
    public ResponseEntity<BaseResponse<PresignedUrlResponseDto>> getPresignedUrl(@RequestBody PresignedUrlRequestDto requestDto) {
        PresignedUrlResponseDto response = videoS3UploadService.generatePresignedUrl(requestDto);
        return ResponseEntity.ok(BaseResponse.of(response, HttpStatus.OK));
    }

}
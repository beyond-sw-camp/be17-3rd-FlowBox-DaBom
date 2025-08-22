package com.dabom.video.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    private String title;
    private String description;
    private boolean isVisibility;

    private String originalFilename;  // 업로드한 원본 파일 이름 (예: user_uploaded.mp4)
    private String originalPath;
    private Long originalSize;
    private String contentType; // MIME 타입 (video/mp4, application/x-mpegURL 등)

    private String savedPath; // 실제 저장된 경로 (로컬 경로 or S3 URL or m3u8 경로)
    private Long savedSize; // 파일 크기 (bytes)

    @Enumerated(EnumType.STRING)
    private VideoStatus videoStatus; // 영상 상태

    @Builder
    public Video(String originalFilename, String originalPath, Long originalSize, String contentType, VideoStatus status) {
        this.originalFilename = originalFilename;
        this.originalPath = originalPath;
        this.originalSize = originalSize;
        this.contentType = contentType;
        this.videoStatus = status;
    }

    public void updateVideoStatus(VideoStatus status) {
        this.videoStatus = status;
    }

    public void mappingVideoMetadata(String title, String description, boolean isVisibility) {
        this.title = title;
        this.description = description;
        this.isVisibility = isVisibility;
        this.videoStatus = VideoStatus.ENCODING_PENDING;
    }

    public void updateSavedPath(String savedPath) {
        this.savedPath = savedPath;
    }
}

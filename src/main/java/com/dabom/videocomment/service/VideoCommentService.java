package com.dabom.videocomment.service;

import com.dabom.member.model.entity.Member;
import com.dabom.member.repository.MemberRepository;
import com.dabom.video.model.Video;
import com.dabom.video.repository.VideoRepository;
import com.dabom.videocomment.model.dto.VideoCommentRegisterDto;
import com.dabom.videocomment.model.dto.VideoCommentResponseDto;
import com.dabom.videocomment.model.dto.VideoCommentUpdateDto;
import com.dabom.videocomment.model.entity.VideoComment;
import com.dabom.videocomment.repository.VideoCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VideoCommentService {

    private final VideoCommentRepository videoCommentRepository;
    private final VideoRepository videoRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Integer register(VideoCommentRegisterDto dto, Integer videoIdx, Integer memberIdx) {
        Video video = videoRepository.findById(videoIdx)
                .orElseThrow(() -> new EntityNotFoundException("영상을 찾을 수 없습니다: " + videoIdx));

        Member member = memberRepository.findById(memberIdx)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberIdx));

        VideoComment videoComment = dto.toEntity(video, member);
        return videoCommentRepository.save(videoComment).getIdx();
    }

    @Transactional
    public void deleted(Integer idx) {
        VideoComment videoComment = videoCommentRepository.findById(idx)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: " + idx));
        videoComment.delete(); // 메서드 이름 수정
        videoCommentRepository.save(videoComment);
    }

    public List<VideoCommentResponseDto> list(Integer videoIdx) {
        List<VideoComment> result = videoCommentRepository.findByVideo_IdxAndIsDeletedFalse(videoIdx);
        return result.stream()
                .map(VideoCommentResponseDto::from)
                .toList();
    }

    @Transactional
    public Integer update(Integer commentIdx, VideoCommentUpdateDto dto) {
        VideoComment entity = videoCommentRepository.findById(commentIdx)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: " + commentIdx));

        dto.toEntity(entity); // 기존 엔티티 수정
        videoCommentRepository.save(entity);

        return entity.getIdx();
    }
}

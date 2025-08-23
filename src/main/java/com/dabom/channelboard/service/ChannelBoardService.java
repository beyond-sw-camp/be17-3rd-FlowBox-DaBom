package com.dabom.channelboard.service;

import com.dabom.channelboard.model.dto.ChannelBoardReadResponseDto;
import com.dabom.channelboard.model.dto.ChannelBoardRegisterRequestDto;
import com.dabom.channelboard.model.dto.ChannelBoardUpdateRequestDto;
import com.dabom.channelboard.model.entity.ChannelBoard;
import com.dabom.channelboard.repositroy.ChannelBoardRepository;
import com.dabom.common.SliceBaseResponse;
import com.dabom.member.model.entity.Member;
import com.dabom.member.repository.MemberRepository;
import com.dabom.member.security.dto.MemberDetailsDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelBoardService {
    private final ChannelBoardRepository channelBoardRepository;
    private final MemberRepository memberRepository;

    public Integer register(ChannelBoardRegisterRequestDto dto
            , MemberDetailsDto memberDetailsDto) {
        Member memberIdx = memberRepository.findById(memberDetailsDto.getIdx()).
                orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ChannelBoard result = channelBoardRepository.save(dto.toEntity(memberIdx));
        return result.getIdx();
    }

    public SliceBaseResponse<ChannelBoardReadResponseDto> list(
            Integer page, Integer size, String sort, MemberDetailsDto memberDetailsDto) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<ChannelBoard> channelBoardSlice;

        switch (sort) {
            case "latest":
                channelBoardSlice = channelBoardRepository.findAllByChannelIdxAndIsDeletedFalseOrderByIdxDesc(
                        memberDetailsDto.getIdx(), pageable);
                break;
            case "oldest":
            default:
                channelBoardSlice = channelBoardRepository.findAllByChannelIdxAndIsDeletedFalseOrderByIdxAsc(
                        memberDetailsDto.getIdx(), pageable);
                break;
        }

        List<ChannelBoardReadResponseDto> content = channelBoardSlice.getContent()
                .stream()
                .map(board -> {
                    Long commentCount = channelBoardRepository.countCommentsByBoardIdx(board.getIdx());
                    return ChannelBoardReadResponseDto.fromWithCommentCount(board, commentCount);
                })
                .toList();

        Long totalCount = channelBoardRepository.countByChannelIdxAndIsDeletedFalse(memberDetailsDto.getIdx());
        return new SliceBaseResponse<ChannelBoardReadResponseDto>(content, channelBoardSlice.hasNext(), totalCount);
    }

    public ChannelBoardReadResponseDto read(Integer idx) {
        Optional<ChannelBoard> result = channelBoardRepository.findById(idx);
        if (result.isPresent()) {
            ChannelBoard board = result.get();
            Long commentCount = channelBoardRepository.countCommentsByBoardIdx(board.getIdx());
            return ChannelBoardReadResponseDto.fromWithCommentCount(board, commentCount);
        } else {
            throw new EntityNotFoundException("해당 게시글이 존재하지 않습니다: " + idx);
        }
    }

    public Integer update(ChannelBoardUpdateRequestDto dto) {
        ChannelBoard result = channelBoardRepository.findById(dto.toEntity().getIdx())
                .orElseThrow(() -> new EntityNotFoundException(""));

        result.update(dto.getTitle(),dto.getContents());

        return channelBoardRepository.save(result).getIdx();
    }

    public void delete(Integer idx) {
        Optional<ChannelBoard> result = channelBoardRepository.findById(idx);

        if (result.isPresent()) {
            ChannelBoard board = result.get();
            ChannelBoardUpdateRequestDto dto = new ChannelBoardUpdateRequestDto();
            ChannelBoard deleteBoard = dto.softDelete(board);
            channelBoardRepository.save(deleteBoard);
        } else {
            throw new EntityNotFoundException("해당 게시글이 존재하지 않습니다: " + idx);
        }
    }
}
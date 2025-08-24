package com.dabom.together.model.dto.response;

import com.dabom.member.model.dto.MemberInfoResponseDto;
import com.dabom.member.model.entity.Member;
import com.dabom.together.model.entity.Together;

public record TogetherInfoResponseDto(Integer togetherIdx, String title, Integer maxMemberNum,
                                      Integer joinMemberNumber, MemberInfoResponseDto master, Boolean isOpen) {
    public static TogetherInfoResponseDto toDto(Together together) {
        Member member = together.getMaster();

        return new TogetherInfoResponseDto(together.getIdx(), together.getTitle(), together.getMaxMemberNum(),
                together.getJoinMemberNum(), MemberInfoResponseDto.toDto(member), together.getIsOpen());
    }
}

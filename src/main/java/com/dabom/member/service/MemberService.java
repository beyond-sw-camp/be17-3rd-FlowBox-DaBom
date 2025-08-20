package com.dabom.member.service;

import com.dabom.member.model.dto.*;
import com.dabom.member.model.entity.Member;
import com.dabom.member.repository.MemberRepository;
import com.dabom.member.security.dto.MemberDetailsDto;
import com.dabom.member.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final AuthenticationManager manager;
    private final PasswordEncoder encoder;

    @Transactional
    public void signUpMember(MemberSignupRequestDto dto) {
        String encodedPassword = encoder.encode(dto.password());
        repository.save(dto.toEntity(encodedPassword));
    }

    public String loginMember(MemberLoginRequestDto dto) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password(), null);
        Authentication authenticate = manager.authenticate(token);
        MemberDetailsDto userDto = (MemberDetailsDto) authenticate.getPrincipal();

        return JwtUtils.generateLoginToken(userDto.getIdx(), userDto.getEmail(), userDto.getMemberRole());
    }

    public MemberInfoResponseDto readMemberInfo(MemberDetailsDto dto) {
        Member member = getMemberFromSecurity(dto);

        return MemberInfoResponseDto.toDto(member);
    }

    public MemberEmailCheckResponseDto checkMemberEmail(String email) {
        Optional<Member> optionalMember = repository.findByEmail(email);
        if(optionalMember.isEmpty()) {
            return MemberEmailCheckResponseDto.of(false);
        }
        return MemberEmailCheckResponseDto.of(true);
    }

    public MemberChannelNameCheckResponseDto checkMemberChannelName(String channelName) {
        Optional<Member> optionalMember = repository.findByName(channelName);
        if(optionalMember.isEmpty()) {
            return MemberChannelNameCheckResponseDto.of(false);
        }
        return MemberChannelNameCheckResponseDto.of(true);
    }

    @Transactional
    public void updateMemberName(MemberDetailsDto memberDetailsDto, MemberUpdateChannelRequestDto dto) {
        Member member = getMemberFromSecurity(memberDetailsDto);
        updateChannelName(dto, member);
        updateChannelContent(dto, member);
        repository.save(member);
    }

    @Transactional
    public void deleteMember(MemberDetailsDto dto) {
        Member member = getMemberFromSecurity(dto);
        member.deleteMember();
        repository.save(member);
    }

    private void updateChannelContent(MemberUpdateChannelRequestDto dto, Member member) {
        if(dto.content() != null) {
            member.updateContent(dto.content());
        }
    }

    private void updateChannelName(MemberUpdateChannelRequestDto dto, Member member) {
        if(dto.name() != null) {
            checkDuplicateName(dto);
            member.updateName(dto.name());
        }
    }

    private void checkDuplicateName(MemberUpdateChannelRequestDto dto) {
        MemberChannelNameCheckResponseDto check = checkMemberChannelName(dto.name());
        if(check.isDuplicate()) {
            throw new RuntimeException();
        }
    }

    private Member getMemberFromSecurity(MemberDetailsDto dto) {
        Integer idx = dto.getIdx();
        Optional<Member> optionalMember = repository.findById(idx);
        if(optionalMember.isEmpty()) {
            throw new RuntimeException();
        }
        return optionalMember.get();
    }
}

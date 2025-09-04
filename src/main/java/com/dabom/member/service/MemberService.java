package com.dabom.member.service;

import com.dabom.image.model.dto.ImageUploadResponseDto;
import com.dabom.image.model.entity.Image;
import com.dabom.image.repository.ImageRepository;
import com.dabom.image.service.ImageService;
import com.dabom.member.exception.MemberException;
import com.dabom.member.model.dto.request.MemberLoginRequestDto;
import com.dabom.member.model.dto.request.MemberSearchRequestDto;
import com.dabom.member.model.dto.request.MemberSignupRequestDto;
import com.dabom.member.model.dto.request.MemberUpdateChannelRequestDto;
import com.dabom.member.model.dto.response.*;
import com.dabom.member.model.entity.Member;
import com.dabom.member.repository.MemberRepository;
import com.dabom.member.security.dto.MemberDetailsDto;
import com.dabom.member.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.dabom.member.exception.MemberExceptionType.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final AuthenticationManager manager;
    private final PasswordEncoder encoder;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @Transactional
    public void signUpMember(MemberSignupRequestDto dto) {
        String encodedPassword = encoder.encode(dto.getPassword());
        checkDuplicatedName(dto);
        Optional<Member> optional = repository.findByEmail(dto.getEmail());
        if (optional.isPresent()) {
            Member member = optional.get();
            checkIsNotDelete(member);
            member.rollBackMember();
            repository.save(member);
            return;
        }
        repository.save(dto.toEntity(encodedPassword));
    }

    public MemberLoginResponseDto loginMember(MemberLoginRequestDto dto) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword(), null);
        Authentication authenticate = manager.authenticate(token);
        MemberDetailsDto userDto = (MemberDetailsDto) authenticate.getPrincipal();
        String jwt = JwtUtils.generateLoginToken(userDto.getIdx(), userDto.getEmail(), userDto.getMemberRole());

        return MemberLoginResponseDto.of(jwt, userDto.getName());
    }

    public MemberListResponseDto searchMemberName(MemberSearchRequestDto dto) {
        List<Member> members = repository.findMembersByName(dto.getName());
        return MemberListResponseDto.toDto(members);
    }

    public MemberInfoResponseDto readMemberInfo(MemberDetailsDto dto) {
        Member member = getMemberFromSecurity(dto);
        MemberInfoResponseDto responseDto = MemberInfoResponseDto.toDto(member);
        String profileImageUrl = getProfileImg(member.getIdx());
        return new MemberInfoResponseDto(
            responseDto.id(),
            responseDto.name(),
            responseDto.content(),
            responseDto.email(),
            responseDto.videoCount(),
            profileImageUrl
        );
    }

    public MemberEmailCheckResponseDto checkMemberEmail(String email) {
        Optional<Member> optionalMember = repository.findByEmail(email);
        if (optionalMember.isEmpty()) {
            return MemberEmailCheckResponseDto.of(false);
        }
        return MemberEmailCheckResponseDto.of(true);
    }

    public MemberChannelNameCheckResponseDto checkMemberChannelName(String channelName) {
        Optional<Member> optionalMember = repository.findByName(channelName);
        if (optionalMember.isEmpty()) {
            return MemberChannelNameCheckResponseDto.of(false);
        }
        return MemberChannelNameCheckResponseDto.of(true);
    }

    public MemberInfoResponseDto getMemberInfo(MemberDetailsDto dto) {
        Member member = repository.findById(dto.getIdx()).orElseThrow();
        MemberInfoResponseDto responseDto = MemberInfoResponseDto.toDto(member);
        String profileImageUrl = getProfileImg(member.getIdx());
        return new MemberInfoResponseDto(
            responseDto.id(),
            responseDto.name(),
            responseDto.content(),
            responseDto.email(),
            responseDto.videoCount(),
            profileImageUrl
        );
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

    public MemberInfoResponseDto getChannelInfoByChannelName(String channelName) {
        Member member = repository.findByName(channelName)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
        MemberInfoResponseDto responseDto = MemberInfoResponseDto.toDto(member);
        String profileImageUrl = getProfileImg(member.getIdx());
        return new MemberInfoResponseDto(
            responseDto.id(),
            responseDto.name(),
            responseDto.content(),
            responseDto.email(),
            responseDto.videoCount(),
            profileImageUrl
        );
    }

    private void checkIsNotDelete(Member member) {
        if (member.getIsDeleted()) {
            throw new MemberException(DUPLICATED_SIGNUP);
        }
    }

    private void checkDuplicatedName(MemberSignupRequestDto dto) {
        Optional<Member> checkName = repository.findByName(dto.getChannelName());
        if (checkName.isEmpty()) {
            return;
        }
        throw new MemberException(DUPLICATED_CHANNEL_NAME);
    }

    private void updateChannelContent(MemberUpdateChannelRequestDto dto, Member member) {
        if (dto.getContent() != null) {
            member.updateContent(dto.getContent());
        }
    }

    private void updateChannelName(MemberUpdateChannelRequestDto dto, Member member) {
        if (dto.getName() != null) {
            checkDuplicateName(dto);
            member.updateName(dto.getName());
        }
    }

    private void checkDuplicateName(MemberUpdateChannelRequestDto dto) {
        MemberChannelNameCheckResponseDto check = checkMemberChannelName(dto.getName());
        if (check.isDuplicate()) {
            throw new MemberException(DUPLICATED_CHANNEL_NAME);
        }
    }

    private Member getMemberFromSecurity(MemberDetailsDto dto) {
        Integer idx = dto.getIdx();
        Optional<Member> optionalMember = repository.findById(idx);
        if (optionalMember.isEmpty()) {
            throw new MemberException(MEMBER_NOT_FOUND);
        }
        return optionalMember.get();
    }

    public String getProfileImg(Integer memberIdx) {
        Member member = repository.findById(memberIdx)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
        if (member.getProfileImage() == null) {
            String defaultProfileUrl = "/Image/Dabompng.png";
            return defaultProfileUrl;
        }
        return imageService.find(member.getProfileImage().getIdx());
    }

    @Transactional
    public void updateProfileImage(MemberDetailsDto memberDetailsDto, Image image) {
        Member member = getMemberFromSecurity(memberDetailsDto);
        member.changeProfile(image);
        repository.save(member);
    }

    @Transactional
    public void updateBannerImage(MemberDetailsDto memberDetailsDto, Image image) {
        Member member = getMemberFromSecurity(memberDetailsDto);
        member.changeBannerImg(image);
        repository.save(member);
    }


    @Transactional
    public ImageUploadResponseDto updateMemberProfileImage(MemberDetailsDto memberDetailsDto, MultipartFile file, String directory) throws IOException {
        ImageUploadResponseDto uploadResponse = imageService.uploadSingleImage(file, directory);
        Image profileImage = imageRepository.findById(uploadResponse.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
        updateProfileImage(memberDetailsDto, profileImage);
        return uploadResponse;
    }

    public String getBannerImage(Integer memberIdx) {
        Member member = repository.findById(memberIdx)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
        if (member.getBannerImage() == null) {
            String defaultProfileUrl = "/Image/Dabompng.png";
            return defaultProfileUrl;
        }
        return imageService.find(member.getBannerImage().getIdx());
    }

    public void updateBannerImage(MemberDetailsDto memberDetailsDto, MultipartFile file, String directory) {
        ImageUploadResponseDto uploadResponse = imageService.uploadSingleImage(file, directory);
        Image BannerImage = imageRepository.findById(uploadResponse.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
        updateBannerImage(memberDetailsDto, BannerImage);
    }
}

package com.dabom.member.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberUpdateChannelRequestDto {
    @NotBlank
    private Integer id;
//    @NotBlank
    private String name;

    private String content;
}

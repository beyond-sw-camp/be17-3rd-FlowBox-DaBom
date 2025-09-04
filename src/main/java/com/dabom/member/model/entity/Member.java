package com.dabom.member.model.entity;

import com.dabom.channelboard.model.entity.ChannelBoard;
import com.dabom.common.BaseEntity;
import com.dabom.image.model.entity.Image;
import com.dabom.likes.model.likes.Likes;
import com.dabom.score.model.entity.Score;
import com.dabom.video.model.Video;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String name; // 채널명

    private String password;
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(name = "member_role")
    private MemberRole memberRole;

    // 내가 구독한 사람
    @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY)
    private List<Subscribe> subscribes;
    // 나를 구독한 사람
    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<Subscribe> subscriptions;
    // 채널에 올라가는 게시글 모음
    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<ChannelBoard> channelBoards;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Score> givenScores;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<Score> receivedChannelScores; // 평점 리스트
    private Long score;                        // 평점

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<Likes> likesList;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Video> videoList;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profileimg_idx")
    private Image profileImage;

    @OneToOne
    @JoinColumn(name = "BannerImg_idx")
    private Image bannerImage;

    private Long subscribeCount;
    private Long sumScoreMember;
    private Boolean isDeleted;

    private Long sumScore;

    @Builder
    public Member(String email, String name, String password, String memberRole, Long subscribeCount, Long score, Long sumScoreMember) {
        this.email = email;
        this.name = name;
        this.content = null;
        this.password = password;
        this.memberRole = MemberRole.valueOf(memberRole);
        this.subscribeCount = 0L;
        this.score = 0L;
        this.sumScoreMember = 0L;
        this.isDeleted = false;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void voteScore(Long score) {
        this.score += score;
        this.sumScoreMember++;
    }

    public void updateProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }

    public void countSubscribe() {
        this.subscribeCount++;
    }

    public void cancelSubscribe() {
        this.subscribeCount--;
    }

    public void deleteMember() {
        this.isDeleted = true;
    }

    public void rollBackMember() {
        this.isDeleted = false;
    }

    public void changeProfile(Image profile) {
        this.profileImage = profile;
    }
    public void changeBannerImg(Image banner) {
        this.bannerImage = banner;
    }
}

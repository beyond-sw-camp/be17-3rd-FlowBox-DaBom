package com.dabom.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass // 부모클래스의 변수가 JPA로 동작할 수 있게 해주는 어노테이션
public class BaseEntity {
    @CreatedDate
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
}

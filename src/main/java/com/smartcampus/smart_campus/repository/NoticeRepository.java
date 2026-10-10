package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Notice;
import com.smartcampus.smart_campus.entity.NoticeAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByAudienceInOrCreatedByEmail(
            Collection<NoticeAudience> audiences, String email, Pageable pageable);
}
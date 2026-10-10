package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.dto.NoticeRequest;
import com.smartcampus.smart_campus.dto.NoticeResponse;
import com.smartcampus.smart_campus.dto.PageResponse;
import com.smartcampus.smart_campus.entity.Notice;
import com.smartcampus.smart_campus.entity.NoticeAudience;
import com.smartcampus.smart_campus.entity.Role;
import com.smartcampus.smart_campus.entity.User;
import com.smartcampus.smart_campus.exception.ForbiddenException;
import com.smartcampus.smart_campus.exception.ResourceNotFoundException;
import com.smartcampus.smart_campus.repository.NoticeRepository;
import com.smartcampus.smart_campus.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public NoticeService(NoticeRepository noticeRepository, UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NoticeResponse create(NoticeRequest request, String email) {
        User user = findUser(email);
        if (user.getRole() == Role.FACULTY && request.audience() != NoticeAudience.STUDENT) {
            throw new ForbiddenException("Faculty can post notices only for students");
        }

        Notice notice = new Notice();
        notice.setTitle(request.title());
        notice.setContent(request.content());
        notice.setAudience(request.audience());
        notice.setCreatedBy(user);
        return toResponse(noticeRepository.save(notice));
    }

    @Transactional(readOnly = true)
    public PageResponse<NoticeResponse> list(String email, Pageable pageable) {
        User user = findUser(email);
        Page<Notice> page;
        if (user.getRole() == Role.ADMIN) {
            page = noticeRepository.findAll(pageable);
        } else {
            NoticeAudience mine = user.getRole() == Role.STUDENT
                    ? NoticeAudience.STUDENT : NoticeAudience.FACULTY;
            page = noticeRepository.findByAudienceInOrCreatedByEmail(
                    List.of(NoticeAudience.ALL, mine), email, pageable);
        }
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public void delete(Long id, String email) {
        User user = findUser(email);
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id " + id));
        boolean owner = notice.getCreatedBy().getId().equals(user.getId());
        if (user.getRole() != Role.ADMIN && !owner) {
            throw new ForbiddenException("You can delete only your own notices");
        }
        noticeRepository.delete(notice);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NoticeResponse toResponse(Notice n) {
        return new NoticeResponse(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.getAudience(),
                n.getCreatedBy().getFullName(),
                n.getCreatedBy().getRole(),
                n.getCreatedAt());
    }
}
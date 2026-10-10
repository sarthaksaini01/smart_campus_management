package com.smartcampus.smart_campus.controller;

import com.smartcampus.smart_campus.dto.NoticeRequest;
import com.smartcampus.smart_campus.dto.NoticeResponse;
import com.smartcampus.smart_campus.dto.PageResponse;
import com.smartcampus.smart_campus.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<NoticeResponse> create(@Valid @RequestBody NoticeRequest request,
                                                 Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeService.create(request, auth.getName()));
    }

    @GetMapping
    public PageResponse<NoticeResponse> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             Authentication auth) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return noticeService.list(auth.getName(), pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        noticeService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
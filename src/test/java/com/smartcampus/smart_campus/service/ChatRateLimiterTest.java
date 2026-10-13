package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatRateLimiterTest {

    private final ChatRateLimiter limiter = new ChatRateLimiter();

    @Test
    void allowsEightMessagesPerMinuteThenBlocks() {
        for (int i = 0; i < 8; i++) {
            assertDoesNotThrow(() -> limiter.check("a@test.com"));
        }
        assertThrows(TooManyRequestsException.class, () -> limiter.check("a@test.com"));
    }

    @Test
    void eachUserHasHisOwnLimit() {
        for (int i = 0; i < 8; i++) {
            limiter.check("a@test.com");
        }
        assertDoesNotThrow(() -> limiter.check("b@test.com"));
    }
}
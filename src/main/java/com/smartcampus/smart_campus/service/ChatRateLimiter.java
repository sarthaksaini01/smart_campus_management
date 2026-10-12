package com.smartcampus.smart_campus.service;

import com.smartcampus.smart_campus.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRateLimiter {

    private static final int PER_MINUTE = 8;
    private static final int PER_DAY = 100;
    private static final long MINUTE_MS = 60_000L;
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public void check(String user) {
        long now = System.currentTimeMillis();
        Deque<Long> times = hits.computeIfAbsent(user, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() > DAY_MS) {
                times.pollFirst();
            }
            if (times.size() >= PER_DAY) {
                throw new TooManyRequestsException("Daily AI chat limit reached. Please try again tomorrow");
            }
            long lastMinute = times.stream().filter(t -> now - t <= MINUTE_MS).count();
            if (lastMinute >= PER_MINUTE) {
                throw new TooManyRequestsException("Too many messages. Please wait a minute");
            }
            times.addLast(now);
        }
    }
}
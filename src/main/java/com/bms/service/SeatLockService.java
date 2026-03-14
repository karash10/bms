package com.bms.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(StringRedisTemplate.class)
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;

    public SeatLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final long LOCK_TTL = 300; // 5 minutes

    private String getKey(Long showId, Long seatId) {
        return "seat_lock:" + showId + ":" + seatId;
    }

    public boolean lockSeat(Long showId, Long seatId, String userId) {

        String key = getKey(showId, seatId);

        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key,
                userId,
                LOCK_TTL,
                TimeUnit.SECONDS
        );

        return Boolean.TRUE.equals(success);
    }

    public void releaseSeat(Long showId, Long seatId) {

        String key = getKey(showId, seatId);
        redisTemplate.delete(key);
    }

    public boolean isSeatLocked(Long showId, Long seatId) {

        String key = getKey(showId, seatId);
        Boolean exists = redisTemplate.hasKey(key);

        return Boolean.TRUE.equals(exists);
    }

    public boolean lockSeats(Long showId, List<Long> seatIds, String userId) {

        for (Long seatId : seatIds) {

            boolean locked = lockSeat(showId, seatId, userId);

            if (!locked) {
                releaseSeats(showId, seatIds);
                return false;
            }
        }

        return true;
    }

    public void releaseSeats(Long showId, List<Long> seatIds) {

        for (Long seatId : seatIds) {
            releaseSeat(showId, seatId);
        }
    }
}

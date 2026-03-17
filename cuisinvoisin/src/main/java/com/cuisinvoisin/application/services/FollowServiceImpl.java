// com/cuisinvoisin/application/services/FollowServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.response.CookSummaryResponse;
import com.cuisinvoisin.application.mappers.CookMapper;
import com.cuisinvoisin.domain.entities.Follow;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.repositories.CookRepository;
import com.cuisinvoisin.domain.repositories.FollowRepository;
import com.cuisinvoisin.domain.services.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final CookRepository cookRepository;
    private final CookMapper cookMapper;

    @Override
    @Transactional
    public void toggleFollow(UUID clientId, UUID cookId) {
        if (!cookRepository.existsById(cookId)) {
            throw new ResourceNotFoundException("Cook", cookId);
        }
        if (followRepository.existsByClientIdAndCookId(clientId, cookId)) {
            followRepository.deleteByClientIdAndCookId(clientId, cookId);
            log.debug("Unfollowed: client={} cook={}", clientId, cookId);
        } else {
            Follow follow = Follow.builder()
                    .clientId(clientId)
                    .cookId(cookId)
                    .followedAt(Instant.now())
                    .build();
            followRepository.save(follow);
            log.debug("Followed: client={} cook={}", clientId, cookId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CookSummaryResponse> getFollowedCooks(UUID clientId) {
        return followRepository.findByClientIdOrderByFollowedAtDesc(clientId).stream()
                .map(f -> cookRepository.findById(f.getCookId()).orElse(null))
                .filter(cook -> cook != null)
                .map(cookMapper::toSummary)
                .toList();
    }
}

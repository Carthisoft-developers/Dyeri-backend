// com/cuisinvoisin/application/services/DashboardServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.response.DashboardResponse;
import com.cuisinvoisin.domain.services.CookService;
import com.cuisinvoisin.domain.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CookService cookService;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getCookDashboard(UUID cookId) {
        return cookService.getCookDashboard(cookId);
    }
}

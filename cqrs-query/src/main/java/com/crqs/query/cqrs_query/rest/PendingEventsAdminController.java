package com.crqs.query.cqrs_query.rest;

import com.crqs.query.cqrs_query.domain.dto.PendingOrderEventDTO;
import com.crqs.query.cqrs_query.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/admin/pending-events")
@RequiredArgsConstructor
@Slf4j
public class PendingEventsAdminController {

    private final EventQueryService eventQueryService;

    @PostMapping("/process")
    public String processAllPendingEvents() {
        return eventQueryService.processAllPendingEvents();
    }

    @GetMapping("/pending")
    public List<PendingOrderEventDTO> getAllPendingEvents() {
        return eventQueryService.getAllPendingEventsDTO();
    }

    @GetMapping("/pending/{correlationId}")
    public List<PendingOrderEventDTO> getPendingEventsByCorrelationId(@PathVariable String correlationId) {
        return eventQueryService.getPendingEventsByCorrelationIdDTO(correlationId);
    }
} 
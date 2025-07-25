package com.crqs.query.cqrs_query.rest;

import com.crqs.query.cqrs_query.domain.dto.ProcessedEventDTO;
import com.crqs.query.cqrs_query.service.EventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/processed-events")
@RequiredArgsConstructor
public class ProcessedEventsAdminController {
    private final EventQueryService eventQueryService;

    @GetMapping
    public List<ProcessedEventDTO> getAllProcessedEvents() {
        return eventQueryService.getAllProcessedEventsDTO();
    }

    @GetMapping("/{correlationId}")
    public List<ProcessedEventDTO> getProcessedEventsByCorrelationId(@PathVariable String correlationId) {
        return eventQueryService.getProcessedEventsByCorrelationIdDTO(correlationId);
    }
} 
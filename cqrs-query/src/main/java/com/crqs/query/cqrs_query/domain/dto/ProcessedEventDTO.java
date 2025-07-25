package com.crqs.query.cqrs_query.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEventDTO {
    private String id;
    private String correlationId;
    private String eventType;
    private LocalDateTime processedAt;
} 
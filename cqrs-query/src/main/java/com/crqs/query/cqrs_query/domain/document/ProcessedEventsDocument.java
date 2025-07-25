package com.crqs.query.cqrs_query.domain.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "processed_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessedEventsDocument {
    @Id
    private String id;
    @Indexed
    private String correlationId;
    private String eventType;
    private LocalDateTime processedAt;
}

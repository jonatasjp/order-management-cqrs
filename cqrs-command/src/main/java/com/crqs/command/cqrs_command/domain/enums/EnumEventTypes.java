package com.crqs.command.cqrs_command.domain.enums;

import com.crqs.command.cqrs_command.domain.dto.events.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EnumEventTypes {
    ORDER_CREATED(CreatedOrderEvent.class),
    ORDER_CONFIRMED(ConfirmedOrderEvent.class),
    ORDER_CANCELED(CanceledOrderEvent.class),
    ITEM_ADDED_TO_ORDER(ItemAddedToOrderEvent.class),
    ITEM_REMOVED_FROM_ORDER(ItemRemovedFromOrderEvent.class);

    private final Class<?> payloadClass;

    public static EnumEventTypes fromEventType(String eventType) {
        return Arrays.stream(values())
                .filter(meta -> meta.name().equals(eventType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown event type: " + eventType));
    }

}

package com.crqs.query.cqrs_query.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumEventTypes {
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_CANCELED,
    ITEM_ADDED_TO_ORDER,
    ITEM_REMOVED_FROM_ORDER;
}



export interface AlertSchema {
    record CreateAlertRequest(
        String name,
        String[] eventTypes,
        Integer[] mccCategoryCodes,
        Long minBudgetCents,
        Long maxBudgetCents,
        Integer minQuantity,
        Boolean notifyPush,
        Boolean notifyEmail
    ) {}

    record AlertResponse(
        String id,
        String name,
        String[] eventTypes,
        Integer[] mccCategoryCodes,
        Long minBudgetCents,
        Long maxBudgetCents,
        Boolean active,
        Instant createdAt
    ) {}
}

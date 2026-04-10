export interface ToggleComparisonResult {
  nextIds: string[];
  limitReached: boolean;
}

export function toggleComparisonSelection(
  currentIds: string[],
  responseId: string,
  maxSelections = 2
): ToggleComparisonResult {
  if (currentIds.includes(responseId)) {
    return {
      nextIds: currentIds.filter((id) => id !== responseId),
      limitReached: false,
    };
  }

  if (currentIds.length >= maxSelections) {
    return {
      nextIds: currentIds,
      limitReached: true,
    };
  }

  return {
    nextIds: [...currentIds, responseId],
    limitReached: false,
  };
}


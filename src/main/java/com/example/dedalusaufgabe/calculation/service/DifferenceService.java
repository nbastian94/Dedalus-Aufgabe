package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.model.DenominationCount;
import com.example.dedalusaufgabe.calculation.model.DenominationDelta;
import com.example.dedalusaufgabe.calculation.model.EuroDenomination;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates per-denomination deltas between a previous and current breakdown.
 */
@Service
public class DifferenceService {

    public List<DenominationDelta> calculateDifference(
            List<DenominationCount> previous,
            List<DenominationCount> current
    ) {
        Map<EuroDenomination, Integer> previousMap = toMap(previous);
        Map<EuroDenomination, Integer> currentMap = toMap(current);

        return EuroDenomination.descending().stream()
                .map(denomination -> new DenominationDelta(
                        denomination,
                        currentMap.getOrDefault(denomination, 0) - previousMap.getOrDefault(denomination, 0)
                ))
                .filter(delta -> previousMap.getOrDefault(delta.denomination(), 0) != 0
                        || currentMap.getOrDefault(delta.denomination(), 0) != 0)
                .collect(Collectors.toList());
    }

    private Map<EuroDenomination, Integer> toMap(List<DenominationCount> counts) {
        Map<EuroDenomination, Integer> map = new EnumMap<>(EuroDenomination.class);
        for (DenominationCount count : counts) {
            map.put(count.denomination(), count.count());
        }
        return map;
    }
}

package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.dto.CalculateResponseDto;
import com.example.dedalusaufgabe.calculation.dto.CalculationDto;
import com.example.dedalusaufgabe.calculation.dto.DenominationCountDto;
import com.example.dedalusaufgabe.calculation.dto.DifferenceDto;
import com.example.dedalusaufgabe.calculation.exception.InvalidCalculationException;
import com.example.dedalusaufgabe.calculation.model.Calculation;
import com.example.dedalusaufgabe.calculation.model.DenominationCount;
import com.example.dedalusaufgabe.calculation.model.DenominationDelta;
import com.example.dedalusaufgabe.calculation.model.EuroDenomination;
import com.example.dedalusaufgabe.calculation.util.MoneyUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CalculationMapper {

    public CalculateResponseDto toCalculateResponseDto(Calculation calculation, List<DenominationDelta> difference) {
        List<DifferenceDto> differenceDto = difference.isEmpty()
                ? null
                : difference.stream()
                .map(value -> new DifferenceDto(value.denomination().display(), value.delta()))
                .toList();

        return new CalculateResponseDto(
                MoneyUtils.centsToAmount(calculation.amountInCents()),
                toDenominationCountDtos(calculation.breakdown()),
                differenceDto
        );
    }

    public CalculationDto toCalculationDto(Calculation calculation) {
        return new CalculationDto(
                MoneyUtils.centsToAmount(calculation.amountInCents()),
                toDenominationCountDtos(calculation.breakdown())
        );
    }

    public Calculation fromCalculationDto(CalculationDto dto) {
        long amountInCents = MoneyUtils.parseAmountToCents(dto.amount(), true);
        if (dto.breakdown() == null) {
            throw new InvalidCalculationException("Breakdown is required");
        }
        List<DenominationCount> breakdown = toModelBreakdown(dto.breakdown());

        long breakdownTotal = breakdown.stream()
                .mapToLong(item -> (long) item.count() * item.denomination().cents())
                .sum();

        if (breakdownTotal != amountInCents) {
            throw new InvalidCalculationException("Breakdown does not sum up to amount");
        }

        return new Calculation(amountInCents, breakdown);
    }

    private List<DenominationCountDto> toDenominationCountDtos(List<DenominationCount> breakdown) {
        return breakdown.stream()
                .filter(item -> item.count() > 0)
                .map(item -> new DenominationCountDto(item.denomination().display(), item.count()))
                .toList();
    }

    private List<DenominationCount> toModelBreakdown(List<DenominationCountDto> dtoBreakdown) {
        List<DenominationCount> mapped = new ArrayList<>();
        Set<EuroDenomination> seen = new HashSet<>();

        for (DenominationCountDto item : dtoBreakdown) {
            if (item.count() < 0) {
                throw new InvalidCalculationException("Count must be >= 0 for denomination: " + item.denomination());
            }
            EuroDenomination denomination = EuroDenomination.fromDisplay(item.denomination());
            if (!seen.add(denomination)) {
                throw new InvalidCalculationException("Duplicate denomination: " + item.denomination());
            }
            if (item.count() > 0) {
                mapped.add(new DenominationCount(denomination, item.count()));
            }
        }

        return mapped.stream()
                .sorted(Comparator.comparingInt(item -> item.denomination().ordinal()))
                .collect(Collectors.toList());
    }
}

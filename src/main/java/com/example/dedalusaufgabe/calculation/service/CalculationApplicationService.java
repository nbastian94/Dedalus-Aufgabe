package com.example.dedalusaufgabe.calculation.service;

import com.example.dedalusaufgabe.calculation.dto.CalculateResponseDto;
import com.example.dedalusaufgabe.calculation.dto.CalculationDto;
import com.example.dedalusaufgabe.calculation.model.Calculation;
import com.example.dedalusaufgabe.calculation.model.DenominationDelta;
import com.example.dedalusaufgabe.calculation.model.EuroDenomination;
import com.example.dedalusaufgabe.calculation.util.MoneyUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrates calculation use cases, persistence of last result, and DTO mapping.
 */
@Service
public class CalculationApplicationService {

    private final BreakdownService breakdownService;
    private final DifferenceService differenceService;
    private final CalculationStoreService calculationStoreService;
    private final CalculationMapper calculationMapper;

    public CalculationApplicationService(
            BreakdownService breakdownService,
            DifferenceService differenceService,
            CalculationStoreService calculationStoreService,
            CalculationMapper calculationMapper
    ) {
        this.breakdownService = breakdownService;
        this.differenceService = differenceService;
        this.calculationStoreService = calculationStoreService;
        this.calculationMapper = calculationMapper;
    }

    public CalculateResponseDto calculate(String amount) {
        long amountInCents = MoneyUtils.parseAmountToCents(amount, true);
        Calculation current = new Calculation(amountInCents, breakdownService.calculateBreakdown(amountInCents));

        List<DenominationDelta> difference = calculationStoreService.getLastCalculation()
                .map(previous -> differenceService.calculateDifference(previous.breakdown(), current.breakdown()))
                .orElse(List.of());

        calculationStoreService.saveCalculation(current);
        return calculationMapper.toCalculateResponseDto(current, difference);
    }

    public Optional<CalculationDto> getStoredCalculation() {
        return calculationStoreService.getLastCalculation().map(calculationMapper::toCalculationDto);
    }

    public void storeCalculation(CalculationDto calculationDto) {
        Calculation calculation = calculationMapper.fromCalculationDto(calculationDto);
        calculationStoreService.saveCalculation(calculation);
    }

    public List<String> getDenominations() {
        return EuroDenomination.descending().stream()
                .map(EuroDenomination::display)
                .toList();
    }
}

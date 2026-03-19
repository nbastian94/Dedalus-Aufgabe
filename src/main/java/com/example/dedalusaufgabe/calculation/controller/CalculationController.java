package com.example.dedalusaufgabe.calculation.controller;

import com.example.dedalusaufgabe.calculation.dto.CalculateRequestDto;
import com.example.dedalusaufgabe.calculation.dto.CalculateResponseDto;
import com.example.dedalusaufgabe.calculation.dto.CalculationDto;
import com.example.dedalusaufgabe.calculation.service.CalculationApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes REST endpoints for calculating, retrieving, and storing euro denomination breakdowns.
 */
@RestController
public class CalculationController {

    private final CalculationApplicationService calculationApplicationService;

    public CalculationController(CalculationApplicationService calculationApplicationService) {
        this.calculationApplicationService = calculationApplicationService;
    }

    @PostMapping("/calculate")
    public CalculateResponseDto calculate(@Valid @RequestBody CalculateRequestDto requestDto) {
        return calculationApplicationService.calculate(requestDto.amount());
    }

    @GetMapping("/calculations")
    public ResponseEntity<CalculationDto> getStoredCalculation() {
        return calculationApplicationService.getStoredCalculation()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/calculations")
    public ResponseEntity<Void> storeCalculation(@Valid @RequestBody CalculationDto calculationDto) {
        calculationApplicationService.storeCalculation(calculationDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/denominations")
    public List<String> getDenominations() {
        return calculationApplicationService.getDenominations();
    }
}

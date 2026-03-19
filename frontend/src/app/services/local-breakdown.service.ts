import { Injectable } from '@angular/core';

import { EURO_DENOMINATIONS } from '../constants/denominations';
import { CalculateResponseDto, CalculationDto, DenominationCountDto, DifferenceDto } from '../models/calculation.models';

@Injectable({ providedIn: 'root' })
export class LocalBreakdownService {
  calculate(amount: string, previous: CalculationDto | null): CalculateResponseDto {
    const amountInCents = this.amountToCents(amount);
    const breakdown = this.calculateBreakdown(amountInCents);
    const difference = previous ? this.calculateDifference(previous.breakdown, breakdown) : undefined;

    return {
      amount: this.centsToAmount(amountInCents),
      breakdown,
      difference: difference && difference.length > 0 ? difference : undefined
    };
  }

  amountToCents(amount: string): number {
    const normalized = Number.parseFloat(amount);
    if (Number.isNaN(normalized) || normalized <= 0) {
      throw new Error('Betrag muss groesser als 0 sein.');
    }
    return Math.round(normalized * 100);
  }

  centsToAmount(cents: number): string {
    return (cents / 100).toFixed(2);
  }

  private calculateBreakdown(amountInCents: number): DenominationCountDto[] {
    let remaining = amountInCents;
    const result: DenominationCountDto[] = [];

    for (const denomination of EURO_DENOMINATIONS) {
      const denominationInCents = this.amountToCents(denomination);
      const count = Math.floor(remaining / denominationInCents);
      if (count > 0) {
        result.push({ denomination, count });
        remaining -= count * denominationInCents;
      }
    }

    return result;
  }

  private calculateDifference(
    previous: DenominationCountDto[],
    current: DenominationCountDto[]
  ): DifferenceDto[] {
    const previousMap = this.toCountMap(previous);
    const currentMap = this.toCountMap(current);
    const differences: DifferenceDto[] = [];

    for (const denomination of EURO_DENOMINATIONS) {
      const previousCount = previousMap.get(denomination) ?? 0;
      const currentCount = currentMap.get(denomination) ?? 0;
      if (previousCount !== 0 || currentCount !== 0) {
        differences.push({
          denomination,
          delta: currentCount - previousCount
        });
      }
    }

    return differences;
  }

  toCountMap(breakdown: DenominationCountDto[]): Map<string, number> {
    const result = new Map<string, number>();
    for (const row of breakdown) {
      result.set(row.denomination, row.count);
    }
    return result;
  }
}

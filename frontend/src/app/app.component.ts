import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SliderModule } from 'primeng/slider';
import { TableModule } from 'primeng/table';

import { EURO_DENOMINATIONS } from './constants/denominations';
import { CalculationMode, CalculateResponseDto, CalculationDto, DenominationCountDto } from './models/calculation.models';
import { CalculationApiService } from './services/calculation-api.service';
import { LocalBreakdownService } from './services/local-breakdown.service';

interface ModeOption {
  label: string;
  value: CalculationMode;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InputNumberModule,
    SelectButtonModule,
    ButtonModule,
    TableModule,
    SliderModule,
    CardModule,
    MessageModule
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  // Fixed denomination order for calculation and table/slider rendering.
  readonly denominations: string[] = [...EURO_DENOMINATIONS];
  readonly modeOptions: ModeOption[] = [
    { label: 'Frontend', value: 'frontend' },
    { label: 'Backend', value: 'backend' }
  ];

  amount: number | null = null;
  mode: CalculationMode = 'frontend';
  loading = false;
  errorMessage = '';
  infoMessage = '';

  currentResult: CalculateResponseDto | null = null;
  private previousFrontend: CalculationDto | null = null;
  private previousBackend: CalculationDto | null = null;

  manualCounts: Record<string, number> = {};

  private readonly api = inject(CalculationApiService);
  private readonly localBreakdownService = inject(LocalBreakdownService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly euroFormatter = new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR'
  });

  async onModeChange(nextMode: CalculationMode): Promise<void> {
    const oldMode = this.mode;
    if (oldMode === nextMode) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.infoMessage = '';

    try {
      // Keep both modes in sync: when leaving backend, pull the persisted state into frontend.
      if (oldMode === 'backend' && nextMode === 'frontend') {
        const storedCalculation = await this.api.getStoredCalculation();
        this.previousFrontend = storedCalculation;
        if (storedCalculation) {
          this.infoMessage = 'Vorherige Backend-Berechnung wurde in den Frontend-Modus uebernommen.';
        } else {
          this.infoMessage = 'Keine gespeicherte Backend-Berechnung vorhanden.';
        }
      }

      // When entering backend, persist the latest frontend state as backend baseline.
      if (oldMode === 'frontend' && nextMode === 'backend' && this.previousFrontend) {
        await this.api.storeCalculation(this.previousFrontend);
        this.previousBackend = this.previousFrontend;
        this.infoMessage = 'Vorherige Frontend-Berechnung wurde im Backend gespeichert.';
      }

      this.mode = nextMode;
    } catch (error: unknown) {
      this.errorMessage = this.toErrorMessage(error);
    } finally {
      this.loading = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  async calculate(): Promise<void> {
    if (!this.isAmountValid()) {
      this.errorMessage = 'Bitte einen gueltigen Betrag > 0 eingeben.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.infoMessage = '';

    try {
      const amount = this.numberToAmount(this.amount as number);

      // Backend mode delegates full calculation (including difference) to the API.
      if (this.mode === 'backend') {
        const response = await this.api.calculate({ amount });
        this.currentResult = response;
        this.previousBackend = {
          amount: response.amount,
          breakdown: response.breakdown
        };
      } else {
        // Frontend mode computes locally against the previous frontend snapshot.
        const response = this.localBreakdownService.calculate(amount, this.previousFrontend);
        this.currentResult = response;
        this.previousFrontend = {
          amount: response.amount,
          breakdown: response.breakdown
        };
      }

      this.initializeManualCounts();
    } catch (error: unknown) {
      this.errorMessage = this.toErrorMessage(error);
    } finally {
      this.loading = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  async applyManualBreakdown(): Promise<void> {
    if (!this.currentResult || !this.canApplyManualBreakdown()) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.infoMessage = '';

    try {
      const updatedCalculation = this.buildCalculationFromManualCounts();

      // Manual changes become the new baseline in the active mode.
      if (this.mode === 'backend') {
        await this.api.storeCalculation(updatedCalculation);
        this.previousBackend = updatedCalculation;
      } else {
        this.previousFrontend = updatedCalculation;
      }

      this.currentResult = {
        amount: updatedCalculation.amount,
        breakdown: updatedCalculation.breakdown
      };

      this.infoMessage = 'Manuelle Stückelung wurde übernommen.';
      this.initializeManualCounts();
    } catch (error: unknown) {
      this.errorMessage = this.toErrorMessage(error);
    } finally {
      this.loading = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  isAmountValid(): boolean {
    return this.amount !== null && Number.isFinite(this.amount) && this.amount > 0;
  }

  hasResult(): boolean {
    return this.currentResult !== null;
  }

  getRemainingAmountText(): string {
    return this.euroFormatter.format(this.getRemainingInCents() / 100);
  }

  getManualCount(denomination: string): number {
    return this.manualCounts[denomination] ?? 0;
  }

  onSliderChange(denomination: string, nextCount: number): void {
    const normalizedCount = Math.max(0, Math.floor(nextCount));
    const max = this.getSliderMax(denomination);
    this.manualCounts[denomination] = Math.min(normalizedCount, max);
  }

  getSliderMax(denomination: string): number {
    const currentCount = this.getManualCount(denomination);
    return currentCount + this.getAdditionalPossibleCount(denomination);
  }

  getAdditionalPossibleCount(denomination: string): number {
    const denominationInCents = this.localBreakdownService.amountToCents(denomination);
    const remaining = this.getRemainingInCents();
    if (remaining <= 0) {
      return 0;
    }
    return Math.floor(remaining / denominationInCents);
  }

  canApplyManualBreakdown(): boolean {
    return this.hasResult() && this.getRemainingInCents() === 0 && this.manualCountsChanged();
  }

  toTestId(value: string): string {
    return value.replace('.', '-');
  }

  private initializeManualCounts(): void {
    if (!this.currentResult) {
      this.manualCounts = {};
      return;
    }

    const countMap = this.localBreakdownService.toCountMap(this.currentResult.breakdown);
    const nextCounts: Record<string, number> = {};
    for (const denomination of this.denominations) {
      nextCounts[denomination] = countMap.get(denomination) ?? 0;
    }
    this.manualCounts = nextCounts;
  }

  private getRemainingInCents(): number {
    if (!this.currentResult) {
      return 0;
    }

    const amountInCents = this.localBreakdownService.amountToCents(this.currentResult.amount);
    // Remaining = target amount - sum(current slider selection).
    const usedInCents = this.denominations
      .map((denomination) => this.localBreakdownService.amountToCents(denomination) * this.getManualCount(denomination))
      .reduce((sum, value) => sum + value, 0);

    return amountInCents - usedInCents;
  }

  private manualCountsChanged(): boolean {
    if (!this.currentResult) {
      return false;
    }

    const currentMap = this.localBreakdownService.toCountMap(this.currentResult.breakdown);
    return this.denominations.some((denomination) => (currentMap.get(denomination) ?? 0) !== this.getManualCount(denomination));
  }

  private buildCalculationFromManualCounts(): CalculationDto {
    // Persist only non-zero rows to match backend/frontend DTO expectations.
    const breakdown: DenominationCountDto[] = this.denominations
      .map((denomination) => ({
        denomination,
        count: this.getManualCount(denomination)
      }))
      .filter((row) => row.count > 0);

    return {
      amount: this.currentResult!.amount,
      breakdown
    };
  }

  private numberToAmount(value: number): string {
    return value.toFixed(2);
  }

  private toErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const apiMessage = typeof error.error?.message === 'string' ? error.error.message : '';
      return apiMessage || `API Fehler (${error.status}).`;
    }
    if (error instanceof Error) {
      return error.message;
    }
    return 'Unbekannter Fehler.';
  }
}

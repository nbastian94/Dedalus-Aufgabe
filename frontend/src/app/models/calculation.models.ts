export interface DenominationCountDto {
  denomination: string;
  count: number;
}

export interface DifferenceDto {
  denomination: string;
  delta: number;
}

export interface CalculationDto {
  amount: string;
  breakdown: DenominationCountDto[];
}

export interface CalculateRequestDto {
  amount: string;
}

export interface CalculateResponseDto extends CalculationDto {
  difference?: DifferenceDto[];
}

export type CalculationMode = 'frontend' | 'backend';

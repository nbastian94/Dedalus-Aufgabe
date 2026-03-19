import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { CalculateRequestDto, CalculateResponseDto, CalculationDto } from '../models/calculation.models';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class CalculationApiService {
  private readonly httpClient = inject(HttpClient);

  async calculate(request: CalculateRequestDto): Promise<CalculateResponseDto> {
    return firstValueFrom(
      this.httpClient.post<CalculateResponseDto>(`${API_BASE_URL}/calculate`, request)
    );
  }

  async getStoredCalculation(): Promise<CalculationDto | null> {
    const response: HttpResponse<CalculationDto> = await firstValueFrom(
      this.httpClient.get<CalculationDto>(`${API_BASE_URL}/calculations`, { observe: 'response' })
    );
    if (response.status === 204 || !response.body) {
      return null;
    }
    return response.body;
  }

  async storeCalculation(calculation: CalculationDto): Promise<void> {
    await firstValueFrom(
      this.httpClient.post<void>(`${API_BASE_URL}/calculations`, calculation)
    );
  }
}

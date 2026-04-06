import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

export interface DashboardSummary {
  requestCountByStatus: Record<string, number>;
  assetCountByStatus: Record<string, number>;
}

export interface RequestTrend {
  month: string;
  count: number;
}

export interface StatusCount {
  status: string;
  count: number;
}

export interface SlaAgingBucket {
  bucket: string;
  count: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly baseUrl = `${environment.apiUrl}/api/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<ApiResponse<DashboardSummary>>(`${this.baseUrl}/summary`).pipe(map(r => r.data));
  }

  getRequestTrends(): Observable<RequestTrend[]> {
    return this.http.get<ApiResponse<RequestTrend[]>>(`${this.baseUrl}/request-trends`).pipe(map(r => r.data));
  }

  getAssetsByStatus(): Observable<StatusCount[]> {
    return this.http.get<ApiResponse<StatusCount[]>>(`${this.baseUrl}/assets-by-status`).pipe(map(r => r.data));
  }

  getSlaAging(): Observable<SlaAgingBucket[]> {
    return this.http.get<ApiResponse<SlaAgingBucket[]>>(`${this.baseUrl}/sla-aging`).pipe(map(r => r.data));
  }
}

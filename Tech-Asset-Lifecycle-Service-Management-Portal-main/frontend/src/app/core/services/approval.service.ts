import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api-response.model';

export interface ApprovalStep {
  id: number;
  requestId: number;
  requestNumber: string;
  requestTitle: string;
  approverId: number;
  approverName: string;
  requesterId: number;
  requesterName: string;
  decision: 'PENDING' | 'APPROVED' | 'REJECTED';
  comment: string | null;
  decidedAt: string | null;
  sequenceOrder: number;
  createdAt: string;
}

export interface ApprovalActionRequest {
  comment?: string;
}

@Injectable({ providedIn: 'root' })
export class ApprovalService {
  private readonly baseUrl = `${environment.apiUrl}/api`;

  constructor(private readonly http: HttpClient) {}

  getPendingApprovals(page = 0, size = 20): Observable<ApiResponse<PagedResponse<ApprovalStep>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PagedResponse<ApprovalStep>>>(`${this.baseUrl}/approvals/pending`, { params });
  }

  approve(requestId: number, request: ApprovalActionRequest): Observable<ApprovalStep> {
    return this.http
      .post<ApiResponse<ApprovalStep>>(`${this.baseUrl}/requests/${requestId}/approve`, request)
      .pipe(map(r => r.data));
  }

  reject(requestId: number, request: ApprovalActionRequest): Observable<ApprovalStep> {
    return this.http
      .post<ApiResponse<ApprovalStep>>(`${this.baseUrl}/requests/${requestId}/reject`, request)
      .pipe(map(r => r.data));
  }

  getApprovalHistory(requestId: number): Observable<ApprovalStep[]> {
    return this.http
      .get<ApiResponse<ApprovalStep[]>>(`${this.baseUrl}/requests/${requestId}/approvals`)
      .pipe(map(r => r.data));
  }
}

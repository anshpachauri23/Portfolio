import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import {
  ServiceRequest, RequestComment, RequestType,
  CreateServiceRequestRequest, UpdateRequestStatusRequest, AddCommentRequest,
} from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  private readonly baseUrl = `${environment.apiUrl}/api/requests`;
  private readonly typesUrl = `${environment.apiUrl}/api/request-types`;

  constructor(private readonly http: HttpClient) {}

  getMyRequests(page = 0, size = 20): Observable<ApiResponse<PagedResponse<ServiceRequest>>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<PagedResponse<ServiceRequest>>>(`${this.baseUrl}/my`, { params });
  }

  getQueue(statuses: string[] = [], page = 0, size = 20): Observable<ApiResponse<PagedResponse<ServiceRequest>>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'createdAt,desc');
    statuses.forEach(s => { params = params.append('status', s); });
    return this.http.get<ApiResponse<PagedResponse<ServiceRequest>>>(`${this.baseUrl}/queue`, { params });
  }

  getRequest(id: number): Observable<ServiceRequest> {
    return this.http
      .get<ApiResponse<ServiceRequest>>(`${this.baseUrl}/${id}`)
      .pipe(map(r => r.data));
  }

  createRequest(request: CreateServiceRequestRequest): Observable<ServiceRequest> {
    return this.http
      .post<ApiResponse<ServiceRequest>>(this.baseUrl, request)
      .pipe(map(r => r.data));
  }

  updateStatus(id: number, request: UpdateRequestStatusRequest): Observable<ServiceRequest> {
    return this.http
      .patch<ApiResponse<ServiceRequest>>(`${this.baseUrl}/${id}/status`, request)
      .pipe(map(r => r.data));
  }

  addComment(id: number, request: AddCommentRequest): Observable<RequestComment> {
    return this.http
      .post<ApiResponse<RequestComment>>(`${this.baseUrl}/${id}/comments`, request)
      .pipe(map(r => r.data));
  }

  getActiveRequestTypes(): Observable<RequestType[]> {
    return this.http
      .get<ApiResponse<RequestType[]>>(this.typesUrl)
      .pipe(map(r => r.data));
  }
}

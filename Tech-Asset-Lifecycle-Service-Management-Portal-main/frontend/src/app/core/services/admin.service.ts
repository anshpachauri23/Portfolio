import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api-response.model';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  employeeCode: string | null;
  role: string;
  departmentId: number | null;
  departmentName: string | null;
  managerId: number | null;
  managerName: string | null;
  active: boolean;
  createdAt: string;
}

export interface UserCreateRequest {
  name: string;
  email: string;
  employeeCode?: string;
  password: string;
  role: string;
  departmentId?: number;
  managerId?: number;
}

export interface UserUpdateRequest {
  name: string;
  email: string;
  employeeCode?: string;
  role?: string;
  departmentId?: number;
  managerId?: number;
}

export interface AdminRequestType {
  id: number;
  name: string;
  description: string | null;
  approvalRequired: boolean;
  active: boolean;
  createdAt: string;
}

export interface RequestTypeRequest {
  name: string;
  description?: string;
  approvalRequired: boolean;
}

export interface Department {
  id: number;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly usersUrl = `${environment.apiUrl}/api/admin/users`;
  private readonly typesUrl = `${environment.apiUrl}/api/admin/request-types`;

  constructor(private readonly http: HttpClient) {}

  getUsers(page = 0, size = 20): Observable<ApiResponse<PagedResponse<AdminUser>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PagedResponse<AdminUser>>>(this.usersUrl, { params });
  }

  getUser(id: number): Observable<AdminUser> {
    return this.http.get<ApiResponse<AdminUser>>(`${this.usersUrl}/${id}`).pipe(map(r => r.data));
  }

  createUser(request: UserCreateRequest): Observable<AdminUser> {
    return this.http.post<ApiResponse<AdminUser>>(this.usersUrl, request).pipe(map(r => r.data));
  }

  updateUser(id: number, request: UserUpdateRequest): Observable<AdminUser> {
    return this.http.put<ApiResponse<AdminUser>>(`${this.usersUrl}/${id}`, request).pipe(map(r => r.data));
  }

  deactivateUser(id: number): Observable<AdminUser> {
    return this.http.patch<ApiResponse<AdminUser>>(`${this.usersUrl}/${id}/deactivate`, {}).pipe(map(r => r.data));
  }

  getRequestTypes(): Observable<AdminRequestType[]> {
    return this.http.get<ApiResponse<AdminRequestType[]>>(this.typesUrl).pipe(map(r => r.data));
  }

  createRequestType(request: RequestTypeRequest): Observable<AdminRequestType> {
    return this.http.post<ApiResponse<AdminRequestType>>(this.typesUrl, request).pipe(map(r => r.data));
  }

  updateRequestType(id: number, request: RequestTypeRequest): Observable<AdminRequestType> {
    return this.http.put<ApiResponse<AdminRequestType>>(`${this.typesUrl}/${id}`, request).pipe(map(r => r.data));
  }

  toggleRequestTypeActive(id: number): Observable<AdminRequestType> {
    return this.http.patch<ApiResponse<AdminRequestType>>(`${this.typesUrl}/${id}/toggle-active`, {}).pipe(map(r => r.data));
  }
}

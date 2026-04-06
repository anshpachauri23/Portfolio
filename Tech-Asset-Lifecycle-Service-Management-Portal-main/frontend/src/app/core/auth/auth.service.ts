import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { JwtPayload, Role, User } from '../models/user.model';
import { ApiResponse } from '../models/api-response.model';

interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/api/auth`;

  // In-memory only — intentional per spec. Token is lost on page refresh.
  private readonly tokenSubject = new BehaviorSubject<string | null>(null);

  readonly currentUser$: Observable<User | null> = this.tokenSubject.asObservable().pipe(
    map(token => (token ? this.decodeToken(token) : null)),
  );

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${this.apiUrl}/login`, credentials)
      .pipe(
        map(r => r.data),
        tap(data => this.tokenSubject.next(data.token)),
      );
  }

  logout(): void {
    this.tokenSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.tokenSubject.getValue();
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodeRawToken(token);
      return Date.now() / 1000 < payload.exp;
    } catch {
      return false;
    }
  }

  hasAnyRole(...roles: Role[]): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodeRawToken(token);
      return roles.includes(payload.role);
    } catch {
      return false;
    }
  }

  private decodeToken(token: string): User {
    const payload = this.decodeRawToken(token);
    return {
      id: payload.userId,
      name: payload.sub,
      email: payload.sub,
      employeeCode: null,
      role: payload.role,
      departmentId: null,
      departmentName: null,
    };
  }

  private decodeRawToken(token: string): JwtPayload {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64)) as JwtPayload;
  }
}

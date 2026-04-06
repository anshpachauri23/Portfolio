import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, map, switchMap, timer, takeUntil } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api-response.model';

export interface Notification {
  id: number;
  title: string;
  body: string | null;
  read: boolean;
  entityType: string | null;
  entityId: string | null;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private readonly baseUrl = `${environment.apiUrl}/api/notifications`;
  private readonly destroy$ = new Subject<void>();
  private readonly unreadCount$ = new BehaviorSubject<number>(0);

  readonly unreadCount: Observable<number> = this.unreadCount$.asObservable();

  constructor(private readonly http: HttpClient) {}

  startPolling(): void {
    timer(0, 60000)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.fetchUnreadCount()),
      )
      .subscribe({
        next: count => this.unreadCount$.next(count),
        error: () => {},
      });
  }

  stopPolling(): void {
    this.destroy$.next();
  }

  getNotifications(page = 0, size = 20): Observable<ApiResponse<PagedResponse<Notification>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PagedResponse<Notification>>>(this.baseUrl, { params });
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http
      .patch<ApiResponse<Notification>>(`${this.baseUrl}/${id}/read`, {})
      .pipe(
        map(r => r.data),
      );
  }

  markAllAsRead(): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${this.baseUrl}/read-all`, {})
      .pipe(map(() => {
        this.unreadCount$.next(0);
      }));
  }

  private fetchUnreadCount(): Observable<number> {
    return this.http
      .get<ApiResponse<number>>(`${this.baseUrl}/unread-count`)
      .pipe(map(r => r.data));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

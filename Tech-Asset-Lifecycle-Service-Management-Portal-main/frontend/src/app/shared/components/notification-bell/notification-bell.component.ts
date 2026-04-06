import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { Subject, takeUntil } from 'rxjs';
import { NotificationService, Notification } from '../../../core/services/notification.service';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatButtonModule, MatBadgeModule, MatMenuModule, MatDividerModule],
  template: `
    <button mat-icon-button [matMenuTriggerFor]="notifMenu"
            [matBadge]="unreadCount > 0 ? unreadCount : null"
            matBadgeColor="warn" matBadgeSize="small">
      <mat-icon>notifications</mat-icon>
    </button>

    <mat-menu #notifMenu="matMenu" class="notification-menu">
      <div style="padding: 8px 16px; display: flex; justify-content: space-between; align-items: center; min-width: 320px;">
        <strong>Notifications</strong>
        @if (unreadCount > 0) {
          <button mat-button color="primary" (click)="markAllRead($event)">Mark all read</button>
        }
      </div>
      <mat-divider />

      @if (notifications.length === 0) {
        <div style="padding: 16px; text-align: center; color: #999;">No notifications</div>
      }

      @for (n of notifications; track n.id) {
        <button mat-menu-item (click)="onNotificationClick(n)"
                [style.background-color]="n.read ? 'transparent' : '#e3f2fd'">
          <div style="white-space: normal; max-width: 300px;">
            <div style="font-weight: 500;">{{ n.title }}</div>
            @if (n.body) {
              <div style="font-size: 12px; color: #666;">{{ n.body }}</div>
            }
            <div style="font-size: 11px; color: #999;">{{ n.createdAt | date:'short' }}</div>
          </div>
        </button>
      }
    </mat-menu>
  `,
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount = 0;
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.startPolling();
    this.notificationService.unreadCount
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => {
        this.unreadCount = count;
        if (count > 0) {
          this.loadNotifications();
        }
      });
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.notificationService.stopPolling();
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications(0, 10).subscribe({
      next: res => this.notifications = res.data.content,
      error: () => {},
    });
  }

  onNotificationClick(n: Notification): void {
    if (!n.read) {
      this.notificationService.markAsRead(n.id).subscribe({
        next: () => {
          n.read = true;
          if (this.unreadCount > 0) this.unreadCount--;
        },
      });
    }
  }

  markAllRead(event: Event): void {
    event.stopPropagation();
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
        this.unreadCount = 0;
      },
    });
  }
}

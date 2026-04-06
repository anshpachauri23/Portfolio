import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { BehaviorSubject, of } from 'rxjs';
import { NotificationBellComponent } from './notification-bell.component';
import { NotificationService, Notification } from '../../../core/services/notification.service';

describe('NotificationBellComponent (smoke)', () => {
  let fixture: ComponentFixture<NotificationBellComponent>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let unreadCount$: BehaviorSubject<number>;

  const mockNotificationsPage = {
    data: {
      content: [
        { id: 1, title: 'Asset Assigned', body: 'Laptop assigned to you', read: false,
          entityType: 'ASSET', entityId: '5', createdAt: '2026-01-01T10:00:00Z' },
      ] as Notification[],
      page: 0, size: 10, totalElements: 1, totalPages: 1,
    },
    message: 'ok',
    timestamp: '',
  };

  beforeEach(async () => {
    unreadCount$ = new BehaviorSubject<number>(3);

    notificationServiceSpy = jasmine.createSpyObj('NotificationService', [
      'getNotifications', 'markAsRead', 'markAllAsRead', 'startPolling', 'stopPolling',
    ], {
      unreadCount: unreadCount$.asObservable(),
    });
    notificationServiceSpy.getNotifications.and.returnValue(of(mockNotificationsPage));
    notificationServiceSpy.markAsRead.and.returnValue(of({ ...mockNotificationsPage.data.content[0], read: true }));
    notificationServiceSpy.markAllAsRead.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [NotificationBellComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationBellComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('subscribes to unreadCount from service', () => {
    expect(fixture.componentInstance.unreadCount).toBe(3);
  });

  it('shows unread badge when count > 0', () => {
    expect(fixture.componentInstance.unreadCount).toBeGreaterThan(0);
  });

  it('loads notifications when menu opens', () => {
    fixture.componentInstance.loadNotifications();
    expect(notificationServiceSpy.getNotifications).toHaveBeenCalled();
  });

  it('has notifications populated after load', () => {
    fixture.componentInstance.loadNotifications();
    fixture.detectChanges();
    expect(fixture.componentInstance.notifications.length).toBe(1);
    expect(fixture.componentInstance.notifications[0].title).toBe('Asset Assigned');
  });
});

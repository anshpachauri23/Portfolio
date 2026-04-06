import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { ApprovalQueueComponent } from './approval-queue.component';
import { ApprovalService } from '../../../core/services/approval.service';

describe('ApprovalQueueComponent (smoke)', () => {
  let fixture: ComponentFixture<ApprovalQueueComponent>;
  let approvalServiceSpy: jasmine.SpyObj<ApprovalService>;

  const emptyPage = {
    data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
    message: 'ok',
    timestamp: '',
  };

  beforeEach(async () => {
    approvalServiceSpy = jasmine.createSpyObj('ApprovalService', ['getPendingApprovals', 'approve', 'reject']);
    approvalServiceSpy.getPendingApprovals.and.returnValue(of(emptyPage));

    await TestBed.configureTestingModule({
      imports: [ApprovalQueueComponent, MatDialogModule, MatSnackBarModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: ApprovalService, useValue: approvalServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApprovalQueueComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('calls getPendingApprovals on init', () => {
    expect(approvalServiceSpy.getPendingApprovals).toHaveBeenCalledWith(0, 20);
  });

  it('shows empty state message when no approvals', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No pending approvals');
  });

  it('displays approval rows when data is present', () => {
    const pendingPage = {
      data: {
        content: [{
          id: 1, requestId: 10, requestNumber: 'SR-001', requestTitle: 'VPN Access',
          approverId: 2, approverName: 'Manager', requesterId: 3, requesterName: 'Employee',
          decision: 'PENDING' as const, comment: null, decidedAt: null, sequenceOrder: 1,
          createdAt: '2026-01-01T10:00:00Z',
        }],
        page: 0, size: 20, totalElements: 1, totalPages: 1,
      },
      message: 'ok',
      timestamp: '',
    };
    approvalServiceSpy.getPendingApprovals.and.returnValue(of(pendingPage));

    fixture.componentInstance.loadApprovals();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tr[mat-row]');
    expect(rows.length).toBe(1);
  });
});

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { ApprovalService, ApprovalStep } from '../../../core/services/approval.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-approval-queue',
  standalone: true,
  imports: [
    CommonModule, RouterModule, MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule, MatDialogModule, MatSnackBarModule,
    MatCardModule, MatProgressSpinnerModule, MatFormFieldModule,
    MatInputModule, FormsModule, StatusBadgeComponent,
  ],
  template: `
    <h2>Approval Queue</h2>

    @if (loading) {
      <mat-spinner diameter="40" />
    } @else {
      <table mat-table [dataSource]="approvals" class="mat-elevation-z2" style="width: 100%;">
        <ng-container matColumnDef="requestNumber">
          <th mat-header-cell *matHeaderCellDef>Request #</th>
          <td mat-cell *matCellDef="let row">
            <a [routerLink]="['/dashboard/requests', row.requestId]">{{ row.requestNumber }}</a>
          </td>
        </ng-container>

        <ng-container matColumnDef="requestTitle">
          <th mat-header-cell *matHeaderCellDef>Title</th>
          <td mat-cell *matCellDef="let row">{{ row.requestTitle }}</td>
        </ng-container>

        <ng-container matColumnDef="requesterName">
          <th mat-header-cell *matHeaderCellDef>Requester</th>
          <td mat-cell *matCellDef="let row">{{ row.requesterName }}</td>
        </ng-container>

        <ng-container matColumnDef="createdAt">
          <th mat-header-cell *matHeaderCellDef>Submitted</th>
          <td mat-cell *matCellDef="let row">{{ row.createdAt | date:'short' }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let row">
            <button mat-raised-button color="primary" (click)="onApprove(row)" style="margin-right: 8px;">
              <mat-icon>check</mat-icon> Approve
            </button>
            <button mat-raised-button color="warn" (click)="onReject(row)">
              <mat-icon>close</mat-icon> Reject
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>

      @if (approvals.length === 0) {
        <p style="text-align: center; padding: 24px; color: #666;">No pending approvals.</p>
      }

      <mat-paginator [length]="totalElements" [pageSize]="pageSize"
                     (page)="onPage($event)" showFirstLastButtons />
    }
  `,
})
export class ApprovalQueueComponent implements OnInit {
  approvals: ApprovalStep[] = [];
  loading = true;
  totalElements = 0;
  pageSize = 20;
  currentPage = 0;

  readonly displayedColumns = ['requestNumber', 'requestTitle', 'requesterName', 'createdAt', 'actions'];

  constructor(
    private readonly approvalService: ApprovalService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadApprovals();
  }

  loadApprovals(): void {
    this.loading = true;
    this.approvalService.getPendingApprovals(this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.approvals = res.data.content;
        this.totalElements = res.data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load approvals', 'Close', { duration: 3000 });
      },
    });
  }

  onPage(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadApprovals();
  }

  onApprove(step: ApprovalStep): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Approve Request', message: `Approve request ${step.requestNumber}?`, confirmLabel: 'Approve' },
    });
    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.approvalService.approve(step.requestId, {}).subscribe({
        next: () => {
          this.snackBar.open('Request approved', 'Close', { duration: 3000 });
          this.loadApprovals();
        },
        error: err => this.snackBar.open(err?.error?.message || 'Failed to approve', 'Close', { duration: 3000 }),
      });
    });
  }

  onReject(step: ApprovalStep): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Reject Request', message: `Reject request ${step.requestNumber}?`, confirmLabel: 'Reject' },
    });
    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.approvalService.reject(step.requestId, {}).subscribe({
        next: () => {
          this.snackBar.open('Request rejected', 'Close', { duration: 3000 });
          this.loadApprovals();
        },
        error: err => this.snackBar.open(err?.error?.message || 'Failed to reject', 'Close', { duration: 3000 }),
      });
    });
  }
}

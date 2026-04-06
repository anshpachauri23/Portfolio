import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged, switchMap, startWith } from 'rxjs';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ServiceRequest, RequestStatus } from '../../../core/models/service-request.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-request-queue',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatTableModule, MatPaginatorModule,
    MatFormFieldModule, MatSelectModule,
    MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatSnackBarModule,
    StatusBadgeComponent,
  ],
  templateUrl: './request-queue.component.html',
})
export class RequestQueueComponent implements OnInit {
  requests: ServiceRequest[] = [];
  totalElements = 0;
  loading = false;
  currentPage = 0;

  displayedColumns = ['requestNumber', 'title', 'requestTypeName', 'requesterName', 'priority', 'status', 'createdAt', 'actions'];

  readonly queueStatuses: RequestStatus[] = [
    'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'IN_PROGRESS', 'WAITING_FOR_USER',
  ];

  readonly filterForm = this.fb.group({
    statuses: [[] as RequestStatus[]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly requestService: ServiceRequestService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.filterForm.valueChanges.pipe(
      startWith(this.filterForm.value),
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(filters => {
        this.loading = true;
        this.currentPage = 0;
        return this.requestService.getQueue(filters.statuses ?? [], 0);
      }),
    ).subscribe({
      next: r => {
        this.requests = r.data.content;
        this.totalElements = r.data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load queue', 'Dismiss', { duration: 3000 });
      },
    });
  }

  onPage(event: { pageIndex: number }): void {
    this.currentPage = event.pageIndex;
    this.loading = true;
    const statuses = this.filterForm.value.statuses ?? [];
    this.requestService.getQueue(statuses, event.pageIndex).subscribe({
      next: r => {
        this.requests = r.data.content;
        this.totalElements = r.data.totalElements;
        this.loading = false;
      },
    });
  }
}

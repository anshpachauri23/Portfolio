import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-my-requests',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatSnackBarModule,
    StatusBadgeComponent,
  ],
  templateUrl: './my-requests.component.html',
})
export class MyRequestsComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  requests: ServiceRequest[] = [];
  totalElements = 0;
  loading = false;
  displayedColumns = ['requestNumber', 'title', 'requestTypeName', 'priority', 'status', 'createdAt', 'actions'];

  constructor(
    private readonly requestService: ServiceRequestService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadRequests(0);
  }

  loadRequests(page: number): void {
    this.loading = true;
    this.requestService.getMyRequests(page).subscribe({
      next: r => {
        this.requests = r.data.content;
        this.totalElements = r.data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load requests', 'Dismiss', { duration: 3000 });
      },
    });
  }

  onPage(event: { pageIndex: number }): void {
    this.loadRequests(event.pageIndex);
  }
}

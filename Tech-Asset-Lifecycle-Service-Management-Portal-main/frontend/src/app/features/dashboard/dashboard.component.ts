import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import {
  DashboardService, DashboardSummary, RequestTrend, StatusCount, SlaAgingBucket,
} from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule, MatTableModule],
  template: `
    @if (loading) {
      <mat-spinner diameter="40" />
    } @else {
      <h2>Dashboard</h2>

      <div style="display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 24px;">
        @for (card of summaryCards; track card.label) {
          <mat-card style="flex: 1; min-width: 200px;">
            <mat-card-header>
              <mat-card-title>{{ card.value }}</mat-card-title>
              <mat-card-subtitle>{{ card.label }}</mat-card-subtitle>
            </mat-card-header>
          </mat-card>
        }
      </div>

      <div style="display: flex; gap: 24px; flex-wrap: wrap;">
        <mat-card style="flex: 1; min-width: 400px;">
          <mat-card-header>
            <mat-card-title>Request Trends (Last 6 Months)</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="trends" style="width: 100%;">
              <ng-container matColumnDef="month">
                <th mat-header-cell *matHeaderCellDef>Month</th>
                <td mat-cell *matCellDef="let row">{{ row.month }}</td>
              </ng-container>
              <ng-container matColumnDef="count">
                <th mat-header-cell *matHeaderCellDef>Requests</th>
                <td mat-cell *matCellDef="let row">{{ row.count }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="['month', 'count']"></tr>
              <tr mat-row *matRowDef="let row; columns: ['month', 'count']"></tr>
            </table>
          </mat-card-content>
        </mat-card>

        <mat-card style="flex: 1; min-width: 300px;">
          <mat-card-header>
            <mat-card-title>Assets by Status</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="assetsByStatus" style="width: 100%;">
              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Status</th>
                <td mat-cell *matCellDef="let row">{{ row.status }}</td>
              </ng-container>
              <ng-container matColumnDef="count">
                <th mat-header-cell *matHeaderCellDef>Count</th>
                <td mat-cell *matCellDef="let row">{{ row.count }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="['status', 'count']"></tr>
              <tr mat-row *matRowDef="let row; columns: ['status', 'count']"></tr>
            </table>
          </mat-card-content>
        </mat-card>

        <mat-card style="flex: 1; min-width: 300px;">
          <mat-card-header>
            <mat-card-title>SLA Aging</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            @if (slaAging.length === 0) {
              <p style="color: #666; padding: 16px;">No overdue requests.</p>
            } @else {
              <table mat-table [dataSource]="slaAging" style="width: 100%;">
                <ng-container matColumnDef="bucket">
                  <th mat-header-cell *matHeaderCellDef>Days Overdue</th>
                  <td mat-cell *matCellDef="let row">{{ row.bucket }}</td>
                </ng-container>
                <ng-container matColumnDef="count">
                  <th mat-header-cell *matHeaderCellDef>Count</th>
                  <td mat-cell *matCellDef="let row">{{ row.count }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="['bucket', 'count']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['bucket', 'count']"></tr>
              </table>
            }
          </mat-card-content>
        </mat-card>
      </div>
    }
  `,
})
export class DashboardComponent implements OnInit {
  loading = true;
  summary: DashboardSummary | null = null;
  trends: RequestTrend[] = [];
  assetsByStatus: StatusCount[] = [];
  slaAging: SlaAgingBucket[] = [];
  summaryCards: { label: string; value: number }[] = [];

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void {
    forkJoin({
      summary: this.dashboardService.getSummary(),
      trends: this.dashboardService.getRequestTrends(),
      assets: this.dashboardService.getAssetsByStatus(),
      sla: this.dashboardService.getSlaAging(),
    }).subscribe({
      next: ({ summary, trends, assets, sla }) => {
        this.summary = summary;
        this.trends = trends;
        this.assetsByStatus = assets;
        this.slaAging = sla;

        const totalRequests = Object.values(summary.requestCountByStatus).reduce((a, b) => a + b, 0);
        const totalAssets = Object.values(summary.assetCountByStatus).reduce((a, b) => a + b, 0);
        const openRequests = Object.entries(summary.requestCountByStatus)
          .filter(([s]) => !['CLOSED', 'REJECTED', 'COMPLETED'].includes(s))
          .reduce((a, [, v]) => a + v, 0);

        this.summaryCards = [
          { label: 'Total Requests', value: totalRequests },
          { label: 'Open Requests', value: openRequests },
          { label: 'Total Assets', value: totalAssets },
        ];

        this.loading = false;
      },
      error: () => this.loading = false,
    });
  }
}

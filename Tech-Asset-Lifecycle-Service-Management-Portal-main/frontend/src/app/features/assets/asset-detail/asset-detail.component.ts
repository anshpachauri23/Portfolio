import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { AssetService } from '../../../core/services/asset.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Asset, AssetHistory, ASSET_STATUS_TRANSITIONS, AssetStatus } from '../../../core/models/asset.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-asset-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatCardModule, MatButtonModule, MatIconModule, MatListModule,
    MatDividerModule, MatDialogModule, MatSelectModule,
    MatFormFieldModule, MatProgressSpinnerModule, MatSnackBarModule,
    StatusBadgeComponent,
  ],
  templateUrl: './asset-detail.component.html',
})
export class AssetDetailComponent implements OnInit {
  asset: Asset | null = null;
  history: AssetHistory[] = [];
  loading = true;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly assetService: AssetService,
    readonly authService: AuthService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      asset: this.assetService.getAsset(id),
      history: this.assetService.getHistory(id),
    }).subscribe({
      next: ({ asset, history }) => {
        this.asset = asset;
        this.history = history;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load asset', 'Dismiss', { duration: 3000 });
      },
    });
  }

  get validNextStatuses(): AssetStatus[] {
    if (!this.asset) return [];
    return ASSET_STATUS_TRANSITIONS[this.asset.status as AssetStatus] ?? [];
  }

  canManage(): boolean {
    return this.authService.hasAnyRole('ADMIN', 'TECHNICIAN');
  }

  changeStatus(newStatus: AssetStatus): void {
    if (!this.asset) return;
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Status Change',
        message: `Change status from ${this.asset.status} to ${newStatus}?`,
        confirmLabel: 'Change',
      },
    });

    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed || !this.asset) return;
      this.assetService.updateStatus(this.asset.id, { status: newStatus }).subscribe({
        next: updated => {
          this.asset = updated;
          this.assetService.getHistory(updated.id).subscribe(h => this.history = h);
          this.snackBar.open('Status updated', 'OK', { duration: 2000 });
        },
        error: err => {
          this.snackBar.open(err?.error?.message ?? 'Update failed', 'Dismiss', { duration: 4000 });
        },
      });
    });
  }
}

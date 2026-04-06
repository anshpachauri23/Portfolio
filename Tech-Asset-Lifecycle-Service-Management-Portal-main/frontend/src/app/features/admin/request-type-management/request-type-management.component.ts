import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { AdminService, AdminRequestType } from '../../../core/services/admin.service';

@Component({
  selector: 'app-request-type-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatTableModule, MatButtonModule,
    MatIconModule, MatSnackBarModule, MatFormFieldModule, MatInputModule,
    MatCheckboxModule, MatProgressSpinnerModule, MatCardModule, MatChipsModule,
  ],
  template: `
    <h2>Request Type Management</h2>

    <button mat-raised-button color="primary" (click)="showForm = !showForm" style="margin-bottom: 16px;">
      <mat-icon>{{ showForm ? 'close' : 'add' }}</mat-icon>
      {{ showForm ? 'Cancel' : 'Add Request Type' }}
    </button>

    @if (showForm) {
      <mat-card style="margin-bottom: 16px; padding: 16px;">
        <form [formGroup]="form" (ngSubmit)="save()">
          <div style="display: flex; gap: 16px; flex-wrap: wrap; align-items: center;">
            <mat-form-field style="flex: 1; min-width: 200px;">
              <mat-label>Name</mat-label>
              <input matInput formControlName="name">
            </mat-form-field>
            <mat-form-field style="flex: 2; min-width: 250px;">
              <mat-label>Description</mat-label>
              <input matInput formControlName="description">
            </mat-form-field>
            <mat-checkbox formControlName="approvalRequired">Approval Required</mat-checkbox>
          </div>
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || saving">
            {{ editingId ? 'Update' : 'Create' }}
          </button>
        </form>
      </mat-card>
    }

    @if (loading) {
      <mat-spinner diameter="40" />
    } @else {
      <table mat-table [dataSource]="types" class="mat-elevation-z2" style="width: 100%;">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Name</th>
          <td mat-cell *matCellDef="let row">{{ row.name }}</td>
        </ng-container>
        <ng-container matColumnDef="description">
          <th mat-header-cell *matHeaderCellDef>Description</th>
          <td mat-cell *matCellDef="let row">{{ row.description || '—' }}</td>
        </ng-container>
        <ng-container matColumnDef="approvalRequired">
          <th mat-header-cell *matHeaderCellDef>Approval Required</th>
          <td mat-cell *matCellDef="let row">
            <mat-icon [style.color]="row.approvalRequired ? 'orange' : '#ccc'">
              {{ row.approvalRequired ? 'verified' : 'remove_circle_outline' }}
            </mat-icon>
          </td>
        </ng-container>
        <ng-container matColumnDef="active">
          <th mat-header-cell *matHeaderCellDef>Active</th>
          <td mat-cell *matCellDef="let row">
            <mat-icon [style.color]="row.active ? 'green' : 'red'">
              {{ row.active ? 'check_circle' : 'cancel' }}
            </mat-icon>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let row">
            <button mat-icon-button (click)="edit(row)" title="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button (click)="toggleActive(row)"
                    [title]="row.active ? 'Deactivate' : 'Activate'">
              <mat-icon>{{ row.active ? 'toggle_on' : 'toggle_off' }}</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    }
  `,
})
export class RequestTypeManagementComponent implements OnInit {
  types: AdminRequestType[] = [];
  loading = true;
  saving = false;
  showForm = false;
  editingId: number | null = null;

  readonly displayedColumns = ['name', 'description', 'approvalRequired', 'active', 'actions'];

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    approvalRequired: [false],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly adminService: AdminService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadTypes();
  }

  loadTypes(): void {
    this.loading = true;
    this.adminService.getRequestTypes().subscribe({
      next: types => {
        this.types = types;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load request types', 'Close', { duration: 3000 });
      },
    });
  }

  edit(rt: AdminRequestType): void {
    this.editingId = rt.id;
    this.showForm = true;
    this.form.patchValue({
      name: rt.name,
      description: rt.description || '',
      approvalRequired: rt.approvalRequired,
    });
  }

  save(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;
    const val = this.form.getRawValue();

    const request = { name: val.name, description: val.description || undefined, approvalRequired: val.approvalRequired };

    const obs = this.editingId
      ? this.adminService.updateRequestType(this.editingId, request)
      : this.adminService.createRequestType(request);

    obs.subscribe({
      next: () => {
        this.snackBar.open(this.editingId ? 'Updated' : 'Created', 'Close', { duration: 3000 });
        this.resetForm();
        this.loadTypes();
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err?.error?.message || 'Failed', 'Close', { duration: 3000 });
      },
    });
  }

  toggleActive(rt: AdminRequestType): void {
    this.adminService.toggleRequestTypeActive(rt.id).subscribe({
      next: () => {
        this.snackBar.open(`Request type ${rt.active ? 'deactivated' : 'activated'}`, 'Close', { duration: 3000 });
        this.loadTypes();
      },
      error: err => this.snackBar.open(err?.error?.message || 'Failed', 'Close', { duration: 3000 }),
    });
  }

  private resetForm(): void {
    this.saving = false;
    this.showForm = false;
    this.editingId = null;
    this.form.reset({ name: '', description: '', approvalRequired: false });
  }
}

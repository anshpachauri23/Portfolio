import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { AdminService, AdminUser } from '../../../core/services/admin.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule, MatDialogModule, MatSnackBarModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatProgressSpinnerModule,
    MatChipsModule, MatCardModule,
  ],
  template: `
    <h2>User Management</h2>

    <button mat-raised-button color="primary" (click)="showForm = !showForm" style="margin-bottom: 16px;">
      <mat-icon>{{ showForm ? 'close' : 'person_add' }}</mat-icon>
      {{ showForm ? 'Cancel' : 'Add User' }}
    </button>

    @if (showForm) {
      <mat-card style="margin-bottom: 16px; padding: 16px;">
        <form [formGroup]="userForm" (ngSubmit)="saveUser()">
          <div style="display: flex; gap: 16px; flex-wrap: wrap;">
            <mat-form-field style="flex: 1; min-width: 200px;">
              <mat-label>Name</mat-label>
              <input matInput formControlName="name">
            </mat-form-field>
            <mat-form-field style="flex: 1; min-width: 200px;">
              <mat-label>Email</mat-label>
              <input matInput formControlName="email" type="email">
            </mat-form-field>
            <mat-form-field style="flex: 1; min-width: 150px;">
              <mat-label>Employee Code</mat-label>
              <input matInput formControlName="employeeCode">
            </mat-form-field>
          </div>
          <div style="display: flex; gap: 16px; flex-wrap: wrap;">
            @if (!editingId) {
              <mat-form-field style="flex: 1; min-width: 200px;">
                <mat-label>Password</mat-label>
                <input matInput formControlName="password" type="password">
              </mat-form-field>
            }
            <mat-form-field style="flex: 1; min-width: 150px;">
              <mat-label>Role</mat-label>
              <mat-select formControlName="role">
                @for (r of roles; track r) {
                  <mat-option [value]="r">{{ r }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
          </div>
          <button mat-raised-button color="primary" type="submit" [disabled]="userForm.invalid || saving">
            {{ editingId ? 'Update' : 'Create' }}
          </button>
        </form>
      </mat-card>
    }

    @if (loading) {
      <mat-spinner diameter="40" />
    } @else {
      <table mat-table [dataSource]="users" class="mat-elevation-z2" style="width: 100%;">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Name</th>
          <td mat-cell *matCellDef="let row">{{ row.name }}</td>
        </ng-container>
        <ng-container matColumnDef="email">
          <th mat-header-cell *matHeaderCellDef>Email</th>
          <td mat-cell *matCellDef="let row">{{ row.email }}</td>
        </ng-container>
        <ng-container matColumnDef="role">
          <th mat-header-cell *matHeaderCellDef>Role</th>
          <td mat-cell *matCellDef="let row">{{ row.role }}</td>
        </ng-container>
        <ng-container matColumnDef="department">
          <th mat-header-cell *matHeaderCellDef>Department</th>
          <td mat-cell *matCellDef="let row">{{ row.departmentName || '—' }}</td>
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
            <button mat-icon-button (click)="editUser(row)" title="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            @if (row.active) {
              <button mat-icon-button color="warn" (click)="deactivate(row)" title="Deactivate">
                <mat-icon>block</mat-icon>
              </button>
            }
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>

      <mat-paginator [length]="totalElements" [pageSize]="pageSize"
                     (page)="onPage($event)" showFirstLastButtons />
    }
  `,
})
export class UserManagementComponent implements OnInit {
  users: AdminUser[] = [];
  loading = true;
  saving = false;
  showForm = false;
  editingId: number | null = null;
  totalElements = 0;
  pageSize = 20;
  currentPage = 0;

  readonly roles = ['EMPLOYEE', 'TECHNICIAN', 'MANAGER', 'ADMIN'];
  readonly displayedColumns = ['name', 'email', 'role', 'department', 'active', 'actions'];

  userForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    employeeCode: [''],
    password: ['', Validators.required],
    role: ['EMPLOYEE', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly adminService: AdminService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getUsers(this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.users = res.data.content;
        this.totalElements = res.data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
      },
    });
  }

  onPage(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  editUser(user: AdminUser): void {
    this.editingId = user.id;
    this.showForm = true;
    this.userForm.patchValue({
      name: user.name,
      email: user.email,
      employeeCode: user.employeeCode || '',
      role: user.role,
    });
    this.userForm.controls.password.clearValidators();
    this.userForm.controls.password.updateValueAndValidity();
  }

  saveUser(): void {
    if (this.userForm.invalid || this.saving) return;
    this.saving = true;
    const val = this.userForm.getRawValue();

    if (this.editingId) {
      this.adminService.updateUser(this.editingId, {
        name: val.name, email: val.email, employeeCode: val.employeeCode || undefined, role: val.role,
      }).subscribe({
        next: () => {
          this.snackBar.open('User updated', 'Close', { duration: 3000 });
          this.resetForm();
          this.loadUsers();
        },
        error: err => {
          this.saving = false;
          this.snackBar.open(err?.error?.message || 'Failed to update', 'Close', { duration: 3000 });
        },
      });
    } else {
      this.adminService.createUser({
        name: val.name, email: val.email, employeeCode: val.employeeCode || undefined,
        password: val.password, role: val.role,
      }).subscribe({
        next: () => {
          this.snackBar.open('User created', 'Close', { duration: 3000 });
          this.resetForm();
          this.loadUsers();
        },
        error: err => {
          this.saving = false;
          this.snackBar.open(err?.error?.message || 'Failed to create', 'Close', { duration: 3000 });
        },
      });
    }
  }

  deactivate(user: AdminUser): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Deactivate User', message: `Deactivate ${user.name}?`, confirmLabel: 'Deactivate' },
    });
    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.adminService.deactivateUser(user.id).subscribe({
        next: () => {
          this.snackBar.open('User deactivated', 'Close', { duration: 3000 });
          this.loadUsers();
        },
        error: err => this.snackBar.open(err?.error?.message || 'Failed', 'Close', { duration: 3000 }),
      });
    });
  }

  private resetForm(): void {
    this.saving = false;
    this.showForm = false;
    this.editingId = null;
    this.userForm.reset({ name: '', email: '', employeeCode: '', password: '', role: 'EMPLOYEE' });
    this.userForm.controls.password.setValidators(Validators.required);
    this.userForm.controls.password.updateValueAndValidity();
  }
}

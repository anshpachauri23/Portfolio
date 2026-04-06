import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'assets', pathMatch: 'full' },
      {
        path: 'assets',
        loadComponent: () =>
          import('./features/assets/asset-list/asset-list.component').then(m => m.AssetListComponent),
      },
      {
        path: 'assets/new',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'TECHNICIAN'] },
        loadComponent: () =>
          import('./features/assets/asset-form/asset-form.component').then(m => m.AssetFormComponent),
      },
      {
        path: 'assets/:id',
        loadComponent: () =>
          import('./features/assets/asset-detail/asset-detail.component').then(m => m.AssetDetailComponent),
      },
      {
        path: 'assets/:id/edit',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'TECHNICIAN'] },
        loadComponent: () =>
          import('./features/assets/asset-form/asset-form.component').then(m => m.AssetFormComponent),
      },
      {
        path: 'requests',
        loadComponent: () =>
          import('./features/requests/my-requests/my-requests.component').then(m => m.MyRequestsComponent),
      },
      {
        path: 'requests/queue',
        canActivate: [roleGuard],
        data: { roles: ['TECHNICIAN', 'MANAGER', 'ADMIN'] },
        loadComponent: () =>
          import('./features/requests/request-queue/request-queue.component').then(m => m.RequestQueueComponent),
      },
      {
        path: 'requests/new',
        loadComponent: () =>
          import('./features/requests/request-form/request-form.component').then(m => m.RequestFormComponent),
      },
      {
        path: 'requests/:id',
        loadComponent: () =>
          import('./features/requests/request-detail/request-detail.component').then(m => m.RequestDetailComponent),
      },
      {
        path: 'approvals',
        canActivate: [roleGuard],
        data: { roles: ['MANAGER'] },
        loadComponent: () =>
          import('./features/approvals/approval-queue/approval-queue.component').then(m => m.ApprovalQueueComponent),
      },
      {
        path: 'overview',
        canActivate: [roleGuard],
        data: { roles: ['TECHNICIAN', 'MANAGER', 'ADMIN'] },
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'admin/users',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('./features/admin/user-management/user-management.component').then(m => m.UserManagementComponent),
      },
      {
        path: 'admin/request-types',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('./features/admin/request-type-management/request-type-management.component').then(m => m.RequestTypeManagementComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

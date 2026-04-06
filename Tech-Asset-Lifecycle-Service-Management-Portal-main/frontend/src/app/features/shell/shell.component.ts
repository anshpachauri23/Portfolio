import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { User } from '../../core/models/user.model';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    NotificationBellComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.scss'],
})
export class ShellComponent implements OnInit {
  currentUser$!: Observable<User | null>;

  readonly navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard/overview', roles: ['TECHNICIAN', 'MANAGER', 'ADMIN'] },
    { label: 'Assets', icon: 'devices', route: '/dashboard/assets' },
    { label: 'My Requests', icon: 'assignment', route: '/dashboard/requests' },
    { label: 'Request Queue', icon: 'queue', route: '/dashboard/requests/queue', roles: ['TECHNICIAN', 'MANAGER', 'ADMIN'] },
    { label: 'Approvals', icon: 'approval', route: '/dashboard/approvals', roles: ['MANAGER'] },
    { label: 'New Request', icon: 'add_circle', route: '/dashboard/requests/new' },
    { label: 'Users', icon: 'people', route: '/dashboard/admin/users', roles: ['ADMIN'] },
    { label: 'Request Types', icon: 'category', route: '/dashboard/admin/request-types', roles: ['ADMIN'] },
  ];

  constructor(readonly authService: AuthService) {}

  ngOnInit(): void {
    this.currentUser$ = this.authService.currentUser$;
  }

  visibleNavItems(user: User | null): NavItem[] {
    if (!user) return [];
    return this.navItems.filter(item =>
      !item.roles || item.roles.includes(user.role)
    );
  }

  logout(): void {
    this.authService.logout();
  }
}

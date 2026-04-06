import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { Role } from '../models/user.model';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const allowedRoles: Role[] = route.data['roles'] ?? [];
  if (allowedRoles.length === 0 || authService.hasAnyRole(...allowedRoles)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};

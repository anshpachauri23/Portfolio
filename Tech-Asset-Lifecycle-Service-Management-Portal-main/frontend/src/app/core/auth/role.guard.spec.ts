import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, Router, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { roleGuard } from './role.guard';
import { AuthService } from './auth.service';
import { Role } from '../models/user.model';

describe('roleGuard', () => {
  let authService: AuthService;
  let router: Router;

  const executeGuard = (roles: Role[] = []) => {
    const route = { data: { roles } } as unknown as ActivatedRouteSnapshot;
    return TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('redirects to /login when not authenticated', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(false);

    const result = executeGuard(['ADMIN']);

    expect(result).not.toBeTrue();
    expect((result as ReturnType<Router['createUrlTree']>).toString()).toBe('/login');
  });

  it('returns true when authenticated and user has allowed role', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'hasAnyRole').and.returnValue(true);

    expect(executeGuard(['MANAGER'])).toBeTrue();
  });

  it('redirects to /dashboard when authenticated but role not allowed', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    spyOn(authService, 'hasAnyRole').and.returnValue(false);

    const result = executeGuard(['ADMIN']);

    expect(result).not.toBeTrue();
    expect((result as ReturnType<Router['createUrlTree']>).toString()).toBe('/dashboard');
  });

  it('returns true when no roles specified (open to any authenticated user)', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    const hasAnyRoleSpy = spyOn(authService, 'hasAnyRole');

    const result = executeGuard([]);

    expect(result).toBeTrue();
    expect(hasAnyRoleSpy).not.toHaveBeenCalled();
  });
});

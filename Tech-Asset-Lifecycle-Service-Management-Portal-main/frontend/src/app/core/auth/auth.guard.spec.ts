import { TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  let authService: AuthService;
  let router: Router;

  const executeGuard = () =>
    TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

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

  it('returns true when user is authenticated', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(true);
    expect(executeGuard()).toBeTrue();
  });

  it('redirects to /login when not authenticated', () => {
    spyOn(authService, 'isLoggedIn').and.returnValue(false);
    const result = executeGuard();
    expect(result).not.toBeTrue();
    expect((result as ReturnType<Router['createUrlTree']>).toString()).toBe('/login');
  });
});

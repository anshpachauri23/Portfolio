import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';

const MOCK_JWT =
  'eyJhbGciOiJIUzI1NiJ9.' +
  btoa(JSON.stringify({ sub: 'alice@company.com', userId: 1, role: 'ADMIN', iat: 1000, exp: 9999999999 })).replace(/=/g, '') +
  '.signature';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('login stores token in memory (not localStorage)', () => {
    service.login({ email: 'alice@company.com', password: 'Password1!' }).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush({ data: { token: MOCK_JWT, userId: 1, name: 'Alice', email: 'alice@company.com', role: 'ADMIN' }, message: 'ok', timestamp: '' });

    expect(service.getToken()).toBe(MOCK_JWT);
    expect(localStorage.getItem('token')).toBeNull();
    expect(sessionStorage.getItem('token')).toBeNull();
  });

  it('logout clears token', () => {
    service.login({ email: 'alice@company.com', password: 'Password1!' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush({ data: { token: MOCK_JWT, userId: 1, name: 'Alice', email: 'alice@company.com', role: 'ADMIN' }, message: 'ok', timestamp: '' });

    service.logout();
    expect(service.getToken()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('currentUser$ emits decoded user after login', (done) => {
    service.login({ email: 'alice@company.com', password: 'Password1!' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush({ data: { token: MOCK_JWT, userId: 1, name: 'Alice', email: 'alice@company.com', role: 'ADMIN' }, message: 'ok', timestamp: '' });

    service.currentUser$.subscribe(user => {
      if (user) {
        expect(user.role).toBe('ADMIN');
        expect(user.id).toBe(1);
        done();
      }
    });
  });

  it('isLoggedIn returns false before login', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('hasAnyRole returns true for matching role', () => {
    service.login({ email: 'alice@company.com', password: 'Password1!' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush({ data: { token: MOCK_JWT, userId: 1, name: 'Alice', email: 'alice@company.com', role: 'ADMIN' }, message: 'ok', timestamp: '' });

    expect(service.hasAnyRole('ADMIN')).toBeTrue();
    expect(service.hasAnyRole('EMPLOYEE')).toBeFalse();
  });
});

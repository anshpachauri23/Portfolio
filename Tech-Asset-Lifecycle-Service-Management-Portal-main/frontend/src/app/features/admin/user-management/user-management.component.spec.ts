import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { UserManagementComponent } from './user-management.component';
import { AdminService } from '../../../core/services/admin.service';

describe('UserManagementComponent (smoke)', () => {
  let fixture: ComponentFixture<UserManagementComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;

  const emptyPage = {
    data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
    message: 'ok',
    timestamp: '',
  };

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'getUsers', 'createUser', 'updateUser', 'deactivateUser',
    ]);
    adminServiceSpy.getUsers.and.returnValue(of(emptyPage));

    await TestBed.configureTestingModule({
      imports: [UserManagementComponent, MatDialogModule, MatSnackBarModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: AdminService, useValue: adminServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('calls getUsers on init', () => {
    expect(adminServiceSpy.getUsers).toHaveBeenCalledWith(0, 20);
  });

  it('shows Add User button', () => {
    const button = fixture.nativeElement.querySelector('button');
    expect(button.textContent).toContain('Add User');
  });

  it('form is not shown by default', () => {
    expect(fixture.componentInstance.showForm).toBeFalse();
    const form = fixture.nativeElement.querySelector('form');
    expect(form).toBeNull();
  });

  it('shows form when showForm is toggled', () => {
    fixture.componentInstance.showForm = true;
    fixture.detectChanges();
    expect(fixture.componentInstance.showForm).toBeTrue();
    expect(fixture.componentInstance.userForm).toBeTruthy();
  });

  it('userForm has required validators on name, email, and password', () => {
    const form = fixture.componentInstance.userForm;
    expect(form.controls.name.valid).toBeFalse();
    expect(form.controls.email.valid).toBeFalse();
    expect(form.controls.password.valid).toBeFalse();
  });

  it('userForm is valid when all required fields are filled', () => {
    const form = fixture.componentInstance.userForm;
    form.setValue({ name: 'Alice', email: 'alice@company.com', employeeCode: 'E001', password: 'pass', role: 'EMPLOYEE' });
    expect(form.valid).toBeTrue();
  });
});

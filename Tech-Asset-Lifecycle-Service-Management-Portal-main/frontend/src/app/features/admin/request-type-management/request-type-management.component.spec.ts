import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { RequestTypeManagementComponent } from './request-type-management.component';
import { AdminService, AdminRequestType } from '../../../core/services/admin.service';

describe('RequestTypeManagementComponent (smoke)', () => {
  let fixture: ComponentFixture<RequestTypeManagementComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;

  const mockTypes: AdminRequestType[] = [
    { id: 1, name: 'New Device', description: 'Request a new device', approvalRequired: true, active: true, createdAt: '2026-01-01T00:00:00Z' },
    { id: 2, name: 'Repair', description: null, approvalRequired: false, active: true, createdAt: '2026-01-02T00:00:00Z' },
  ];

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'getRequestTypes', 'createRequestType', 'updateRequestType', 'toggleRequestTypeActive',
    ]);
    adminServiceSpy.getRequestTypes.and.returnValue(of(mockTypes));

    await TestBed.configureTestingModule({
      imports: [RequestTypeManagementComponent, MatSnackBarModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: AdminService, useValue: adminServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RequestTypeManagementComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('calls getRequestTypes on init', () => {
    expect(adminServiceSpy.getRequestTypes).toHaveBeenCalled();
  });

  it('populates types from service', () => {
    expect(fixture.componentInstance.types.length).toBe(2);
    expect(fixture.componentInstance.types[0].name).toBe('New Device');
  });

  it('renders one row per request type', () => {
    const rows = fixture.nativeElement.querySelectorAll('tr[mat-row]');
    expect(rows.length).toBe(2);
  });

  it('form is not shown by default', () => {
    expect(fixture.componentInstance.showForm).toBeFalse();
  });

  it('form has required validator on name', () => {
    const form = fixture.componentInstance.form;
    expect(form.controls.name.valid).toBeFalse();

    form.controls.name.setValue('My Type');
    expect(form.controls.name.valid).toBeTrue();
  });

  it('edit() populates form and shows it', () => {
    fixture.componentInstance.edit(mockTypes[0]);
    fixture.detectChanges();

    expect(fixture.componentInstance.showForm).toBeTrue();
    expect(fixture.componentInstance.editingId).toBe(1);
    expect(fixture.componentInstance.form.controls.name.value).toBe('New Device');
    expect(fixture.componentInstance.form.controls.approvalRequired.value).toBeTrue();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { AssetListComponent } from './asset-list.component';
import { AssetService } from '../../../core/services/asset.service';

describe('AssetListComponent (smoke)', () => {
  let fixture: ComponentFixture<AssetListComponent>;
  let assetServiceSpy: jasmine.SpyObj<AssetService>;

  const mockPaged = {
    data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
    message: 'ok',
    timestamp: '',
  };

  beforeEach(async () => {
    assetServiceSpy = jasmine.createSpyObj('AssetService', ['getAssets']);
    assetServiceSpy.getAssets.and.returnValue(of(mockPaged));

    await TestBed.configureTestingModule({
      imports: [AssetListComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: AssetService, useValue: assetServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AssetListComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('shows the New Asset button for ADMIN', () => {
    spyOn(fixture.componentInstance.authService, 'hasAnyRole').and.returnValue(true);
    fixture.detectChanges();
    const btn = fixture.nativeElement.querySelector('a[routerLink]');
    expect(btn).toBeTruthy();
  });
});

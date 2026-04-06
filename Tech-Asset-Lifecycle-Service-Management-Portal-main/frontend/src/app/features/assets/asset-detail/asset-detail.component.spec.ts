import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { AssetDetailComponent } from './asset-detail.component';
import { AssetService } from '../../../core/services/asset.service';
import { Asset } from '../../../core/models/asset.model';

const mockAsset: Asset = {
  id: 1, assetTag: 'TAG-001', serialNumber: null, assetType: 'LAPTOP',
  vendor: 'Dell', model: 'XPS 15', status: 'AVAILABLE',
  purchaseDate: null, warrantyExpiry: null,
  assignedUserId: null, assignedUserName: null,
  location: null, costCenter: null, notes: null,
  createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(),
};

describe('AssetDetailComponent (smoke)', () => {
  let fixture: ComponentFixture<AssetDetailComponent>;
  let assetServiceSpy: jasmine.SpyObj<AssetService>;

  beforeEach(async () => {
    assetServiceSpy = jasmine.createSpyObj('AssetService', ['getAsset', 'getHistory']);
    assetServiceSpy.getAsset.and.returnValue(of(mockAsset));
    assetServiceSpy.getHistory.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AssetDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: AssetService, useValue: assetServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' }, url: [] } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AssetDetailComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('displays the asset tag', () => {
    const h1 = fixture.nativeElement.querySelector('h1');
    expect(h1?.textContent).toContain('TAG-001');
  });

  it('shows empty history message when no history', () => {
    expect(fixture.nativeElement.textContent).toContain('No history yet');
  });
});

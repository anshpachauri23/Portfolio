import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../core/services/dashboard.service';

describe('DashboardComponent (smoke)', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let dashboardServiceSpy: jasmine.SpyObj<DashboardService>;

  const mockSummary = {
    requestCountByStatus: { IN_PROGRESS: 5, CLOSED: 10 },
    assetCountByStatus: { AVAILABLE: 20, ASSIGNED: 8 },
  };

  beforeEach(async () => {
    dashboardServiceSpy = jasmine.createSpyObj('DashboardService', [
      'getSummary', 'getRequestTrends', 'getAssetsByStatus', 'getSlaAging',
    ]);
    dashboardServiceSpy.getSummary.and.returnValue(of(mockSummary));
    dashboardServiceSpy.getRequestTrends.and.returnValue(of([
      { month: '2026-01', count: 3 },
      { month: '2026-02', count: 7 },
    ]));
    dashboardServiceSpy.getAssetsByStatus.and.returnValue(of([
      { status: 'AVAILABLE', count: 20 },
    ]));
    dashboardServiceSpy.getSlaAging.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideAnimations(),
        { provide: DashboardService, useValue: dashboardServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('renders without errors', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('calls all four dashboard service methods on init', () => {
    expect(dashboardServiceSpy.getSummary).toHaveBeenCalled();
    expect(dashboardServiceSpy.getRequestTrends).toHaveBeenCalled();
    expect(dashboardServiceSpy.getAssetsByStatus).toHaveBeenCalled();
    expect(dashboardServiceSpy.getSlaAging).toHaveBeenCalled();
  });

  it('populates summaryCards from service data', () => {
    const comp = fixture.componentInstance;
    expect(comp.summaryCards.length).toBe(3);

    const totalRequests = comp.summaryCards.find(c => c.label === 'Total Requests');
    expect(totalRequests?.value).toBe(15);

    const totalAssets = comp.summaryCards.find(c => c.label === 'Total Assets');
    expect(totalAssets?.value).toBe(28);
  });

  it('counts open requests correctly (excludes CLOSED/COMPLETED/REJECTED)', () => {
    const comp = fixture.componentInstance;
    const open = comp.summaryCards.find(c => c.label === 'Open Requests');
    expect(open?.value).toBe(5);
  });

  it('populates trends data', () => {
    expect(fixture.componentInstance.trends.length).toBe(2);
    expect(fixture.componentInstance.trends[0].month).toBe('2026-01');
  });

  it('shows empty SLA aging message when no overdue requests', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No overdue requests');
  });
});

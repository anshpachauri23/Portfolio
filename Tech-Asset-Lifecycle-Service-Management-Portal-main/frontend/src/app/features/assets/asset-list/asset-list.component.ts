import { ChangeDetectorRef, Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, distinctUntilChanged, merge, startWith, switchMap } from 'rxjs';
import { AssetService } from '../../../core/services/asset.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Asset, AssetStatus } from '../../../core/models/asset.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-asset-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatFormFieldModule, MatSelectModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    StatusBadgeComponent,
  ],
  templateUrl: './asset-list.component.html',
})
export class AssetListComponent implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns = ['assetTag', 'assetType', 'status', 'vendor', 'model', 'assignedUserName', 'actions'];
  assets: Asset[] = [];
  totalElements = 0;
  loading = false;

  readonly assetStatuses: AssetStatus[] = ['AVAILABLE', 'ASSIGNED', 'UNDER_REPAIR', 'RECLAIMED', 'LOST', 'RETIRED'];

  readonly filterForm = this.fb.group({
    status: [''],
    assetType: [''],
  });

  readonly canCreate$ = this.authService.currentUser$.pipe(
    // emits true when role is ADMIN or TECHNICIAN
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly assetService: AssetService,
    readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    merge(
      this.filterForm.valueChanges.pipe(debounceTime(300), distinctUntilChanged()),
      this.sort.sortChange,
      this.paginator.page,
    )
      .pipe(
        startWith({}),
        switchMap(() => {
          this.loading = true;
          const { status, assetType } = this.filterForm.value;
          return this.assetService.getAssets({
            status: status || undefined,
            assetType: assetType || undefined,
            page: this.paginator.pageIndex,
            size: this.paginator.pageSize,
            sort: this.sort.active
              ? `${this.sort.active},${this.sort.direction}`
              : 'createdAt,desc',
          });
        }),
      )
      .subscribe(response => {
        this.loading = false;
        this.assets = response.data.content;
        this.totalElements = response.data.totalElements;
        this.cdr.detectChanges();
      });
  }

  canManage(): boolean {
    return this.authService.hasAnyRole('ADMIN', 'TECHNICIAN');
  }
}

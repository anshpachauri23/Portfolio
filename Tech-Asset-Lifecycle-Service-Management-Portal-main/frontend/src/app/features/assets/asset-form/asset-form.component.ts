import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AssetService } from '../../../core/services/asset.service';

@Component({
  selector: 'app-asset-form',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatDatepickerModule, MatNativeDateModule,
    MatProgressSpinnerModule, MatSnackBarModule,
  ],
  templateUrl: './asset-form.component.html',
})
export class AssetFormComponent implements OnInit {
  isEditMode = false;
  assetId: number | null = null;
  loading = false;
  saving = false;

  readonly form = this.fb.nonNullable.group({
    assetTag:      ['', [Validators.required, Validators.maxLength(50)]],
    serialNumber:  [''],
    assetType:     ['', [Validators.required, Validators.maxLength(50)]],
    vendor:        [''],
    model:         [''],
    purchaseDate:  [null as Date | null],
    warrantyExpiry:[null as Date | null],
    location:      [''],
    costCenter:    [''],
    notes:         [''],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly assetService: AssetService,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && this.route.snapshot.url.some(s => s.path === 'edit')) {
      this.isEditMode = true;
      this.assetId = Number(id);
      this.loading = true;
      this.assetService.getAsset(this.assetId).subscribe({
        next: asset => {
          this.form.patchValue({
            assetTag: asset.assetTag,
            serialNumber: asset.serialNumber ?? '',
            assetType: asset.assetType,
            vendor: asset.vendor ?? '',
            model: asset.model ?? '',
            purchaseDate: asset.purchaseDate ? new Date(asset.purchaseDate) : null,
            warrantyExpiry: asset.warrantyExpiry ? new Date(asset.warrantyExpiry) : null,
            location: asset.location ?? '',
            costCenter: asset.costCenter ?? '',
            notes: asset.notes ?? '',
          });
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.snackBar.open('Failed to load asset', 'Dismiss', { duration: 3000 });
        },
      });
    }
  }

  submit(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;

    const val = this.form.getRawValue();
    const payload = {
      assetTag: val.assetTag,
      serialNumber: val.serialNumber || undefined,
      assetType: val.assetType,
      vendor: val.vendor || undefined,
      model: val.model || undefined,
      purchaseDate: val.purchaseDate ? (val.purchaseDate as Date).toISOString().split('T')[0] : undefined,
      warrantyExpiry: val.warrantyExpiry ? (val.warrantyExpiry as Date).toISOString().split('T')[0] : undefined,
      location: val.location || undefined,
      costCenter: val.costCenter || undefined,
      notes: val.notes || undefined,
    };

    const request$ = this.isEditMode && this.assetId
      ? this.assetService.updateAsset(this.assetId, payload)
      : this.assetService.createAsset(payload);

    request$.subscribe({
      next: asset => {
        this.saving = false;
        this.snackBar.open(this.isEditMode ? 'Asset updated' : 'Asset created', 'OK', { duration: 2000 });
        this.router.navigate(['/dashboard/assets', asset.id]);
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err?.error?.message ?? 'Save failed', 'Dismiss', { duration: 4000 });
      },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { switchMap, startWith } from 'rxjs';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { RequestType, RequestPriority } from '../../../core/models/service-request.model';

@Component({
  selector: 'app-request-form',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, MatSnackBarModule,
  ],
  templateUrl: './request-form.component.html',
})
export class RequestFormComponent implements OnInit {
  requestTypes: RequestType[] = [];
  selectedTypeApprovalRequired = false;
  saving = false;

  readonly priorities: RequestPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  readonly form = this.fb.nonNullable.group({
    requestTypeId: [null as number | null, Validators.required],
    title:         ['', [Validators.required, Validators.maxLength(255)]],
    description:   ['', [Validators.required, Validators.minLength(10)]],
    priority:      ['MEDIUM' as RequestPriority, Validators.required],
    assetId:       [null as number | null],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly requestService: ServiceRequestService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.requestService.getActiveRequestTypes().subscribe({
      next: types => { this.requestTypes = types; },
      error: () => this.snackBar.open('Failed to load request types', 'Dismiss', { duration: 3000 }),
    });

    this.form.controls.requestTypeId.valueChanges.subscribe(typeId => {
      const type = this.requestTypes.find(t => t.id === typeId);
      this.selectedTypeApprovalRequired = type?.approvalRequired ?? false;
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;

    const val = this.form.getRawValue();
    this.requestService.createRequest({
      requestTypeId: val.requestTypeId!,
      title: val.title,
      description: val.description,
      priority: val.priority,
      assetId: val.assetId ?? undefined,
    }).subscribe({
      next: created => {
        this.saving = false;
        this.snackBar.open('Request submitted', 'OK', { duration: 2000 });
        this.router.navigate(['/dashboard/requests', created.id]);
      },
      error: err => {
        this.saving = false;
        this.snackBar.open(err?.error?.message ?? 'Submission failed', 'Dismiss', { duration: 4000 });
      },
    });
  }
}

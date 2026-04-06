import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { AssetService } from '../../../core/services/asset.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ServiceRequest, RequestStatus } from '../../../core/models/service-request.model';
import { Asset } from '../../../core/models/asset.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ApprovalService, ApprovalStep } from '../../../core/services/approval.service';
import { AttachmentService, AttachmentResponse } from '../../../core/services/attachment.service';

const VALID_TECHNICIAN_TRANSITIONS: Record<RequestStatus, RequestStatus[]> = {
  DRAFT:            ['SUBMITTED'],
  SUBMITTED:        ['IN_PROGRESS'],
  PENDING_APPROVAL: [],
  APPROVED:         ['IN_PROGRESS'],
  IN_PROGRESS:      ['WAITING_FOR_USER', 'COMPLETED'],
  WAITING_FOR_USER: ['IN_PROGRESS', 'COMPLETED'],
  COMPLETED:        ['CLOSED'],
  CLOSED:           [],
  REJECTED:         [],
};

@Component({
  selector: 'app-request-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule, ReactiveFormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatDividerModule, MatListModule,
    MatProgressSpinnerModule, MatSnackBarModule,
    StatusBadgeComponent,
  ],
  templateUrl: './request-detail.component.html',
})
export class RequestDetailComponent implements OnInit {
  request: ServiceRequest | null = null;
  loading = true;
  submittingComment = false;
  assigning = false;
  approvalSteps: ApprovalStep[] = [];
  attachments: AttachmentResponse[] = [];
  availableAssets: Asset[] = [];

  readonly commentForm = this.fb.nonNullable.group({
    body: ['', [Validators.required, Validators.minLength(1)]],
  });

  readonly fulfilForm = this.fb.nonNullable.group({
    assetId: [null as number | null, Validators.required],
    notes:   [''],
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly requestService: ServiceRequestService,
    private readonly assetService: AssetService,
    readonly authService: AuthService,
    private readonly fb: FormBuilder,
    private readonly snackBar: MatSnackBar,
    private readonly approvalService: ApprovalService,
    private readonly attachmentService: AttachmentService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadRequest(id);
  }

  loadRequest(id: number): void {
    this.requestService.getRequest(id).subscribe({
      next: r => {
        this.request = r;
        this.loading = false;
        this.loadApprovalHistory(id);
        this.loadAttachments(id);
        if (this.canFulfil()) {
          this.loadAvailableAssets();
        }
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load request', 'Dismiss', { duration: 3000 });
      },
    });
  }

  loadApprovalHistory(id: number): void {
    this.approvalService.getApprovalHistory(id).subscribe({
      next: steps => { this.approvalSteps = steps; },
      error: () => {},
    });
  }

  loadAttachments(id: number): void {
    this.attachmentService.listRequestAttachments(id).subscribe({
      next: a => { this.attachments = a; },
      error: () => {},
    });
  }

  loadAvailableAssets(): void {
    this.assetService.getAssets({ status: 'AVAILABLE', size: 100 }).subscribe({
      next: r => { this.availableAssets = r.data.content; },
      error: () => {},
    });
  }

  canFulfil(): boolean {
    if (!this.request) return false;
    return (
      this.authService.hasAnyRole('TECHNICIAN', 'ADMIN') &&
      this.request.status === 'IN_PROGRESS' &&
      this.request.assetId === null
    );
  }

  fulfil(): void {
    if (this.fulfilForm.invalid || this.assigning || !this.request) return;
    this.assigning = true;
    const { assetId, notes } = this.fulfilForm.getRawValue();

    this.assetService.assignAsset(assetId!, {
      userId: this.request.requesterId,
      notes: notes || undefined,
    }).subscribe({
      next: () => {
        this.assigning = false;
        this.fulfilForm.reset();
        this.snackBar.open('Asset assigned successfully', 'OK', { duration: 3000 });
        this.loadRequest(this.request!.id);
      },
      error: err => {
        this.assigning = false;
        this.snackBar.open(err?.error?.message ?? 'Assignment failed', 'Dismiss', { duration: 4000 });
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length || !this.request) return;
    const file = input.files[0];
    this.attachmentService.uploadToRequest(this.request.id, file).subscribe({
      next: att => {
        this.attachments = [att, ...this.attachments];
        this.snackBar.open('File uploaded', 'OK', { duration: 2000 });
      },
      error: () => this.snackBar.open('Upload failed', 'Dismiss', { duration: 3000 }),
    });
  }

  getDownloadUrl(attachmentId: number): string {
    return this.attachmentService.getDownloadUrl(attachmentId);
  }

  get validNextStatuses(): RequestStatus[] {
    if (!this.request) return [];
    if (!this.authService.hasAnyRole('TECHNICIAN', 'ADMIN')) return [];
    return VALID_TECHNICIAN_TRANSITIONS[this.request.status] ?? [];
  }

  canComment(): boolean {
    return this.authService.isLoggedIn();
  }

  changeStatus(newStatus: RequestStatus): void {
    if (!this.request) return;
    this.requestService.updateStatus(this.request.id, { status: newStatus }).subscribe({
      next: updated => {
        this.request = updated;
        this.snackBar.open('Status updated', 'OK', { duration: 2000 });
      },
      error: err => {
        this.snackBar.open(err?.error?.message ?? 'Update failed', 'Dismiss', { duration: 4000 });
      },
    });
  }

  submitComment(): void {
    if (this.commentForm.invalid || this.submittingComment || !this.request) return;
    this.submittingComment = true;
    const { body } = this.commentForm.getRawValue();

    this.requestService.addComment(this.request.id, { body }).subscribe({
      next: comment => {
        this.request!.comments = [...this.request!.comments, comment];
        this.commentForm.reset();
        this.submittingComment = false;
      },
      error: () => {
        this.submittingComment = false;
        this.snackBar.open('Failed to add comment', 'Dismiss', { duration: 3000 });
      },
    });
  }
}

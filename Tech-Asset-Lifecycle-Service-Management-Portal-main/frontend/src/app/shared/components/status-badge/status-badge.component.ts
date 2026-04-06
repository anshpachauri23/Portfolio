import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';

const STATUS_COLORS: Record<string, string> = {
  // Asset statuses
  AVAILABLE:    'green',
  ASSIGNED:     'blue',
  UNDER_REPAIR: 'orange',
  RECLAIMED:    'purple',
  LOST:         'red',
  RETIRED:      'grey',
  // Request statuses
  DRAFT:            'grey',
  SUBMITTED:        'blue',
  PENDING_APPROVAL: 'orange',
  APPROVED:         'teal',
  IN_PROGRESS:      'blue',
  WAITING_FOR_USER: 'yellow',
  COMPLETED:        'green',
  CLOSED:           'grey',
  REJECTED:         'red',
};

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule, MatChipsModule],
  template: `
    <span class="status-badge" [ngClass]="'badge-' + colorClass">
      {{ label }}
    </span>
  `,
  styles: [`
    .status-badge {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.4px;
    }
    .badge-green  { background: #e8f5e9; color: #2e7d32; }
    .badge-blue   { background: #e3f2fd; color: #1565c0; }
    .badge-orange { background: #fff3e0; color: #e65100; }
    .badge-red    { background: #ffebee; color: #c62828; }
    .badge-grey   { background: #f5f5f5; color: #616161; }
    .badge-purple { background: #f3e5f5; color: #6a1b9a; }
    .badge-teal   { background: #e0f2f1; color: #00695c; }
    .badge-yellow { background: #fffde7; color: #f57f17; }
  `],
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: string;

  get colorClass(): string {
    return STATUS_COLORS[this.status] ?? 'grey';
  }

  get label(): string {
    return this.status.replace(/_/g, ' ');
  }
}

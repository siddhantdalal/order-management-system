import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="badge" [ngClass]="getClass()">{{ status }}</span>`,
  styles: [`
    .badge {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .pending { background: #fef3c7; color: #92400e; }
    .processing { background: #dbeafe; color: #1e40af; }
    .confirmed { background: #d1fae5; color: #065f46; }
    .shipped { background: #e0e7ff; color: #3730a3; }
    .delivered { background: #d1fae5; color: #065f46; }
    .cancelled { background: #fee2e2; color: #991b1b; }
    .completed { background: #d1fae5; color: #065f46; }
    .failed { background: #fee2e2; color: #991b1b; }
    .refunded { background: #fef3c7; color: #92400e; }
  `]
})
export class StatusBadgeComponent {
  @Input() status = '';

  getClass(): string {
    const s = this.status.toLowerCase().replace('payment_', '');
    if (s.includes('pending')) return 'pending';
    if (s.includes('processing')) return 'processing';
    if (s.includes('confirmed') || s.includes('completed') || s.includes('delivered')) return 'confirmed';
    if (s.includes('shipped')) return 'shipped';
    if (s.includes('cancelled') || s.includes('failed')) return 'cancelled';
    if (s.includes('refunded')) return 'refunded';
    return 'pending';
  }
}

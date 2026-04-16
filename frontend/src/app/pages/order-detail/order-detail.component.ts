import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { PaymentService } from '../../services/payment.service';
import { StatusBadgeComponent } from '../../components/status-badge/status-badge.component';
import { Order, Payment } from '../../models/models';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, StatusBadgeComponent],
  template: `
    @if (order) {
      <div class="page-header">
        <h1>Order #{{ order.id }}</h1>
      </div>

      <div class="detail-grid">
        <div class="card">
          <h3>Order Details</h3>
          <div class="detail-row"><span>Status</span><app-status-badge [status]="order.status"></app-status-badge></div>
          <div class="detail-row"><span>Date</span><span>{{ order.createdAt | date:'medium' }}</span></div>
          <div class="detail-row"><span>Shipping</span><span>{{ order.shippingAddress }}</span></div>
          <div class="detail-row"><span>Total</span><span class="total">\${{ order.totalAmount | number:'1.2-2' }}</span></div>

          @if (order.status !== 'CANCELLED' && order.status !== 'DELIVERED') {
            <button class="btn btn-danger" (click)="cancelOrder()" style="margin-top:16px">Cancel Order</button>
          }
        </div>

        <div class="card">
          <h3>Items</h3>
          <table>
            <thead><tr><th>Product</th><th>Qty</th><th>Price</th><th>Subtotal</th></tr></thead>
            <tbody>
              @for (item of order.items; track item.productId) {
                <tr>
                  <td>{{ item.productName }}</td>
                  <td>{{ item.quantity }}</td>
                  <td>\${{ item.unitPrice | number:'1.2-2' }}</td>
                  <td>\${{ item.unitPrice * item.quantity | number:'1.2-2' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        @if (payment) {
          <div class="card">
            <h3>Payment Info</h3>
            <div class="detail-row"><span>Status</span><app-status-badge [status]="payment.status"></app-status-badge></div>
            <div class="detail-row"><span>Method</span><span>{{ payment.paymentMethod }}</span></div>
            <div class="detail-row"><span>Transaction</span><span>{{ payment.transactionId }}</span></div>
            <div class="detail-row"><span>Amount</span><span>\${{ payment.amount | number:'1.2-2' }}</span></div>
          </div>
        }
      </div>

      <button class="btn btn-secondary" (click)="goBack()" style="margin-top:20px">Back to Orders</button>
    }
  `,
  styles: [`
    .detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 24px; }
    h3 { margin-bottom: 16px; }
    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f3f4f6; font-size: 14px; }
    .total { font-weight: 700; color: #4f46e5; font-size: 16px; }
  `]
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  payment: Payment | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getById(id).subscribe(res => {
      this.order = res.data;
    });
    this.paymentService.getByOrderId(id).subscribe({
      next: (res) => { this.payment = res.data; },
      error: () => {}
    });
  }

  cancelOrder(): void {
    if (this.order) {
      this.orderService.cancelOrder(this.order.id).subscribe(() => {
        this.router.navigate(['/orders']);
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }
}

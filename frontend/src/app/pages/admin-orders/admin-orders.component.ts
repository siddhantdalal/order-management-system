import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { StatusBadgeComponent } from '../../components/status-badge/status-badge.component';
import { Order } from '../../models/models';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  template: `
    <div class="page-header">
      <h1>Manage Orders</h1>
    </div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th>Order #</th>
            <th>User ID</th>
            <th>Items</th>
            <th>Total</th>
            <th>Status</th>
            <th>Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          @for (order of orders; track order.id) {
            <tr>
              <td>#{{ order.id }}</td>
              <td>{{ order.userId }}</td>
              <td>{{ order.items.length }}</td>
              <td>\${{ order.totalAmount | number:'1.2-2' }}</td>
              <td><app-status-badge [status]="order.status"></app-status-badge></td>
              <td>{{ order.createdAt | date:'short' }}</td>
              <td>
                <div class="action-btns">
                  @if (order.status === 'CONFIRMED') {
                    <button class="btn btn-primary" (click)="updateStatus(order.id, 'SHIPPED')">Ship</button>
                  }
                  @if (order.status === 'SHIPPED') {
                    <button class="btn btn-success" (click)="updateStatus(order.id, 'DELIVERED')">Deliver</button>
                  }
                  <a [routerLink]="['/orders', order.id]" class="btn btn-secondary">View</a>
                </div>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .action-btns { display: flex; gap: 8px; }
  `]
})
export class AdminOrdersComponent implements OnInit {
  orders: Order[] = [];

  constructor(
    private orderService: OrderService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Load all orders - admin uses userId 0 to get all, or we iterate
    // For demo, we'll load for the admin user (they can see all via the admin endpoint pattern)
    const user = this.authService.getUser();
    if (user) {
      this.orderService.getByUserId(user.id).subscribe(res => {
        this.orders = res.data;
      });
    }
  }

  updateStatus(orderId: number, status: string): void {
    this.orderService.updateStatus(orderId, status).subscribe(() => {
      this.ngOnInit();
    });
  }
}

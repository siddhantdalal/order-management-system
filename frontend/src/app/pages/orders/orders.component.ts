import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { StatusBadgeComponent } from '../../components/status-badge/status-badge.component';
import { Order } from '../../models/models';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent],
  template: `
    <div class="page-header">
      <h1>My Orders</h1>
    </div>

    @if (orders.length === 0) {
      <div class="card" style="text-align:center;padding:40px">
        <p>No orders yet.</p>
        <a routerLink="/products" class="btn btn-primary" style="display:inline-block;margin-top:16px">Start Shopping</a>
      </div>
    } @else {
      <div class="card">
        <table>
          <thead>
            <tr>
              <th>Order #</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (order of orders; track order.id) {
              <tr>
                <td>#{{ order.id }}</td>
                <td>{{ order.createdAt | date:'medium' }}</td>
                <td>{{ order.items.length }} item(s)</td>
                <td>\${{ order.totalAmount | number:'1.2-2' }}</td>
                <td><app-status-badge [status]="order.status"></app-status-badge></td>
                <td><a [routerLink]="['/orders', order.id]" class="btn btn-secondary">View</a></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];

  constructor(private orderService: OrderService, private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.orderService.getByUserId(user.id).subscribe(res => {
        this.orders = res.data;
      });
    }
  }
}

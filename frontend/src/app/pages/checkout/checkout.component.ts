import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h1>Checkout</h1>
    </div>

    @if (error) {
      <div class="alert alert-error">{{ error }}</div>
    }

    <div class="checkout-layout">
      <div class="card">
        <h3>Shipping Address</h3>
        <div class="form-group">
          <label>Full Address</label>
          <textarea [(ngModel)]="shippingAddress" rows="3" placeholder="Enter your shipping address"></textarea>
        </div>

        <h3 style="margin-top:20px">Payment Method</h3>
        <div class="form-group">
          <select [(ngModel)]="paymentMethod">
            <option value="CREDIT_CARD">Credit Card</option>
            <option value="DEBIT_CARD">Debit Card</option>
            <option value="UPI">UPI</option>
            <option value="NET_BANKING">Net Banking</option>
          </select>
        </div>
      </div>

      <div class="card summary">
        <h3>Order Summary</h3>
        @for (item of cartItems; track item.product.id) {
          <div class="summary-item">
            <span>{{ item.product.name }} x {{ item.quantity }}</span>
            <span>\${{ item.product.price * item.quantity | number:'1.2-2' }}</span>
          </div>
        }
        <div class="summary-total">
          <span>Total</span>
          <span class="total">\${{ cartService.getTotal() | number:'1.2-2' }}</span>
        </div>
        <button class="btn btn-primary" style="width:100%;margin-top:16px" (click)="placeOrder()" [disabled]="loading">
          {{ loading ? 'Placing Order...' : 'Place Order' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .checkout-layout { display: grid; grid-template-columns: 1fr 360px; gap: 24px; align-items: start; }
    h3 { margin-bottom: 12px; }
    .summary-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; border-bottom: 1px solid #f3f4f6; }
    .summary-total { display: flex; justify-content: space-between; padding: 12px 0; border-top: 2px solid #e5e7eb; margin-top: 8px; }
    .total { font-size: 20px; font-weight: 700; color: #4f46e5; }
  `]
})
export class CheckoutComponent {
  shippingAddress = '';
  paymentMethod = 'CREDIT_CARD';
  error = '';
  loading = false;
  cartItems = this.cartService['cartItems'];

  constructor(
    public cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  placeOrder(): void {
    if (!this.shippingAddress.trim()) {
      this.error = 'Please enter a shipping address';
      return;
    }

    this.loading = true;
    this.error = '';

    const orderRequest = {
      items: this.cartItems.map((item: any) => ({
        productId: item.product.id,
        productName: item.product.name,
        quantity: item.quantity,
        unitPrice: item.product.price
      })),
      shippingAddress: this.shippingAddress
    };

    this.orderService.createOrder(orderRequest).subscribe({
      next: (res) => {
        this.cartService.clearCart();
        this.router.navigate(['/orders', res.data.id]);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to place order';
        this.loading = false;
      }
    });
  }
}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { CartItem } from '../../models/models';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-header">
      <h1>Shopping Cart</h1>
    </div>

    @if ((cartService.cart$ | async)?.length === 0) {
      <div class="card" style="text-align:center;padding:40px">
        <p>Your cart is empty.</p>
        <a routerLink="/products" class="btn btn-primary" style="display:inline-block;margin-top:16px">Browse Products</a>
      </div>
    } @else {
      <div class="cart-layout">
        <div class="card">
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Subtotal</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              @for (item of (cartService.cart$ | async); track item.product.id) {
                <tr>
                  <td>{{ item.product.name }}</td>
                  <td>\${{ item.product.price | number:'1.2-2' }}</td>
                  <td>
                    <div class="qty-controls">
                      <button (click)="updateQty(item, -1)">-</button>
                      <span>{{ item.quantity }}</span>
                      <button (click)="updateQty(item, 1)">+</button>
                    </div>
                  </td>
                  <td>\${{ item.product.price * item.quantity | number:'1.2-2' }}</td>
                  <td><button class="btn btn-danger" (click)="remove(item.product.id)">Remove</button></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        <div class="card summary">
          <h3>Order Summary</h3>
          <div class="summary-row">
            <span>Total</span>
            <span class="total">\${{ cartService.getTotal() | number:'1.2-2' }}</span>
          </div>
          <a routerLink="/checkout" class="btn btn-primary" style="display:block;text-align:center;margin-top:16px">
            Proceed to Checkout
          </a>
        </div>
      </div>
    }
  `,
  styles: [`
    .cart-layout { display: grid; grid-template-columns: 1fr 320px; gap: 24px; align-items: start; }
    .qty-controls { display: flex; align-items: center; gap: 8px; }
    .qty-controls button { width: 28px; height: 28px; border: 1px solid #d1d5db; border-radius: 4px; background: white; cursor: pointer; font-weight: 600; }
    .summary h3 { margin-bottom: 16px; }
    .summary-row { display: flex; justify-content: space-between; padding: 12px 0; border-top: 1px solid #e5e7eb; }
    .total { font-size: 20px; font-weight: 700; color: #4f46e5; }
  `]
})
export class CartComponent {
  constructor(public cartService: CartService) {}

  updateQty(item: CartItem, delta: number): void {
    this.cartService.updateQuantity(item.product.id, item.quantity + delta);
  }

  remove(productId: number): void {
    this.cartService.removeFromCart(productId);
  }
}

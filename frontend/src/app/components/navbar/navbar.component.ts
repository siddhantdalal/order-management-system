import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="container navbar-content">
        <a routerLink="/products" class="logo">OrderFlow</a>
        <div class="nav-links">
          <a routerLink="/products" routerLinkActive="active">Products</a>
          @if (authService.isLoggedIn()) {
            <a routerLink="/cart" routerLinkActive="active">
              Cart ({{ cartService.getItemCount() }})
            </a>
            <a routerLink="/orders" routerLinkActive="active">Orders</a>
            <a routerLink="/notifications" routerLinkActive="active">Notifications</a>
            @if (authService.isAdmin()) {
              <a routerLink="/admin/products" routerLinkActive="active">Manage Products</a>
              <a routerLink="/admin/orders" routerLinkActive="active">Manage Orders</a>
            }
            <span class="user-info">{{ authService.getUser()?.firstName }}</span>
            <button class="btn btn-secondary" (click)="logout()">Logout</button>
          } @else {
            <a routerLink="/login" routerLinkActive="active">Login</a>
            <a routerLink="/register" routerLinkActive="active">Register</a>
          }
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      background: white;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      padding: 0 20px;
    }
    .navbar-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 60px;
    }
    .logo {
      font-size: 22px;
      font-weight: 700;
      color: #4f46e5;
    }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 20px;
    }
    .nav-links a {
      font-size: 14px;
      font-weight: 500;
      color: #6b7280;
      transition: color 0.2s;
      &:hover, &.active { color: #4f46e5; }
    }
    .user-info {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }
  `]
})
export class NavbarComponent {
  constructor(
    public authService: AuthService,
    public cartService: CartService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

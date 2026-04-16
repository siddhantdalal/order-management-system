import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/models';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (product) {
      <div class="product-detail">
        <div class="product-image">
          @if (product.imageUrl) {
            <img [src]="product.imageUrl" [alt]="product.name">
          } @else {
            <div class="placeholder-img">No Image Available</div>
          }
        </div>
        <div class="product-info card">
          <span class="category-tag">{{ product.category }}</span>
          <h1>{{ product.name }}</h1>
          <p class="price">\${{ product.price | number:'1.2-2' }}</p>
          <p class="description">{{ product.description }}</p>
          <p class="stock" [class.out-of-stock]="product.stockQuantity === 0">
            {{ product.stockQuantity > 0 ? product.stockQuantity + ' items in stock' : 'Out of stock' }}
          </p>
          @if (authService.isLoggedIn()) {
            <button class="btn btn-primary" (click)="addToCart()" [disabled]="product.stockQuantity === 0">
              Add to Cart
            </button>
          }
          <button class="btn btn-secondary" (click)="goBack()" style="margin-left:8px">Back</button>
        </div>
      </div>
    }
  `,
  styles: [`
    .product-detail { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; }
    .product-image { height: 400px; background: #f3f4f6; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
    .product-image img { width: 100%; height: 100%; object-fit: cover; }
    .placeholder-img { color: #9ca3af; }
    .category-tag { font-size: 12px; text-transform: uppercase; color: #6b7280; }
    h1 { font-size: 28px; margin: 8px 0 16px; }
    .price { font-size: 32px; font-weight: 700; color: #4f46e5; margin-bottom: 16px; }
    .description { color: #4b5563; margin-bottom: 16px; line-height: 1.8; }
    .stock { font-size: 14px; color: #16a34a; margin-bottom: 20px; }
    .out-of-stock { color: #dc2626; }
  `]
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getById(id).subscribe(res => {
      this.product = res.data;
    });
  }

  addToCart(): void {
    if (this.product) {
      this.cartService.addToCart(this.product);
      this.router.navigate(['/cart']);
    }
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }
}

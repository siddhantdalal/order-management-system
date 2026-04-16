import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/models';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <h1>Product Catalog</h1>
      <p>Browse our selection of products</p>
    </div>

    <div class="filters">
      <input type="text" [(ngModel)]="searchQuery" (input)="onSearch()" placeholder="Search products..." class="search-input">
      <select [(ngModel)]="selectedCategory" (change)="onCategoryChange()">
        <option value="">All Categories</option>
        <option value="ELECTRONICS">Electronics</option>
        <option value="CLOTHING">Clothing</option>
        <option value="BOOKS">Books</option>
        <option value="HOME">Home</option>
        <option value="SPORTS">Sports</option>
        <option value="OTHER">Other</option>
      </select>
    </div>

    <div class="grid grid-4">
      @for (product of products; track product.id) {
        <div class="card product-card">
          <div class="product-image">
            @if (product.imageUrl) {
              <img [src]="product.imageUrl" [alt]="product.name">
            } @else {
              <div class="placeholder-img">No Image</div>
            }
          </div>
          <div class="product-info">
            <span class="category-tag">{{ product.category }}</span>
            <h3><a [routerLink]="['/products', product.id]">{{ product.name }}</a></h3>
            <p class="price">\${{ product.price | number:'1.2-2' }}</p>
            <p class="stock" [class.out-of-stock]="product.stockQuantity === 0">
              {{ product.stockQuantity > 0 ? product.stockQuantity + ' in stock' : 'Out of stock' }}
            </p>
            @if (authService.isLoggedIn()) {
              <button class="btn btn-primary" (click)="addToCart(product)" [disabled]="product.stockQuantity === 0">
                Add to Cart
              </button>
            }
          </div>
        </div>
      }
    </div>

    @if (products.length === 0) {
      <div class="card" style="text-align:center;padding:40px">
        <p>No products found.</p>
      </div>
    }
  `,
  styles: [`
    .filters { display: flex; gap: 12px; margin-bottom: 24px; }
    .search-input { flex: 1; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; }
    .search-input:focus { outline: none; border-color: #4f46e5; }
    select { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; }
    .product-card { padding: 0; overflow: hidden; }
    .product-image { height: 180px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
    .product-image img { width: 100%; height: 100%; object-fit: cover; }
    .placeholder-img { color: #9ca3af; font-size: 14px; }
    .product-info { padding: 16px; }
    .category-tag { font-size: 11px; text-transform: uppercase; color: #6b7280; letter-spacing: 0.05em; }
    h3 { margin: 4px 0 8px; font-size: 16px; }
    h3 a:hover { color: #4f46e5; }
    .price { font-size: 18px; font-weight: 700; color: #4f46e5; margin-bottom: 4px; }
    .stock { font-size: 13px; color: #16a34a; margin-bottom: 12px; }
    .out-of-stock { color: #dc2626; }
  `]
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  searchQuery = '';
  selectedCategory = '';

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getAll().subscribe(res => {
      this.products = res.data;
    });
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.productService.search(this.searchQuery).subscribe(res => {
        this.products = res.data;
      });
    } else {
      this.loadProducts();
    }
  }

  onCategoryChange(): void {
    if (this.selectedCategory) {
      this.productService.getByCategory(this.selectedCategory).subscribe(res => {
        this.products = res.data;
      });
    } else {
      this.loadProducts();
    }
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product);
  }
}

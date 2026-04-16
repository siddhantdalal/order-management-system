import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/models';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h1>Manage Products</h1>
    </div>

    <div class="admin-layout">
      <div class="card form-panel">
        <h3>{{ editing ? 'Edit Product' : 'Add Product' }}</h3>
        @if (error) { <div class="alert alert-error">{{ error }}</div> }
        @if (success) { <div class="alert alert-success">{{ success }}</div> }
        <div class="form-group"><label>Name</label><input [(ngModel)]="form.name"></div>
        <div class="form-group"><label>Description</label><textarea [(ngModel)]="form.description" rows="3"></textarea></div>
        <div class="form-group"><label>Price</label><input type="number" [(ngModel)]="form.price" step="0.01"></div>
        <div class="form-group"><label>Stock Quantity</label><input type="number" [(ngModel)]="form.stockQuantity"></div>
        <div class="form-group">
          <label>Category</label>
          <select [(ngModel)]="form.category">
            <option value="ELECTRONICS">Electronics</option>
            <option value="CLOTHING">Clothing</option>
            <option value="BOOKS">Books</option>
            <option value="HOME">Home</option>
            <option value="SPORTS">Sports</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        <div style="display:flex;gap:8px">
          <button class="btn btn-primary" (click)="saveProduct()">{{ editing ? 'Update' : 'Create' }}</button>
          @if (editing) {
            <button class="btn btn-secondary" (click)="resetForm()">Cancel</button>
          }
        </div>

        @if (editing && editId) {
          <div style="margin-top:20px">
            <h3>Upload Image</h3>
            <input type="file" (change)="onFileSelected($event)" accept="image/*">
            <button class="btn btn-primary" (click)="uploadImage()" [disabled]="!selectedFile" style="margin-top:8px">Upload</button>
          </div>
        }
      </div>

      <div class="card">
        <table>
          <thead><tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Actions</th></tr></thead>
          <tbody>
            @for (p of products; track p.id) {
              <tr>
                <td>{{ p.id }}</td>
                <td>{{ p.name }}</td>
                <td>{{ p.category }}</td>
                <td>\${{ p.price | number:'1.2-2' }}</td>
                <td>{{ p.stockQuantity }}</td>
                <td><button class="btn btn-secondary" (click)="editProduct(p)">Edit</button></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .admin-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; align-items: start; }
    .form-panel h3 { margin-bottom: 16px; }
  `]
})
export class AdminProductsComponent implements OnInit {
  products: Product[] = [];
  form = { name: '', description: '', price: 0, stockQuantity: 0, category: 'ELECTRONICS' };
  editing = false;
  editId: number | null = null;
  error = '';
  success = '';
  selectedFile: File | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit(): void { this.loadProducts(); }

  loadProducts(): void {
    this.productService.getAll().subscribe(res => { this.products = res.data; });
  }

  saveProduct(): void {
    this.error = '';
    this.success = '';
    if (this.editing && this.editId) {
      this.productService.updateProduct(this.editId, this.form).subscribe({
        next: () => { this.success = 'Product updated'; this.loadProducts(); },
        error: (err) => { this.error = err.error?.message || 'Failed'; }
      });
    } else {
      this.productService.createProduct(this.form).subscribe({
        next: () => { this.success = 'Product created'; this.resetForm(); this.loadProducts(); },
        error: (err) => { this.error = err.error?.message || 'Failed'; }
      });
    }
  }

  editProduct(p: Product): void {
    this.editing = true;
    this.editId = p.id;
    this.form = { name: p.name, description: p.description, price: p.price, stockQuantity: p.stockQuantity, category: p.category };
  }

  resetForm(): void {
    this.editing = false;
    this.editId = null;
    this.form = { name: '', description: '', price: 0, stockQuantity: 0, category: 'ELECTRONICS' };
    this.selectedFile = null;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) { this.selectedFile = input.files[0]; }
  }

  uploadImage(): void {
    if (this.editId && this.selectedFile) {
      this.productService.uploadImage(this.editId, this.selectedFile).subscribe({
        next: () => { this.success = 'Image uploaded'; this.loadProducts(); },
        error: (err) => { this.error = err.error?.message || 'Upload failed'; }
      });
    }
  }
}

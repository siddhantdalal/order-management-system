import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="card auth-card">
        <h2>Create Account</h2>
        <p class="subtitle">Join OrderFlow today</p>
        @if (error) {
          <div class="alert alert-error">{{ error }}</div>
        }
        <form (ngSubmit)="onSubmit()">
          <div style="display:flex;gap:12px">
            <div class="form-group" style="flex:1">
              <label>First Name</label>
              <input type="text" [(ngModel)]="firstName" name="firstName" required>
            </div>
            <div class="form-group" style="flex:1">
              <label>Last Name</label>
              <input type="text" [(ngModel)]="lastName" name="lastName" required>
            </div>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="email" name="email" required>
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" [(ngModel)]="password" name="password" required minlength="6">
          </div>
          <button type="submit" class="btn btn-primary" style="width:100%" [disabled]="loading">
            {{ loading ? 'Creating...' : 'Create Account' }}
          </button>
        </form>
        <p class="auth-link">Already have an account? <a routerLink="/login">Sign In</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; padding-top: 60px; }
    .auth-card { max-width: 420px; width: 100%; }
    h2 { font-size: 24px; margin-bottom: 4px; }
    .subtitle { color: #6b7280; margin-bottom: 24px; }
    .auth-link { text-align: center; margin-top: 16px; font-size: 14px; color: #6b7280; }
    .auth-link a { color: #4f46e5; font-weight: 500; }
  `]
})
export class RegisterComponent {
  firstName = '';
  lastName = '';
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    this.authService.register(this.email, this.password, this.firstName, this.lastName).subscribe({
      next: () => { this.router.navigate(['/products']); },
      error: (err) => {
        this.error = err.error?.message || 'Registration failed';
        this.loading = false;
      }
    });
  }
}

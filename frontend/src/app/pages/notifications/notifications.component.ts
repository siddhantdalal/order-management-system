import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { Notification } from '../../models/models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <h1>Notifications</h1>
    </div>

    @if (notifications.length === 0) {
      <div class="card" style="text-align:center;padding:40px">
        <p>No notifications yet.</p>
      </div>
    } @else {
      <div class="notifications-list">
        @for (n of notifications; track n.id) {
          <div class="card notification-item">
            <div class="notification-header">
              <span class="notification-type">{{ n.type.replace('_', ' ') }}</span>
              <span class="notification-date">{{ n.createdAt | date:'medium' }}</span>
            </div>
            <h3>{{ n.subject }}</h3>
            <p>{{ n.content }}</p>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .notifications-list { display: flex; flex-direction: column; gap: 12px; }
    .notification-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
    .notification-type { font-size: 12px; text-transform: uppercase; color: #4f46e5; font-weight: 600; letter-spacing: 0.05em; }
    .notification-date { font-size: 12px; color: #9ca3af; }
    h3 { font-size: 16px; margin-bottom: 4px; }
    p { font-size: 14px; color: #4b5563; }
  `]
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    if (user) {
      this.notificationService.getByUserId(user.id).subscribe(res => {
        this.notifications = res.data;
      });
    }
  }
}

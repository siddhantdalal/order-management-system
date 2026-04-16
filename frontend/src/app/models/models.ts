export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  category: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: number;
  userId: number;
  items: OrderItem[];
  totalAmount: number;
  status: string;
  shippingAddress: string;
  createdAt: string;
}

export interface Payment {
  id: number;
  orderId: number;
  amount: number;
  status: string;
  paymentMethod: string;
  transactionId: string;
}

export interface Notification {
  id: number;
  userId: number;
  orderId: number;
  type: string;
  channel: string;
  subject: string;
  content: string;
  status: string;
  recipientEmail: string;
  createdAt: string;
}

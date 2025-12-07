import { User, Product, Cart, Order, RegisterRequest, LoginRequest, CreateOrderRequest } from './types';

const USER_BFF_URL = process.env.NEXT_PUBLIC_USER_BFF_URL || 'http://localhost:8081';
const ORDER_BFF_URL = process.env.NEXT_PUBLIC_ORDER_BFF_URL || 'http://localhost:8082';

class ApiClient {
  private async fetch<T>(url: string, options?: RequestInit): Promise<T> {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || `HTTP error! status: ${response.status}`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }

  // ============ User API ============
  async register(data: RegisterRequest): Promise<User> {
    return this.fetch<User>(`${USER_BFF_URL}/api/users/register`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async login(data: LoginRequest): Promise<User> {
    return this.fetch<User>(`${USER_BFF_URL}/api/users/login`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getUser(userId: number): Promise<User> {
    return this.fetch<User>(`${USER_BFF_URL}/api/users/${userId}`);
  }

  async updateUser(userId: number, data: Partial<User>): Promise<User> {
    return this.fetch<User>(`${USER_BFF_URL}/api/users/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  // ============ Product API ============
  async getProducts(): Promise<Product[]> {
    return this.fetch<Product[]>(`${USER_BFF_URL}/api/products`);
  }

  async getProduct(productId: number): Promise<Product> {
    return this.fetch<Product>(`${USER_BFF_URL}/api/products/${productId}`);
  }

  async getProductsByCategory(category: string): Promise<Product[]> {
    return this.fetch<Product[]>(`${USER_BFF_URL}/api/products/category/${category}`);
  }

  async searchProducts(query: string): Promise<Product[]> {
    return this.fetch<Product[]>(`${USER_BFF_URL}/api/products/search?query=${encodeURIComponent(query)}`);
  }

  // ============ Cart API ============
  async getCart(userId: number): Promise<Cart> {
    return this.fetch<Cart>(`${ORDER_BFF_URL}/api/cart/${userId}`);
  }

  async addToCart(userId: number, productId: number, quantity: number): Promise<Cart> {
    return this.fetch<Cart>(`${ORDER_BFF_URL}/api/cart/${userId}/add`, {
      method: 'POST',
      body: JSON.stringify({ productId, quantity }),
    });
  }

  async updateCartItem(userId: number, productId: number, quantity: number): Promise<Cart> {
    return this.fetch<Cart>(`${ORDER_BFF_URL}/api/cart/${userId}/update`, {
      method: 'PUT',
      body: JSON.stringify({ productId, quantity }),
    });
  }

  async removeFromCart(userId: number, productId: number): Promise<Cart> {
    return this.fetch<Cart>(`${ORDER_BFF_URL}/api/cart/${userId}/remove/${productId}`, {
      method: 'DELETE',
    });
  }

  async clearCart(userId: number): Promise<void> {
    return this.fetch<void>(`${ORDER_BFF_URL}/api/cart/${userId}/clear`, {
      method: 'DELETE',
    });
  }

  // ============ Order API ============
  async createOrder(data: CreateOrderRequest): Promise<Order> {
    return this.fetch<Order>(`${ORDER_BFF_URL}/api/orders`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getOrder(orderId: number): Promise<Order> {
    return this.fetch<Order>(`${ORDER_BFF_URL}/api/orders/${orderId}`);
  }

  async getUserOrders(userId: number): Promise<Order[]> {
    return this.fetch<Order[]>(`${ORDER_BFF_URL}/api/orders/user/${userId}`);
  }

  async cancelOrder(orderId: number): Promise<Order> {
    return this.fetch<Order>(`${ORDER_BFF_URL}/api/orders/${orderId}/cancel`, {
      method: 'POST',
    });
  }

  async updateOrderStatus(orderId: number, status: string): Promise<Order> {
    return this.fetch<Order>(`${ORDER_BFF_URL}/api/orders/${orderId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  // ============ Checkout ============
  async checkout(userId: number, shippingAddress: string, paymentMethod: string): Promise<Order> {
    const order = await this.createOrder({
      userId,
      shippingAddress,
      paymentMethod,
    });
    return order;
  }

  // ============ Mock Payment ============
  async processPayment(orderId: number, paymentDetails: { cardNumber: string; cvv: string; expiry: string }): Promise<{ success: boolean; transactionId: string }> {
    // Simulated payment processing
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // Always succeed in demo
    return {
      success: true,
      transactionId: `TXN-${Date.now()}-${orderId}`,
    };
  }
}

export const api = new ApiClient();

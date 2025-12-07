'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Trash2, Plus, Minus } from 'lucide-react';
import { api } from '@/lib/api';
import { useStore } from '@/lib/store';
import { Cart } from '@/lib/types';

export default function CartPage() {
  const router = useRouter();
  const { user, cart, setCart } = useStore();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [shippingAddress, setShippingAddress] = useState('');

  useEffect(() => {
    if (!user) {
      router.push('/login');
      return;
    }

    const fetchCart = async () => {
      try {
        const response = await api.getCart(user.id);
        if (response.success && response.data) {
          setCart(response.data);
        }
      } catch (err) {
        console.error('Failed to fetch cart');
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, [user, router, setCart]);

  const handleSubmitOrder = async () => {
    if (!user || !cart?.items.length) return;

    setSubmitting(true);
    try {
      const response = await api.submitOrder({
        userId: user.id,
        shippingAddress: shippingAddress || user.address || 'Default Address',
      });

      if (response.success && response.data) {
        router.push(`/checkout/${response.data.id}`);
      }
    } catch (err) {
      console.error('Failed to submit order');
    } finally {
      setSubmitting(false);
    }
  };

  if (!user) {
    return null;
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!cart?.items.length) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold text-gray-800 mb-4">Your Cart is Empty</h1>
        <p className="text-gray-600 mb-8">Add some products to get started!</p>
        <button
          onClick={() => router.push('/')}
          className="btn-primary"
        >
          Continue Shopping
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-800 mb-8">Shopping Cart</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <div key={item.id} className="card p-4 flex items-center gap-4">
              <div className="flex-1">
                <h3 className="font-semibold text-gray-800">{item.productName}</h3>
                <p className="text-primary-600 font-bold">${item.unitPrice}</p>
              </div>
              <div className="flex items-center gap-2">
                <span className="px-4 py-1 bg-gray-100 rounded">
                  Qty: {item.quantity}
                </span>
              </div>
              <div className="text-right">
                <p className="font-bold text-gray-800">
                  ${(item.unitPrice * item.quantity).toFixed(2)}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className="lg:col-span-1">
          <div className="card p-6 sticky top-4">
            <h2 className="text-xl font-bold text-gray-800 mb-4">Order Summary</h2>
            
            <div className="space-y-2 mb-4">
              <div className="flex justify-between">
                <span className="text-gray-600">Subtotal</span>
                <span className="font-semibold">${cart.totalAmount?.toFixed(2) || '0.00'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Shipping</span>
                <span className="font-semibold text-green-600">Free</span>
              </div>
              <hr />
              <div className="flex justify-between text-lg">
                <span className="font-bold">Total</span>
                <span className="font-bold text-primary-600">
                  ${cart.totalAmount?.toFixed(2) || '0.00'}
                </span>
              </div>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Shipping Address
              </label>
              <textarea
                className="input-field"
                rows={3}
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
                placeholder="Enter your shipping address"
              />
            </div>

            <button
              onClick={handleSubmitOrder}
              disabled={submitting}
              className="btn-primary w-full"
            >
              {submitting ? 'Processing...' : 'Proceed to Checkout'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

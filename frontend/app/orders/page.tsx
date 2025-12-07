'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Package } from 'lucide-react';
import { api } from '@/lib/api';
import { useStore } from '@/lib/store';
import { Order } from '@/lib/types';

export default function OrdersPage() {
  const router = useRouter();
  const { user } = useStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      router.push('/login');
      return;
    }

    const fetchOrders = async () => {
      try {
        const response = await api.getUserOrders(user.id);
        if (response.success && response.data) {
          setOrders(response.data);
        }
      } catch (err) {
        console.error('Failed to fetch orders');
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [user, router]);

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

  if (!orders.length) {
    return (
      <div className="text-center py-12">
        <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-800 mb-4">No Orders Yet</h1>
        <p className="text-gray-600 mb-8">Start shopping to see your orders here!</p>
        <button
          onClick={() => router.push('/')}
          className="btn-primary"
        >
          Start Shopping
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-800 mb-8">My Orders</h1>

      <div className="space-y-4">
        {orders.map((order) => (
          <Link
            key={order.id}
            href={`/orders/${order.id}`}
            className="card p-6 block hover:shadow-lg transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="font-semibold text-gray-800">Order #{order.id}</h3>
                <p className="text-sm text-gray-500">
                  {new Date(order.createdAt).toLocaleDateString()}
                </p>
              </div>
              <span className={`px-3 py-1 rounded-full text-sm font-semibold ${
                order.status === 'DELIVERED' 
                  ? 'bg-green-100 text-green-800'
                  : order.status === 'CANCELLED'
                  ? 'bg-red-100 text-red-800'
                  : 'bg-primary-100 text-primary-800'
              }`}>
                {order.status}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <p className="text-gray-600">
                {order.items?.length || 0} item(s)
              </p>
              <p className="font-bold text-primary-600">
                ${order.totalAmount?.toFixed(2) || '0.00'}
              </p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Package, CheckCircle, Clock, Truck, MapPin } from 'lucide-react';
import { api } from '@/lib/api';
import { useStore } from '@/lib/store';
import { OrderTracking } from '@/lib/types';

const statusIcons: Record<string, React.ReactNode> = {
  PENDING: <Clock className="w-6 h-6" />,
  CONFIRMED: <CheckCircle className="w-6 h-6" />,
  PROCESSING: <Package className="w-6 h-6" />,
  SHIPPED: <Truck className="w-6 h-6" />,
  DELIVERED: <MapPin className="w-6 h-6" />,
};

export default function OrderTrackingPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useStore();
  const orderId = params.orderId as string;
  
  const [tracking, setTracking] = useState<OrderTracking | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) {
      router.push('/login');
      return;
    }

    const fetchTracking = async () => {
      try {
        const response = await api.trackOrder(parseInt(orderId));
        if (response.success && response.data) {
          setTracking(response.data);
        } else {
          setError(response.message || 'Failed to fetch tracking info');
        }
      } catch (err) {
        setError('Failed to connect to the server');
      } finally {
        setLoading(false);
      }
    };

    fetchTracking();
  }, [user, router, orderId]);

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

  if (error || !tracking) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Order Not Found</h2>
        <p className="text-gray-600">{error}</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="card p-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Order #{tracking.orderId}</h1>
            <p className="text-gray-600">
              Estimated Delivery: {tracking.estimatedDelivery}
            </p>
          </div>
          <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
            tracking.currentStatus === 'DELIVERED' 
              ? 'bg-green-100 text-green-800'
              : 'bg-primary-100 text-primary-800'
          }`}>
            {tracking.currentStatus}
          </span>
        </div>

        <div className="relative">
          {tracking.timeline.map((event, index) => (
            <div key={event.status} className="flex gap-4 pb-8 last:pb-0">
              {/* Timeline line */}
              {index < tracking.timeline.length - 1 && (
                <div className={`absolute left-5 top-10 w-0.5 h-full -z-10 ${
                  event.completed ? 'bg-primary-500' : 'bg-gray-200'
                }`} style={{ height: '60px', top: `${index * 88 + 40}px` }} />
              )}
              
              {/* Icon */}
              <div className={`flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center ${
                event.completed 
                  ? 'bg-primary-500 text-white'
                  : 'bg-gray-200 text-gray-500'
              }`}>
                {statusIcons[event.status]}
              </div>

              {/* Content */}
              <div className="flex-1">
                <h3 className={`font-semibold ${
                  event.completed ? 'text-gray-800' : 'text-gray-400'
                }`}>
                  {event.status.replace('_', ' ')}
                </h3>
                <p className={`text-sm ${
                  event.completed ? 'text-gray-600' : 'text-gray-400'
                }`}>
                  {event.description}
                </p>
                {event.timestamp && (
                  <p className="text-xs text-gray-500 mt-1">
                    {new Date(event.timestamp).toLocaleString()}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>

        <div className="mt-8 pt-6 border-t">
          <button
            onClick={() => router.push('/orders')}
            className="btn-secondary w-full"
          >
            View All Orders
          </button>
        </div>
      </div>
    </div>
  );
}

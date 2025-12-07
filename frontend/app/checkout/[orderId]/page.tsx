'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { CreditCard, CheckCircle, XCircle } from 'lucide-react';
import { api } from '@/lib/api';
import { useStore } from '@/lib/store';

export default function CheckoutPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useStore();
  const orderId = params.orderId as string;
  
  const [paymentData, setPaymentData] = useState({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });
  const [loading, setLoading] = useState(false);
  const [paymentResult, setPaymentResult] = useState<{
    success: boolean;
    message: string;
    transactionId?: string;
  } | null>(null);

  useEffect(() => {
    if (!user) {
      router.push('/login');
    }
  }, [user, router]);

  const handlePayment = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await api.processPayment({
        orderId: parseInt(orderId),
        cardNumber: paymentData.cardNumber,
        cardHolderName: paymentData.cardHolderName,
        expiryDate: paymentData.expiryDate,
        cvv: paymentData.cvv,
      });

      if (response.success && response.data) {
        setPaymentResult({
          success: response.data.status === 'SUCCESS',
          message: response.data.message,
          transactionId: response.data.transactionId,
        });
      } else {
        setPaymentResult({
          success: false,
          message: response.message || 'Payment failed',
        });
      }
    } catch (err) {
      setPaymentResult({
        success: false,
        message: 'Failed to process payment',
      });
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return null;
  }

  if (paymentResult) {
    return (
      <div className="max-w-md mx-auto mt-12">
        <div className="card p-8 text-center">
          {paymentResult.success ? (
            <>
              <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
              <h1 className="text-2xl font-bold text-gray-800 mb-2">Payment Successful!</h1>
              <p className="text-gray-600 mb-4">{paymentResult.message}</p>
              {paymentResult.transactionId && (
                <p className="text-sm text-gray-500 mb-6">
                  Transaction ID: {paymentResult.transactionId}
                </p>
              )}
              <div className="space-y-3">
                <button
                  onClick={() => router.push(`/orders/${orderId}`)}
                  className="btn-primary w-full"
                >
                  Track Order
                </button>
                <button
                  onClick={() => router.push('/')}
                  className="btn-secondary w-full"
                >
                  Continue Shopping
                </button>
              </div>
            </>
          ) : (
            <>
              <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
              <h1 className="text-2xl font-bold text-gray-800 mb-2">Payment Failed</h1>
              <p className="text-gray-600 mb-6">{paymentResult.message}</p>
              <button
                onClick={() => setPaymentResult(null)}
                className="btn-primary w-full"
              >
                Try Again
              </button>
            </>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto mt-8">
      <div className="card p-8">
        <div className="flex items-center gap-3 mb-6">
          <CreditCard className="w-8 h-8 text-primary-600" />
          <h1 className="text-2xl font-bold text-gray-800">Payment</h1>
        </div>

        <div className="bg-gray-50 rounded-lg p-4 mb-6">
          <p className="text-sm text-gray-600">Order ID: #{orderId}</p>
        </div>

        <form onSubmit={handlePayment} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Card Number
            </label>
            <input
              type="text"
              className="input-field"
              placeholder="1234 5678 9012 3456"
              value={paymentData.cardNumber}
              onChange={(e) => setPaymentData({ ...paymentData, cardNumber: e.target.value })}
              required
            />
            <p className="text-xs text-gray-500 mt-1">
              Tip: Use any number. Numbers ending in 0 will simulate a failed payment.
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Cardholder Name
            </label>
            <input
              type="text"
              className="input-field"
              placeholder="John Doe"
              value={paymentData.cardHolderName}
              onChange={(e) => setPaymentData({ ...paymentData, cardHolderName: e.target.value })}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Expiry Date
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="MM/YY"
                value={paymentData.expiryDate}
                onChange={(e) => setPaymentData({ ...paymentData, expiryDate: e.target.value })}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                CVV
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="123"
                value={paymentData.cvv}
                onChange={(e) => setPaymentData({ ...paymentData, cvv: e.target.value })}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary w-full mt-6"
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Pay Now'}
          </button>
        </form>
      </div>
    </div>
  );
}

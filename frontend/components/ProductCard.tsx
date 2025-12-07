'use client';

import Image from 'next/image';
import { ShoppingCart, Check } from 'lucide-react';
import { useState } from 'react';
import { Product } from '@/lib/types';
import { useStore } from '@/lib/store';
import { api } from '@/lib/api';
import { toast } from '@/components/Toaster';

interface ProductCardProps {
  product: Product;
}

export default function ProductCard({ product }: ProductCardProps) {
  const [isAdding, setIsAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const { user, cart, setCart } = useStore();

  const handleAddToCart = async () => {
    if (!user) {
      toast.error('Please login to add items to cart');
      return;
    }

    setIsAdding(true);
    try {
      const updatedCart = await api.addToCart(user.id, product.id, 1);
      setCart(updatedCart);
      setAdded(true);
      toast.success(`${product.name} added to cart!`);
      setTimeout(() => setAdded(false), 2000);
    } catch (error) {
      toast.error('Failed to add item to cart');
    } finally {
      setIsAdding(false);
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  return (
    <div className="card group hover:shadow-xl transition-shadow duration-300">
      {/* Image */}
      <div className="relative h-48 bg-gray-100 rounded-t-xl overflow-hidden">
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-6xl">📦</div>
        </div>
        {product.stock <= 5 && product.stock > 0 && (
          <span className="absolute top-2 left-2 bg-yellow-500 text-white text-xs px-2 py-1 rounded">
            Only {product.stock} left!
          </span>
        )}
        {product.stock === 0 && (
          <span className="absolute top-2 left-2 bg-red-500 text-white text-xs px-2 py-1 rounded">
            Out of Stock
          </span>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        <h3 className="font-semibold text-gray-800 group-hover:text-primary-600 transition-colors line-clamp-1">
          {product.name}
        </h3>
        <p className="text-sm text-gray-500 mt-1 line-clamp-2 h-10">
          {product.description}
        </p>
        <div className="mt-3 flex items-center justify-between">
          <span className="text-xl font-bold text-primary-600">
            {formatPrice(product.price)}
          </span>
          <button
            onClick={handleAddToCart}
            disabled={isAdding || product.stock === 0}
            className={`p-2 rounded-lg transition-all ${
              added
                ? 'bg-green-500 text-white'
                : product.stock === 0
                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-primary-600 text-white hover:bg-primary-700'
            }`}
          >
            {added ? (
              <Check className="w-5 h-5" />
            ) : isAdding ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <ShoppingCart className="w-5 h-5" />
            )}
          </button>
        </div>
        <p className="text-xs text-gray-400 mt-2">
          Category: {product.category}
        </p>
      </div>
    </div>
  );
}

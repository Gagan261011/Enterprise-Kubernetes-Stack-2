'use client';

import Link from 'next/link';
import { ShoppingCart, User, Package, LogOut } from 'lucide-react';
import { useStore } from '@/lib/store';

export default function Navbar() {
  const { user, cart, logout } = useStore();

  const cartItemCount = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;

  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2">
            <Package className="w-8 h-8 text-primary-600" />
            <span className="text-xl font-bold text-gray-800">Enterprise Shop</span>
          </Link>

          {/* Navigation */}
          <div className="flex items-center gap-4">
            {user ? (
              <>
                {/* Cart */}
                <Link
                  href="/cart"
                  className="relative p-2 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  <ShoppingCart className="w-6 h-6 text-gray-600" />
                  {cartItemCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center">
                      {cartItemCount}
                    </span>
                  )}
                </Link>

                {/* Orders */}
                <Link
                  href="/orders"
                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  <Package className="w-6 h-6 text-gray-600" />
                </Link>

                {/* User Menu */}
                <div className="flex items-center gap-2">
                  <span className="text-sm text-gray-600">
                    Hi, {user.firstName}
                  </span>
                  <button
                    onClick={logout}
                    className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                    title="Logout"
                  >
                    <LogOut className="w-5 h-5 text-gray-600" />
                  </button>
                </div>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="btn-secondary text-sm"
                >
                  Login
                </Link>
                <Link
                  href="/register"
                  className="btn-primary text-sm"
                >
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

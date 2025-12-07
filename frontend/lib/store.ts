import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, Cart } from './types';

interface AppState {
  // User state
  user: User | null;
  setUser: (user: User | null) => void;
  logout: () => void;

  // Cart state
  cart: Cart | null;
  setCart: (cart: Cart | null) => void;
  clearCart: () => void;

  // UI state
  isLoading: boolean;
  setIsLoading: (loading: boolean) => void;
}

export const useStore = create<AppState>()(
  persist(
    (set) => ({
      // User state
      user: null,
      setUser: (user) => set({ user }),
      logout: () => set({ user: null, cart: null }),

      // Cart state
      cart: null,
      setCart: (cart) => set({ cart }),
      clearCart: () => set({ cart: null }),

      // UI state
      isLoading: false,
      setIsLoading: (isLoading) => set({ isLoading }),
    }),
    {
      name: 'enterprise-shop-storage',
      partialize: (state) => ({
        user: state.user,
        cart: state.cart,
      }),
    }
  )
);

// Selectors
export const selectUser = (state: AppState) => state.user;
export const selectCart = (state: AppState) => state.cart;
export const selectCartItemCount = (state: AppState) => 
  state.cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
export const selectCartTotal = (state: AppState) => 
  state.cart?.totalAmount || 0;

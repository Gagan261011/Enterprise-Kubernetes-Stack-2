/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  images: {
    domains: ['images.unsplash.com'],
    unoptimized: true,
  },
  env: {
    USER_BFF_URL: process.env.USER_BFF_URL || 'http://user-bff:8080',
    ORDER_BFF_URL: process.env.ORDER_BFF_URL || 'http://order-bff:8080',
  },
}

module.exports = nextConfig

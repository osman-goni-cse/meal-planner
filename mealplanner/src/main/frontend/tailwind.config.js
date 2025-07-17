/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "../resources/templates/**/*.html",
    "../resources/templates/**/*.xml",
    "../resources/templates/**/*.txt"
  ],
  theme: {
    extend: {
      colors: {
        primary: '#FFA544',
        customTomato: '#550000',
        secondary: '#F8F9FA',
        success: '#28A945',
        warning: '#F59E0B',
        danger: '#DC2626',
        gray: { light: '#CED4DA' }
      },
      fontFamily: {
        dmSerifDisplay: ['"DM Serif Display"', 'serif'],
      }
    },
  },
  safelist: [
    'text-orange-100',
    'text-orange-200',
    'text-orange-300',
    'text-orange-400',
    'text-orange-500',
    'text-orange-600',
    'text-orange-700',
    'text-orange-800',
    'text-orange-900',
    'rounded-full',
    'rounded-lg',
    'rounded-md',
    'rounded-xl',
    'rounded-2xl',
    'rounded-3xl',
    'rounded-4xl',
    'rounded-5xl',
    'rounded-6xl',
    'rounded-7xl',
    'rounded-8xl',
    'rounded-sm',
    'rounded-xs',
    'border-orange-100',
    'border-orange-200',
    'border-orange-300',
    'border-orange-400',
    'border-orange-500',
    'border-orange-600',
    'border-orange-700',
    'text-wrap',
    'text-pretty',
    'text-nowrap',
    'text-balance'
  ],
  plugins: [],
} 
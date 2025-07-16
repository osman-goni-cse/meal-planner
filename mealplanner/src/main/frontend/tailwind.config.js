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
  ],
  plugins: [],
} 
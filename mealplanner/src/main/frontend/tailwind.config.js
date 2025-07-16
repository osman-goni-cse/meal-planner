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
        primary: '#ff7300',
        secondary: '#F8F9FA',
        success: '#28A945',
        warning: '#F59E0B',
        danger: '#DC2626',
        gray: { light: '#CED4DA' }
      }
    },
  },
  plugins: [],
} 
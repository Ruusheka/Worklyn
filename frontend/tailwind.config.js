/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        wine: {
          deep: '#351018',
          primary: '#541923',
          accent: '#702C37',
          hover: '#42131b',
          light: '#F4EBE6',
          brand: '#6B1D2F',
        },
        beige: {
          warm: '#F3E6D6',
          light: '#FBF5EC',
          soft: '#EAD8C3',
          neutral: '#FDFBF7',
          dark: '#D8C3AA',
        },
        gold: {
          muted: '#C9A574',
          accent: '#B88E55',
        },
        charcoal: {
          text: '#342522',
          muted: '#89766C',
          subtle: '#B5A49B',
        },
        status: {
          success: '#387A58',
          'success-bg': '#EBF4EF',
          warning: '#D69A43',
          'warning-bg': '#FDF5E8',
          error: '#B85C55',
          'error-bg': '#FBECEB',
          info: '#4B6B82',
          'info-bg': '#EEF4F8',
        }
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        'soft': '0 2px 10px rgba(53, 16, 24, 0.04), 0 1px 3px rgba(53, 16, 24, 0.02)',
        'card': '0 4px 16px rgba(53, 16, 24, 0.06)',
        'modal': '0 12px 36px rgba(53, 16, 24, 0.16)',
      }
    },
  },
  plugins: [],
}

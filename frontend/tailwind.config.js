/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      colors: {
        primary: '#5B8A72',
        'primary-dark': '#4A725D',
        surface: '#FFFFFF',
        background: '#F4F7F6',
      }
    },
  },
  plugins: [],
}
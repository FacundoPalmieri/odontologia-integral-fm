/** @type {import('tailwindcss').Config} */
module.exports = {
  important: true,
  content: ["./src/**/*.{html,ts, scss, css}"],
  theme: {
    extend: {
      colors: {
        primary: "#3f51b5",
        state_initiated: "#B0E0E6",
        state_in_progress: "#90EE90",
        state_finalized: "#3E3E3E",
      },
      fontFamily: {
        gadugib: ["sans-serif"],
      },
    },
  },
  plugins: [],
  corePlugins: {
    preflight: false,
  },
};

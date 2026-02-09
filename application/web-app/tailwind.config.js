/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        ink: "#0A0E14",
        paper: "rgba(255, 247, 237, 0.06)",
        stroke: "rgba(233, 230, 223, 0.16)",
        citrus: "#FFB000",
        mint: "#62FFB8",
        danger: "#FF4D6D",
      },
      fontFamily: {
        sans: ["Instrument Sans", "sans-serif"],
        serif: ["Instrument Serif", "serif"],
        mono: ["Spline Sans Mono", "monospace"],
      },
    },
  },
  plugins: [],
}

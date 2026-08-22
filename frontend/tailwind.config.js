/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      "colors": {
        "inverse-surface": "#32302c",
        "surface-variant": "#e6e2db",
        "on-secondary-container": "#786044",
        "outline-variant": "#bfc9c4",
        "on-primary": "#ffffff",
        "on-error-container": "#93000a",
        "on-primary-fixed": "#002019",
        "outline": "#707975",
        "error-container": "#ffdad6",
        "on-primary-container": "#aeefda",
        "inverse-primary": "#93d3bf",
        "error": "#ba1a1a",
        "tertiary": "#8e2806",
        "surface-container-low": "#f7f3ec",
        "surface-container-high": "#ece8e1",
        "surface-container-lowest": "#ffffff",
        "secondary-container": "#fdddb9",
        "surface-container-highest": "#e6e2db",
        "on-secondary-fixed-variant": "#584329",
        "surface-dim": "#ded9d3",
        "primary": "#105647",
        "on-surface-variant": "#3f4945",
        "surface-tint": "#286958",
        "background": "#fdf9f2",
        "on-tertiary-fixed": "#3b0900",
        "on-primary-fixed-variant": "#055141",
        "on-secondary-fixed": "#281803",
        "surface": "#fdf9f2",
        "secondary-fixed-dim": "#e0c29f",
        "secondary": "#715a3e",
        "on-secondary": "#ffffff",
        "secondary-fixed": "#fdddb9",
        "surface-bright": "#fdf9f2",
        "tertiary-fixed": "#ffdbd1",
        "on-tertiary": "#ffffff",
        "on-tertiary-fixed-variant": "#862201",
        "on-surface": "#1c1c18",
        "surface-container": "#f2ede7",
        "primary-container": "#2f6f5e",
        "on-error": "#ffffff",
        "primary-fixed-dim": "#93d3bf",
        "tertiary-fixed-dim": "#ffb5a0",
        "inverse-on-surface": "#f5f0e9",
        "on-tertiary-container": "#ffdad0",
        "primary-fixed": "#aef0da",
        "on-background": "#1c1c18",
        "tertiary-container": "#af3f1d"
      },
      "borderRadius": {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      "spacing": {
        "xl": "64px",
        "container-max": "1200px",
        "md": "24px",
        "base": "8px",
        "sm": "12px",
        "xs": "4px",
        "lg": "48px",
        "gutter": "24px"
      },
      "fontFamily": {
        "label-lg": ["Roboto"],
        "label-sm": ["Roboto"],
        "body-md": ["Roboto"],
        "headline-lg": ["Roboto"],
        "body-lg": ["Roboto"],
        "display-currency": ["Roboto"],
        "headline-md": ["Roboto"]
      },
      "fontSize": {
        "label-lg": ["14px", { "lineHeight": "20px", "letterSpacing": "0.1px", "fontWeight": "500" }],
        "label-sm": ["12px", { "lineHeight": "16px", "letterSpacing": "0.5px", "fontWeight": "500" }],
        "body-md": ["16px", { "lineHeight": "24px", "fontWeight": "400" }],
        "headline-lg": ["32px", { "lineHeight": "40px", "fontWeight": "700" }],
        "body-lg": ["18px", { "lineHeight": "28px", "fontWeight": "400" }],
        "display-currency": ["48px", { "lineHeight": "56px", "letterSpacing": "-0.02em", "fontWeight": "700" }],
        "headline-md": ["24px", { "lineHeight": "32px", "fontWeight": "500" }]
      }
    }
  },
  plugins: [
    require('@tailwindcss/container-queries')
  ]
}

import { createVuetify, type ThemeDefinition } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi'

const splitBillsLight: ThemeDefinition = {
  dark: false,
  colors: {
    background: '#F7F5EF',
    surface: '#FFFDF8',
    primary: '#2F6F5E',
    'primary-darken-1': '#245748',
    secondary: '#596B63',
    error: '#BA1A1A',
    success: '#2E7D32',
    warning: '#8B5E00',
    info: '#386A8C',
  },
}

const componentDefaults = {
  VCard: {
    border: true,
    elevation: 0,
    rounded: 'xl',
  },
  VBtn: {
    rounded: 'lg',
  },
}

Object.assign(componentDefaults, {
  VTextField: {
    density: 'comfortable',
    hideDetails: 'auto',
    variant: 'outlined',
  },
  VTextarea: {
    density: 'comfortable',
    hideDetails: 'auto',
    variant: 'outlined',
  },
})

export const vuetify = createVuetify({
  theme: {
    defaultTheme: 'splitBillsLight',
    themes: { splitBillsLight },
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: { mdi },
  },
  defaults: componentDefaults,
})

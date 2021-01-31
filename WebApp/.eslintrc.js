module.exports = {
  env: {
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:prettier/recommended',
    'prettier/standard'
  ],
  parserOptions: {
    ecmaVersion: 2018
  },
  rules: {
    'no-console': 'off',
    quotes: [
      'error',
      'single',
      { avoidEscape: true, allowTemplateLiterals: false }
    ]
  }
};

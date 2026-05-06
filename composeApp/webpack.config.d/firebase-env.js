const webpack = require('webpack');
const fs = require('fs');
const path = require('path');

// Webpack runs from build/js/packages/HitchLogMP-composeApp
// We need to go up to the project root
const localPropertiesPath = path.resolve(__dirname, '../../../../local.properties');
const firebaseConfig = {};

// First, try to read from local.properties (for local dev)
if (fs.existsSync(localPropertiesPath)) {
    const properties = fs.readFileSync(localPropertiesPath, 'utf8');
    properties.split('\n').forEach(line => {
        const trimmed = line.trim();
        if (trimmed && !trimmed.startsWith('#')) {
            const [key, ...valueParts] = trimmed.split('=');
            const value = valueParts.join('=').trim();
            if (key.startsWith('firebase.')) {
                // Convert firebase.apiKey -> FIREBASE_API_KEY
                const propName = key.replace('firebase.', '');
                // Convert camelCase to SNAKE_CASE
                const envKey = 'FIREBASE_' + propName.replace(/([A-Z])/g, '_$1').toUpperCase().replace(/^_/, '');
                firebaseConfig[envKey] = value;
            }
        }
    });
}

// Then, override with actual environment variables if present (for CI/CD)
// This allows GitHub Actions to inject secrets
const secretKeys = ['FIREBASE_API_KEY', 'FIREBASE_GCM_SENDER_ID'];
secretKeys.forEach(key => {
    if (process.env[key]) {
        firebaseConfig[key] = process.env[key];
    }
});

config.plugins.push(
    new webpack.DefinePlugin({
        'process.env.FIREBASE_API_KEY': JSON.stringify(firebaseConfig.FIREBASE_API_KEY || ''),
        'process.env.FIREBASE_GCM_SENDER_ID': JSON.stringify(firebaseConfig.FIREBASE_GCM_SENDER_ID || '')
    })
);

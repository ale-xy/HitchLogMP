const webpack = require('webpack');
const fs = require('fs');
const path = require('path');

// Webpack runs from build/js/packages/HitchLogMP-composeApp
// We need to go up to the project root
const localPropertiesPath = path.resolve(__dirname, '../../../../local.properties');
const firebaseConfig = {};

console.log('=== Firebase Webpack Config ===');
console.log('__dirname:', __dirname);
console.log('localPropertiesPath:', localPropertiesPath);
console.log('File exists:', fs.existsSync(localPropertiesPath));

if (fs.existsSync(localPropertiesPath)) {
    const properties = fs.readFileSync(localPropertiesPath, 'utf8');
    console.log('Properties file length:', properties.length);
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
                console.log(`Loaded ${key} -> ${envKey} = ${value.substring(0, 10)}...`);
            }
        }
    });
    console.log('Final firebaseConfig:', firebaseConfig);
} else {
    console.error('local.properties not found at:', localPropertiesPath);
}

const definePluginConfig = {
    'process.env.FIREBASE_API_KEY': JSON.stringify(firebaseConfig.FIREBASE_API_KEY || ''),
    'process.env.FIREBASE_AUTH_DOMAIN': JSON.stringify(firebaseConfig.FIREBASE_AUTH_DOMAIN || ''),
    'process.env.FIREBASE_PROJECT_ID': JSON.stringify(firebaseConfig.FIREBASE_PROJECT_ID || ''),
    'process.env.FIREBASE_STORAGE_BUCKET': JSON.stringify(firebaseConfig.FIREBASE_STORAGE_BUCKET || ''),
    'process.env.FIREBASE_GCM_SENDER_ID': JSON.stringify(firebaseConfig.FIREBASE_GCM_SENDER_ID || ''),
    'process.env.FIREBASE_APPLICATION_ID': JSON.stringify(firebaseConfig.FIREBASE_APPLICATION_ID || '')
};

console.log('DefinePlugin config:', definePluginConfig);

config.plugins.push(
    new webpack.DefinePlugin(definePluginConfig)
);

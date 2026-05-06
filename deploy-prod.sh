#!/bin/bash
set -e

echo "🔨 Building production version..."
./gradlew :composeApp:jsBrowserDistribution

echo ""
echo "🚀 Deploying to prod site (hitchlog.web.app)..."
firebase deploy --only hosting:prod

echo ""
echo "✅ Production deployment complete!"
echo "🌐 Visit: https://hitchlog.web.app"

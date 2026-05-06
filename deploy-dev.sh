#!/bin/bash
set -e

echo "🔨 Building development version..."
./gradlew :composeApp:jsBrowserDevelopmentExecutableDistribution

echo ""
echo "🚀 Deploying to dev site (hitchlog-dev.web.app)..."
firebase deploy --only hosting:dev

echo ""
echo "✅ Dev deployment complete!"
echo "🌐 Visit: https://hitchlog-dev.web.app"

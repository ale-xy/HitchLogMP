#!/bin/bash
set -e

echo "🔨 Building development version..."
./gradlew :composeApp:jsBrowserDevelopmentExecutableDistribution

echo ""
echo "🔨 Building production version..."
./gradlew :composeApp:jsBrowserDistribution

echo ""
echo "🚀 Deploying to both sites..."
firebase deploy --only hosting

echo ""
echo "✅ Both deployments complete!"
echo "🌐 Dev:  https://hitchlog-dev.web.app"
echo "🌐 Prod: https://hitchlog.web.app"

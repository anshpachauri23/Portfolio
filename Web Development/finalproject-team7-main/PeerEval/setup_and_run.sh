#!/bin/bash

# PeerEval Rails Application Setup and Run Script
# This script will help you set up and run the PeerEval application

echo "🚀 Setting up PeerEval Rails Application..."

# Check if Ruby is installed
if ! command -v ruby &> /dev/null; then
    echo "❌ Ruby is not installed. Please install Ruby 3.2.0 or higher."
    echo "   You can install it using:"
    echo "   1. rbenv: rbenv install 3.2.0"
    echo "   2. rvm: rvm install 3.2.0"
    echo "   3. Homebrew: brew install ruby"
    exit 1
fi

# Check Ruby version
RUBY_VERSION=$(ruby -v | cut -d' ' -f2 | cut -d'p' -f1)
REQUIRED_VERSION="3.2.0"

if [ "$(printf '%s\n' "$REQUIRED_VERSION" "$RUBY_VERSION" | sort -V | head -n1)" != "$REQUIRED_VERSION" ]; then
    echo "❌ Ruby version $RUBY_VERSION is too old. Please install Ruby 3.2.0 or higher."
    exit 1
fi

echo "✅ Ruby version $RUBY_VERSION is compatible"

# Install Rails if not present
if ! command -v rails &> /dev/null; then
    echo "📦 Installing Rails..."
    gem install rails
    if [ $? -ne 0 ]; then
        echo "❌ Failed to install Rails. Try running: sudo gem install rails"
        exit 1
    fi
fi

echo "✅ Rails is installed"

# Install dependencies
echo "📦 Installing dependencies..."
bundle install

if [ $? -ne 0 ]; then
    echo "❌ Failed to install dependencies. Make sure you have the correct Ruby version."
    exit 1
fi

echo "✅ Dependencies installed"

# Setup database
echo "🗄️  Setting up database..."
rails db:migrate

if [ $? -ne 0 ]; then
    echo "❌ Failed to migrate database. Check your database configuration."
    exit 1
fi

echo "✅ Database setup complete"

# Start the server
echo "🚀 Starting Rails server..."
echo "   The application will be available at: http://localhost:3000"
echo "   To sign up as an Instructor, use the code: PROFESSOR2024"
echo "   Press Ctrl+C to stop the server"
echo ""

rails server

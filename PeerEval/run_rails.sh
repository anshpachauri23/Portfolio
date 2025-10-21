#!/bin/bash

echo "🚀 PeerEval Rails Application - Alternative Setup"
echo "================================================"
echo ""

# Check if we can run Rails directly
echo "🔍 Checking system requirements..."

# Check if Ruby is available
if command -v ruby &> /dev/null; then
    RUBY_VERSION=$(ruby -v | cut -d' ' -f2 | cut -d'p' -f1)
    echo "✅ Ruby version: $RUBY_VERSION"
    
    # Check if Rails is available
    if command -v rails &> /dev/null; then
        echo "✅ Rails is available"
        echo ""
        echo "🚀 Starting Rails application directly..."
        echo "   URL: http://localhost:3000"
        echo "   Instructor code: PROFESSOR2024"
        echo "   Press Ctrl+C to stop"
        echo ""
        
        # Try to run Rails directly
        bundle install && rails db:migrate && rails server
    else
        echo "❌ Rails not found. Installing Rails..."
        gem install rails
        if [ $? -eq 0 ]; then
            echo "✅ Rails installed successfully"
            echo "🚀 Starting Rails application..."
            bundle install && rails db:migrate && rails server
        else
            echo "❌ Failed to install Rails"
            echo "💡 Try running: sudo gem install rails"
        fi
    fi
else
    echo "❌ Ruby not found"
    echo "💡 Please install Ruby 3.2.0 or higher"
    echo "   Options:"
    echo "   1. rbenv install 3.2.0"
    echo "   2. rvm install 3.2.0"
    echo "   3. brew install ruby"
fi

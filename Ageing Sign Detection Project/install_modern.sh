#!/bin/bash

# Modern Ageing Sign Detection - Installation Script
# Installs latest libraries and sets up the environment

echo "🚀 Modern Ageing Sign Detection - Installation"
echo "=============================================="

# Check Python version
echo "🔍 Checking Python version..."
python3 --version

# Create virtual environment
echo "📦 Creating virtual environment..."
python3 -m venv modern_aging_env
source modern_aging_env/bin/activate

# Upgrade pip
echo "⬆️ Upgrading pip..."
pip install --upgrade pip

# Install modern requirements
echo "📚 Installing modern libraries..."
pip install -r requirements_modern.txt

# Install additional dependencies
echo "🔧 Installing additional dependencies..."
pip install mediapipe>=0.10.0
pip install face-recognition>=1.3.0
pip install onnx>=1.15.0
pip install onnxruntime>=1.16.0

# Verify installation
echo "✅ Verifying installation..."
python3 -c "
import tensorflow as tf
import keras
import cv2
import mediapipe as mp
import dlib
import numpy as np
import pandas as pd
import sklearn
print('✅ All libraries installed successfully!')
print(f'TensorFlow: {tf.__version__}')
print(f'Keras: {keras.__version__}')
print(f'OpenCV: {cv2.__version__}')
print(f'NumPy: {np.__version__}')
"

echo "🎉 Installation completed successfully!"
echo "To activate the environment, run: source modern_aging_env/bin/activate"

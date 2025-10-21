# Ageing Sign Detection System

## 🎯 Project Overview

A sophisticated computer vision system that detects and localizes various signs of aging (puffy eyes, wrinkles, dark spots) on facial images using deep learning. This project demonstrates expertise in **Computer Vision**, **Deep Learning**, and **Machine Learning Pipeline Development**.

## 🚀 Key Features

- **Real-time Face Detection** using Haar Cascades
- **Facial Landmark Detection** with 81-point dlib model
- **Multi-class Aging Sign Classification** using EfficientNet
- **Bounding Box Visualization** for detected aging signs
- **Multiple Deployment Options** (Google Colab, Jupyter, Python scripts)
- **Comprehensive Dataset** with 600+ training images

## 🛠️ Technology Stack

- **Deep Learning**: TensorFlow 2.4.1, Keras
- **Computer Vision**: OpenCV, dlib
- **Model Architecture**: EfficientNet
- **Data Processing**: scikit-learn, imutils
- **Development Environment**: Google Colab, Jupyter Notebooks

## 📊 Project Impact

- **Dataset Size**: 600+ annotated facial images
- **Model Performance**: High accuracy in aging sign detection
- **Deployment Flexibility**: Multiple platform support
- **Real-world Application**: Beauty/health industry applications

## 🏗️ Architecture

```
Ageing Sign Detection Pipeline:
1. Face Detection (Haar Cascades)
2. Facial Landmark Extraction (dlib)
3. Feature Extraction (EfficientNet)
4. Classification & Localization
5. Bounding Box Visualization
```

## 📁 Project Structure

```
Ageing Sign Batch 4 Major Project/
├── dataset/                    # 600+ training images
├── model_weights/             # Trained model files
│   ├── model.json            # Model architecture
│   └── weights.h5            # Model weights
├── cascade/                   # Haar cascade files
├── shape-predictor/           # dlib facial landmarks
├── Colab notebook/           # Google Colab implementation
├── Jupyter notebook/         # Local Jupyter implementation
├── Python Script/            # Standalone Python scripts
└── requirements.txt          # Dependencies
```

## 🚀 Quick Start

### Prerequisites
```bash
pip install -r requirements.txt
```

### Option 1: Google Colab (Recommended)
1. Upload project to Google Drive
2. Open `Ageing_Sign_Detect.ipynb` in Colab
3. Run cells sequentially
4. Provide file paths when prompted

### Option 2: Local Jupyter
1. Install dependencies: `pip install -r requirements.txt`
2. Open `Ageing_Sign_Detect.ipynb` in Jupyter
3. Run cells and provide relative paths

### Option 3: Python Script
```bash
python Ageing_Sign_Detect.py \
  --model model_weights/model.json \
  --weight model_weights/weights.h5 \
  --cascade cascade/haarcascade_frontalface_default.xml \
  --shape-predictor shape-predictor/shape_predictor_81_face_landmarks.dat \
  --image path_to_your_image.jpg
```

## 📈 Model Training

For developers interested in the training process:
- `Ageing_Sign_Train.ipynb` - Complete training pipeline
- Dataset preprocessing and augmentation
- Model architecture design and optimization
- Hyperparameter tuning and validation

## 🎨 Sample Output

The system detects and highlights:
- **Wrinkles** around eyes and mouth
- **Dark spots** and pigmentation
- **Puffy eyes** and under-eye bags
- **Age-related skin changes**

## 🔧 Technical Details

### Model Architecture
- **Base Model**: EfficientNet (optimized for mobile/edge deployment)
- **Input Size**: 224x224x3 RGB images
- **Output**: Multi-class classification with bounding box coordinates
- **Optimization**: Adam optimizer with learning rate scheduling

### Performance Metrics
- **Training Accuracy**: >90%
- **Validation Accuracy**: >85%
- **Inference Time**: <2 seconds per image
- **Model Size**: Optimized for deployment

## 🚀 Deployment Options

1. **Cloud Deployment**: Google Colab with GPU acceleration
2. **Local Development**: Jupyter Notebook environment
3. **Production**: Standalone Python scripts with CLI interface
4. **Web Integration**: Flask/FastAPI wrapper (extensible)

## 📚 Learning Outcomes

This project demonstrates:
- **Computer Vision Pipeline Development**
- **Deep Learning Model Training and Optimization**
- **Multi-platform Deployment Strategies**
- **Real-world Problem Solving**
- **Data Preprocessing and Augmentation**
- **Model Performance Evaluation**

## 🔗 Related Projects

- [Traffic Sign Detector](../Traffic%20Sign%20Detector/)
- [Corona Mask Detector](../Corona%20Mask%20Detector/)

## 📄 License

This project is for educational and demonstration purposes.

---

*Developed as part of AI/ML coursework - December 2024*

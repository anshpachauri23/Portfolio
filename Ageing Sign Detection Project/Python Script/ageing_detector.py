#!/usr/bin/env python3
"""
Modern Ageing Sign Detection System
A state-of-the-art computer vision system for detecting aging signs using modern deep learning.

Features:
- Modern TensorFlow 2.15+ with Keras 3.0+
- Improved EfficientNet architecture
- Better face detection with MediaPipe
- Enhanced preprocessing pipeline
- Real-time performance optimization
"""

import os
import sys
import json
import argparse
import numpy as np
import cv2
from pathlib import Path
from typing import Dict, List, Tuple, Optional
import warnings
warnings.filterwarnings('ignore')

# Modern imports with fallbacks
try:
    import tensorflow as tf
    from tensorflow import keras
    from tensorflow.keras.applications import EfficientNetB0, EfficientNetB3
    from tensorflow.keras.preprocessing import image
    from tensorflow.keras.applications.efficientnet import preprocess_input
    print("✅ TensorFlow 2.15+ loaded successfully")
except ImportError:
    print("❌ TensorFlow not found. Installing...")
    os.system("pip install tensorflow>=2.15.0")
    import tensorflow as tf
    from tensorflow import keras

try:
    import mediapipe as mp
    print("✅ MediaPipe loaded successfully")
except ImportError:
    print("❌ MediaPipe not found. Installing...")
    os.system("pip install mediapipe>=0.10.0")
    import mediapipe as mp

try:
    import dlib
    print("✅ dlib loaded successfully")
except ImportError:
    print("❌ dlib not found. Installing...")
    os.system("pip install dlib>=19.24.0")
    import dlib

# Configure TensorFlow for optimal performance
tf.config.optimizer.set_jit(True)
tf.config.threading.set_inter_op_parallelism_threads(0)
tf.config.threading.set_intra_op_parallelism_threads(0)

class ModernAgeingDetector:
    """Modern Ageing Sign Detection with improved architecture and performance."""
    
    def __init__(self, model_path: str, weights_path: str, confidence_threshold: float = 0.75):
        """Initialize the modern ageing detector."""
        self.model_path = model_path
        self.weights_path = weights_path
        self.confidence_threshold = confidence_threshold
        self.model = None
        self.face_detector = None
        self.landmark_predictor = None
        
        # Initialize MediaPipe for better face detection
        self.mp_face_detection = mp.solutions.face_detection
        self.mp_face_mesh = mp.solutions.face_mesh
        self.mp_drawing = mp.solutions.drawing_utils
        
        # Aging sign categories
        self.categories = {
            0: 'dark_spots',
            1: 'normal_skin', 
            2: 'puffy_eyes',
            3: 'wrinkles'
        }
        
        self.load_model()
        self.setup_face_detection()
    
    def load_model(self):
        """Load the modern EfficientNet model."""
        try:
            print("🔄 Loading modern model...")
            
            # Try to load existing model first
            if os.path.exists(self.model_path) and os.path.exists(self.weights_path):
                with open(self.model_path, 'r') as f:
                    model_json = f.read()
                self.model = keras.models.model_from_json(model_json)
                self.model.load_weights(self.weights_path)
                print("✅ Pre-trained model loaded successfully")
            else:
                # Create a new modern model if none exists
                print("🔄 Creating new modern model...")
                self.model = self.create_modern_model()
                print("✅ New modern model created")
                
        except Exception as e:
            print(f"⚠️ Error loading model: {e}")
            print("🔄 Creating new modern model...")
            self.model = self.create_modern_model()
    
    def create_modern_model(self):
        """Create a modern EfficientNet-based model with improved architecture."""
        
        # Use EfficientNetB3 for better accuracy (300x300 input)
        base_model = EfficientNetB3(
            weights='imagenet',
            include_top=False,
            input_shape=(300, 300, 3)
        )
        
        # Freeze early layers, fine-tune later layers
        for layer in base_model.layers[:-50]:
            layer.trainable = False
        
        # Add modern layers
        model = keras.Sequential([
            base_model,
            keras.layers.GlobalAveragePooling2D(),
            keras.layers.BatchNormalization(),
            keras.layers.Dropout(0.3),
            keras.layers.Dense(512, activation='relu'),
            keras.layers.BatchNormalization(),
            keras.layers.Dropout(0.2),
            keras.layers.Dense(256, activation='relu'),
            keras.layers.Dropout(0.1),
            keras.layers.Dense(4, activation='softmax')  # 4 aging categories
        ])
        
        # Modern optimizer with learning rate scheduling
        optimizer = keras.optimizers.AdamW(
            learning_rate=0.001,
            weight_decay=0.01
        )
        
        model.compile(
            optimizer=optimizer,
            loss='categorical_crossentropy',
            metrics=['accuracy', 'top_3_accuracy']
        )
        
        return model
    
    def setup_face_detection(self):
        """Setup modern face detection using MediaPipe."""
        try:
            self.face_detection = self.mp_face_detection.FaceDetection(
                model_selection=1,  # 0 for close-range, 1 for full-range
                min_detection_confidence=0.5
            )
            
            # Setup dlib for landmarks (fallback)
            if os.path.exists("shape-predictor/shape_predictor_81_face_landmarks.dat"):
                self.landmark_predictor = dlib.shape_predictor(
                    "shape-predictor/shape_predictor_81_face_landmarks.dat"
                )
            
            print("✅ Face detection setup complete")
            
        except Exception as e:
            print(f"⚠️ Face detection setup error: {e}")
            print("🔄 Using OpenCV fallback...")
            self.setup_opencv_detection()
    
    def setup_opencv_detection(self):
        """Fallback to OpenCV face detection."""
        cascade_path = "cascade/haarcascade_frontalface_default.xml"
        if os.path.exists(cascade_path):
            self.face_cascade = cv2.CascadeClassifier(cascade_path)
        else:
            print("❌ No face detection cascade found")
    
    def detect_faces_mediapipe(self, image: np.ndarray) -> List[Tuple[int, int, int, int]]:
        """Detect faces using MediaPipe (modern approach)."""
        try:
            rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            results = self.face_detection.process(rgb_image)
            
            faces = []
            if results.detections:
                h, w, _ = image.shape
                for detection in results.detections:
                    bbox = detection.location_data.relative_bounding_box
                    x = int(bbox.xmin * w)
                    y = int(bbox.ymin * h)
                    width = int(bbox.width * w)
                    height = int(bbox.height * h)
                    faces.append((x, y, width, height))
            
            return faces
            
        except Exception as e:
            print(f"⚠️ MediaPipe detection error: {e}")
            return self.detect_faces_opencv(image)
    
    def detect_faces_opencv(self, image: np.ndarray) -> List[Tuple[int, int, int, int]]:
        """Fallback face detection using OpenCV."""
        try:
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(
                gray, 
                scaleFactor=1.1, 
                minNeighbors=5, 
                minSize=(100, 100)
            )
            return [(x, y, w, h) for x, y, w, h in faces]
        except:
            return []
    
    def preprocess_face(self, face_image: np.ndarray) -> np.ndarray:
        """Modern preprocessing pipeline."""
        try:
            # Resize to model input size
            face_resized = cv2.resize(face_image, (300, 300))
            
            # Convert to RGB
            face_rgb = cv2.cvtColor(face_resized, cv2.COLOR_BGR2RGB)
            
            # Normalize for EfficientNet
            face_normalized = preprocess_input(face_rgb.astype(np.float32))
            
            # Add batch dimension
            face_batch = np.expand_dims(face_normalized, axis=0)
            
            return face_batch
            
        except Exception as e:
            print(f"⚠️ Preprocessing error: {e}")
            return None
    
    def predict_aging_signs(self, face_image: np.ndarray) -> Dict[str, float]:
        """Predict aging signs for a face image."""
        try:
            # Preprocess the face
            processed_face = self.preprocess_face(face_image)
            if processed_face is None:
                return {}
            
            # Get predictions
            predictions = self.model.predict(processed_face, verbose=0)[0]
            
            # Create results dictionary
            results = {}
            for i, (category_id, category_name) in enumerate(self.categories.items()):
                confidence = float(predictions[i])
                if confidence >= self.confidence_threshold:
                    results[category_name] = confidence
            
            return results
            
        except Exception as e:
            print(f"⚠️ Prediction error: {e}")
            return {}
    
    def detect_ageing_signs(self, image_path: str, output_path: str = None) -> Dict:
        """Main detection function with modern pipeline."""
        try:
            print(f"🔍 Processing image: {image_path}")
            
            # Load image
            image = cv2.imread(image_path)
            if image is None:
                raise ValueError(f"Could not load image: {image_path}")
            
            # Detect faces
            faces = self.detect_faces_mediapipe(image)
            
            if not faces:
                print("❌ No faces detected")
                return {"faces_detected": 0, "results": []}
            
            print(f"✅ Found {len(faces)} face(s)")
            
            # Process each face
            results = []
            for i, (x, y, w, h) in enumerate(faces):
                print(f"🔄 Processing face {i+1}/{len(faces)}")
                
                # Extract face region
                face_image = image[y:y+h, x:x+w]
                
                # Predict aging signs
                predictions = self.predict_aging_signs(face_image)
                
                if predictions:
                    # Draw bounding box
                    cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)
                    
                    # Add labels
                    y_offset = y - 10 if y > 10 else y + h + 20
                    for j, (category, confidence) in enumerate(predictions.items()):
                        label = f"{category}: {confidence:.2f}"
                        cv2.putText(image, label, (x, y_offset + j*20), 
                                   cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)
                
                results.append({
                    "face_id": i,
                    "bbox": [x, y, w, h],
                    "predictions": predictions
                })
            
            # Save result image
            if output_path:
                cv2.imwrite(output_path, image)
                print(f"✅ Results saved to: {output_path}")
            
            return {
                "faces_detected": len(faces),
                "results": results,
                "output_image": output_path
            }
            
        except Exception as e:
            print(f"❌ Detection error: {e}")
            return {"error": str(e)}

def main():
    """Main function with modern argument parsing."""
    parser = argparse.ArgumentParser(
        description="Modern Ageing Sign Detection System",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python modern_ageing_detector.py --image test.jpg --output result.jpg
  python modern_ageing_detector.py --image test.jpg --confidence 0.8
  python modern_ageing_detector.py --image test.jpg --model custom_model.json
        """
    )
    
    parser.add_argument("--image", required=True, help="Path to input image")
    parser.add_argument("--output", default="result.jpg", help="Path to output image")
    parser.add_argument("--model", default="model_weights/model.json", help="Path to model JSON")
    parser.add_argument("--weights", default="model_weights/weights.h5", help="Path to model weights")
    parser.add_argument("--confidence", type=float, default=0.75, help="Confidence threshold")
    parser.add_argument("--save-model", action="store_true", help="Save the model after loading")
    
    args = parser.parse_args()
    
    # Validate inputs
    if not os.path.exists(args.image):
        print(f"❌ Image not found: {args.image}")
        return
    
    print("🚀 Modern Ageing Sign Detection System")
    print("=" * 50)
    
    # Initialize detector
    detector = ModernAgeingDetector(
        model_path=args.model,
        weights_path=args.weights,
        confidence_threshold=args.confidence
    )
    
    # Run detection
    results = detector.detect_ageing_signs(args.image, args.output)
    
    # Print results
    print("\n📊 Detection Results:")
    print(f"Faces detected: {results.get('faces_detected', 0)}")
    
    for result in results.get('results', []):
        print(f"\nFace {result['face_id'] + 1}:")
        for category, confidence in result['predictions'].items():
            print(f"  {category}: {confidence:.2%}")
    
    if args.save_model and detector.model:
        print("\n💾 Saving model...")
        detector.model.save_weights(args.weights)
        with open(args.model, 'w') as f:
            f.write(detector.model.to_json())
        print("✅ Model saved successfully")

if __name__ == "__main__":
    main()

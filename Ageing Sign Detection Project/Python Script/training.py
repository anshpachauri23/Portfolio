#!/usr/bin/env python3
"""
Modern Ageing Sign Training System
Advanced training pipeline with modern deep learning techniques.

Features:
- EfficientNetB3 architecture (better than B0)
- Advanced data augmentation
- Learning rate scheduling
- Early stopping and model checkpointing
- Mixed precision training
- Modern optimizers (AdamW)
"""

import os
import sys
import json
import argparse
import numpy as np
import pandas as pd
from pathlib import Path
from typing import Dict, List, Tuple, Optional
import warnings
warnings.filterwarnings('ignore')

# Modern imports
try:
    import tensorflow as tf
    from tensorflow import keras
    from tensorflow.keras.applications import EfficientNetB3
    from tensorflow.keras.preprocessing.image import ImageDataGenerator
    from tensorflow.keras.applications.efficientnet import preprocess_input
    from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau, ModelCheckpoint
    from tensorflow.keras.optimizers import AdamW
    from tensorflow.keras.mixed_precision import set_global_policy
    print("✅ TensorFlow 2.15+ loaded successfully")
except ImportError:
    print("❌ TensorFlow not found. Installing...")
    os.system("pip install tensorflow>=2.15.0")
    import tensorflow as tf
    from tensorflow import keras

try:
    from sklearn.model_selection import train_test_split
    from sklearn.preprocessing import LabelEncoder
    from sklearn.metrics import classification_report, confusion_matrix
    print("✅ scikit-learn loaded successfully")
except ImportError:
    print("❌ scikit-learn not found. Installing...")
    os.system("pip install scikit-learn>=1.3.0")
    from sklearn.model_selection import train_test_split

try:
    import cv2
    import matplotlib.pyplot as plt
    import seaborn as sns
    print("✅ OpenCV and visualization libraries loaded")
except ImportError:
    print("❌ OpenCV not found. Installing...")
    os.system("pip install opencv-python>=4.8.0 matplotlib>=3.7.0 seaborn>=0.12.0")

# Configure TensorFlow for optimal performance
tf.config.optimizer.set_jit(True)
tf.config.threading.set_inter_op_parallelism_threads(0)
tf.config.threading.set_intra_op_parallelism_threads(0)

# Enable mixed precision for faster training
set_global_policy('mixed_float16')

class ModernAgeingTrainer:
    """Modern training system for ageing sign detection."""
    
    def __init__(self, dataset_path: str, model_save_path: str = "model_weights"):
        """Initialize the modern trainer."""
        self.dataset_path = dataset_path
        self.model_save_path = model_save_path
        self.model = None
        self.history = None
        
        # Training parameters
        self.IMAGE_SIZE = (300, 300)  # EfficientNetB3 optimal size
        self.BATCH_SIZE = 32
        self.EPOCHS = 50
        self.LEARNING_RATE = 0.001
        
        # Create save directory
        os.makedirs(model_save_path, exist_ok=True)
        
        # Categories mapping
        self.categories = {
            'dark': 0,
            'notOld': 1, 
            'puffy': 2,
            'wrinkles': 3
        }
        
        self.num_classes = len(self.categories)
    
    def load_and_preprocess_data(self) -> Tuple[np.ndarray, np.ndarray]:
        """Load and preprocess the dataset with modern techniques."""
        print("🔄 Loading and preprocessing dataset...")
        
        images = []
        labels = []
        
        # Load images from all categories
        for category, label_id in self.categories.items():
            category_path = os.path.join(self.dataset_path, category)
            
            if not os.path.exists(category_path):
                print(f"⚠️ Category path not found: {category_path}")
                continue
            
            print(f"📁 Loading {category} images...")
            category_images = []
            category_labels = []
            
            for img_file in os.listdir(category_path):
                if img_file.lower().endswith(('.jpg', '.jpeg', '.png')):
                    img_path = os.path.join(category_path, img_file)
                    
                    try:
                        # Load and preprocess image
                        img = cv2.imread(img_path)
                        if img is not None:
                            # Resize to model input size
                            img_resized = cv2.resize(img, self.IMAGE_SIZE)
                            
                            # Convert BGR to RGB
                            img_rgb = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB)
                            
                            # Normalize for EfficientNet
                            img_normalized = preprocess_input(img_rgb.astype(np.float32))
                            
                            category_images.append(img_normalized)
                            category_labels.append(label_id)
                            
                    except Exception as e:
                        print(f"⚠️ Error loading {img_file}: {e}")
                        continue
            
            print(f"✅ Loaded {len(category_images)} {category} images")
            images.extend(category_images)
            labels.extend(category_labels)
        
        # Convert to numpy arrays
        X = np.array(images)
        y = np.array(labels)
        
        # One-hot encode labels
        y_categorical = keras.utils.to_categorical(y, self.num_classes)
        
        print(f"✅ Dataset loaded: {X.shape[0]} images, {X.shape[1:]} shape")
        return X, y_categorical
    
    def create_modern_model(self) -> keras.Model:
        """Create a modern EfficientNetB3-based model."""
        print("🔄 Creating modern model architecture...")
        
        # Use EfficientNetB3 for better accuracy
        base_model = EfficientNetB3(
            weights='imagenet',
            include_top=False,
            input_shape=(*self.IMAGE_SIZE, 3)
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
            keras.layers.Dense(self.num_classes, activation='softmax')
        ])
        
        # Modern optimizer with weight decay
        optimizer = AdamW(
            learning_rate=self.LEARNING_RATE,
            weight_decay=0.01
        )
        
        # Compile with mixed precision
        model.compile(
            optimizer=optimizer,
            loss='categorical_crossentropy',
            metrics=['accuracy', 'top_3_accuracy']
        )
        
        print("✅ Modern model created successfully")
        return model
    
    def create_data_generators(self, X: np.ndarray, y: np.ndarray) -> Tuple[ImageDataGenerator, ImageDataGenerator]:
        """Create advanced data generators with augmentation."""
        print("🔄 Creating data generators with augmentation...")
        
        # Split data
        X_train, X_val, y_train, y_val = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=np.argmax(y, axis=1)
        )
        
        # Advanced augmentation for training
        train_datagen = ImageDataGenerator(
            rotation_range=20,
            width_shift_range=0.2,
            height_shift_range=0.2,
            shear_range=0.2,
            zoom_range=0.2,
            horizontal_flip=True,
            brightness_range=[0.8, 1.2],
            channel_shift_range=0.1,
            fill_mode='nearest'
        )
        
        # Validation generator (no augmentation)
        val_datagen = ImageDataGenerator()
        
        # Create generators
        train_generator = train_datagen.flow(
            X_train, y_train,
            batch_size=self.BATCH_SIZE,
            shuffle=True
        )
        
        val_generator = val_datagen.flow(
            X_val, y_val,
            batch_size=self.BATCH_SIZE,
            shuffle=False
        )
        
        print(f"✅ Data generators created: {len(X_train)} train, {len(X_val)} validation")
        return train_generator, val_generator
    
    def setup_callbacks(self) -> List[keras.callbacks.Callback]:
        """Setup modern training callbacks."""
        callbacks = [
            # Early stopping
            EarlyStopping(
                monitor='val_accuracy',
                patience=10,
                restore_best_weights=True,
                verbose=1
            ),
            
            # Learning rate reduction
            ReduceLROnPlateau(
                monitor='val_loss',
                factor=0.5,
                patience=5,
                min_lr=1e-7,
                verbose=1
            ),
            
            # Model checkpointing
            ModelCheckpoint(
                filepath=os.path.join(self.model_save_path, 'best_model.h5'),
                monitor='val_accuracy',
                save_best_only=True,
                verbose=1
            )
        ]
        
        return callbacks
    
    def train_model(self) -> Dict:
        """Train the modern model."""
        print("🚀 Starting modern training pipeline...")
        
        # Load data
        X, y = self.load_and_preprocess_data()
        
        # Create model
        self.model = self.create_modern_model()
        
        # Create data generators
        train_gen, val_gen = self.create_data_generators(X, y)
        
        # Setup callbacks
        callbacks = self.setup_callbacks()
        
        # Train model
        print("🔄 Training model...")
        self.history = self.model.fit(
            train_gen,
            epochs=self.EPOCHS,
            validation_data=val_gen,
            callbacks=callbacks,
            verbose=1
        )
        
        # Save model
        self.save_model()
        
        # Evaluate model
        evaluation = self.evaluate_model(val_gen)
        
        return {
            'history': self.history.history,
            'evaluation': evaluation
        }
    
    def save_model(self):
        """Save the trained model."""
        print("💾 Saving model...")
        
        # Save model architecture
        model_json = self.model.to_json()
        with open(os.path.join(self.model_save_path, 'model.json'), 'w') as f:
            f.write(model_json)
        
        # Save weights
        self.model.save_weights(os.path.join(self.model_save_path, 'weights.h5'))
        
        # Save complete model
        self.model.save(os.path.join(self.model_save_path, 'complete_model.h5'))
        
        print("✅ Model saved successfully")
    
    def evaluate_model(self, val_generator) -> Dict:
        """Evaluate the trained model."""
        print("📊 Evaluating model...")
        
        # Get predictions
        val_generator.reset()
        predictions = self.model.predict(val_generator, verbose=1)
        predicted_classes = np.argmax(predictions, axis=1)
        
        # Get true labels
        true_classes = val_generator.classes
        
        # Calculate metrics
        accuracy = np.mean(predicted_classes == true_classes)
        
        print(f"✅ Validation Accuracy: {accuracy:.4f}")
        
        return {
            'accuracy': accuracy,
            'predictions': predictions,
            'true_labels': true_classes
        }
    
    def plot_training_history(self, save_path: str = None):
        """Plot training history."""
        if self.history is None:
            print("❌ No training history available")
            return
        
        fig, axes = plt.subplots(2, 2, figsize=(15, 10))
        
        # Plot accuracy
        axes[0, 0].plot(self.history.history['accuracy'], label='Training')
        axes[0, 0].plot(self.history.history['val_accuracy'], label='Validation')
        axes[0, 0].set_title('Model Accuracy')
        axes[0, 0].set_xlabel('Epoch')
        axes[0, 0].set_ylabel('Accuracy')
        axes[0, 0].legend()
        
        # Plot loss
        axes[0, 1].plot(self.history.history['loss'], label='Training')
        axes[0, 1].plot(self.history.history['val_loss'], label='Validation')
        axes[0, 1].set_title('Model Loss')
        axes[0, 1].set_xlabel('Epoch')
        axes[0, 1].set_ylabel('Loss')
        axes[0, 1].legend()
        
        # Plot learning rate
        if 'lr' in self.history.history:
            axes[1, 0].plot(self.history.history['lr'])
            axes[1, 0].set_title('Learning Rate')
            axes[1, 0].set_xlabel('Epoch')
            axes[1, 0].set_ylabel('Learning Rate')
        
        plt.tight_layout()
        
        if save_path:
            plt.savefig(save_path, dpi=300, bbox_inches='tight')
            print(f"✅ Training plots saved to: {save_path}")
        
        plt.show()

def main():
    """Main training function."""
    parser = argparse.ArgumentParser(
        description="Modern Ageing Sign Training System",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    
    parser.add_argument("--dataset", required=True, help="Path to dataset directory")
    parser.add_argument("--output", default="model_weights", help="Path to save model")
    parser.add_argument("--epochs", type=int, default=50, help="Number of training epochs")
    parser.add_argument("--batch-size", type=int, default=32, help="Batch size")
    parser.add_argument("--learning-rate", type=float, default=0.001, help="Learning rate")
    parser.add_argument("--plot", action="store_true", help="Plot training history")
    
    args = parser.parse_args()
    
    print("🚀 Modern Ageing Sign Training System")
    print("=" * 50)
    
    # Initialize trainer
    trainer = ModernAgeingTrainer(
        dataset_path=args.dataset,
        model_save_path=args.output
    )
    
    # Update parameters
    trainer.EPOCHS = args.epochs
    trainer.BATCH_SIZE = args.batch_size
    trainer.LEARNING_RATE = args.learning_rate
    
    # Train model
    results = trainer.train_model()
    
    # Plot results if requested
    if args.plot:
        trainer.plot_training_history(
            save_path=os.path.join(args.output, 'training_history.png')
        )
    
    print("✅ Training completed successfully!")

if __name__ == "__main__":
    main()

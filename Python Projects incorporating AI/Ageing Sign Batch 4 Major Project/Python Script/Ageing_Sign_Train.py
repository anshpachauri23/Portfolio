### Ageing_Sign_Train

from tensorflow.keras.preprocessing.image import ImageDataGenerator # for image augmentation
from tensorflow.keras.optimizers import Adam # optimizer
from tensorflow.keras.preprocessing.image import img_to_array # converting image to array
from tensorflow.keras.models import Sequential # forward flowing network
from tensorflow.keras.layers import Dense, MaxPooling2D, Flatten, Dropout, Conv2D # dense-fully connected nodes,dropout-# of nodes to block
from tensorflow.keras.applications import EfficientNetB0 #efficientNet model B0 (img size 244, 244, 3)
from sklearn.preprocessing import MultiLabelBinarizer # for multilabel classification
from sklearn.model_selection import train_test_split # splitting the data into training and testing
import matplotlib.pyplot as plt # for plotting training & accuracy graph 
from imutils import paths # to work on directories
import tensorflow as tf
import numpy as np
import argparse
import random
import cv2
import os


ap = argparse.ArgumentParser()
ap.add_argument("-f", "--folder", required=True, help="path where you want to save the model, weights and graph")
ap.add_argument("-d", "--dataset", required=True, help="path to dataset directory")
args = vars(ap.parse_args())

#Defining Constants
EPOCHS = 20 # no. of iterations the neural model trains
INIT_LR = 1e-3 # default value of Adam optimizer(0.001)
BS = 25 # batch_size
IMAGE_DIMS = (244, 244, 3) # Image dimensions for EfficientNetB0
#tf.compat.v1.disable_eager_execution() # synchronization(optional)

imagePaths = sorted(list(paths.list_images(args["dataset"])))# gathering all image paths
random.seed(42) # specifying a random no. to randomize the directories
random.shuffle(imagePaths)

data = []
labels = []

for imagePath in imagePaths: #for every directory we're traversing all the images
  image = cv2.imread(imagePath) # reading the image
  image = cv2.resize(image, (IMAGE_DIMS[1], IMAGE_DIMS[0])) # resizing the image to match efficientNetB0 conditions
  image = img_to_array(image) # converting the resized image to array
  data.append(image)

  l = label = imagePath.split(os.path.sep)[-2].split("_") # splitting the list of labels from the directory name
  labels.append(l)

data = np.array(data, dtype="float")/255.0 # preprocessing(normalization) the image to get values between 0 and 1 to ease computation burden
labels = np.array(labels, dtype=object) # converting the label to an array
print("[INFO] data matrix: {} images ({:.2f} MB)".format(len(imagePaths), data.nbytes / (1024 * 1000.0))) # gathering info about the whole data

print("[INFO] class labels: ")
mlb = MultiLabelBinarizer() # initializing the MultiLabelBinarizer class
labels = mlb.fit_transform(labels) # this class converts all the labels into distinct values

for (i, label) in enumerate(mlb.classes_): # printing the labels (total 4)
    print("{}. {}".format(i+1, label))

(trainX, testX, trainY, testY) = train_test_split(data, labels, test_size=0.2, random_state=42) # splitting data into training & testing


dataGen = ImageDataGenerator(
    rotation_range=25,
    width_shift_range=0.1,
    height_shift_range=0.1,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    fill_mode="nearest"
) # creating modified images, by rotating, shifting, zooming, shearing or flipping the image

dataGen.fit(trainX) # applying the augmentation the training images only

#Specifying the architecture of our neural network
conv_base = EfficientNetB0(weights="imagenet", include_top=False, input_shape=IMAGE_DIMS)#input layer
model = Sequential()
model.add(conv_base)
model.add(Dropout(rate=0.4))
model.add(Flatten())
model.add(Dense(len(mlb.classes_), activation="sigmoid"))# output layer sigmoid is more effective for multilabel classification
conv_base.trainable = False

opt = Adam(learning_rate=INIT_LR, decay=INIT_LR/EPOCHS) #optimizer

#compiling the model
model.compile(loss="binary_crossentropy", optimizer=opt, metrics=["accuracy"]) # binary_crossentropy is a loss function for multi label classification

#training the model
print("[INFO] training the network...")
H=model.fit(
    dataGen.flow(trainX, trainY, batch_size=BS),
    validation_data=(testX, testY),
    epochs=30
)

plt.style.use("ggplot")
plt.figure()
N = 30
plt.plot(np.arange(0, N), H.history["loss"], label="train_loss")
#plt.plot(np.arange(0, N), H.history["val_loss"], label="val_loss")
plt.plot(np.arange(0, N), H.history["accuracy"], label="train_acc")
plt.plot(np.arange(0, N), H.history["val_accuracy"], label="val_acc")
plt.title("Training Loss and Accuracy")
plt.xlabel("Epoch #")
plt.ylabel("Loss/Accuracy")
plt.legend(loc="upper left")

plt.savefig(args["folder"])

#saving the model and weights
model_json = model.to_json()
with open(args["folder"]+"model.json", "w") as file:
  file.write(model_json)
  file.close()

model.save_weights(args["folder"]+"weights.h5")
print("Model Saved Successfully!!")
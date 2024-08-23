## Ageing_Sign_Batch_4

A model to classify & localize different signs of ageing such as puffy eyes, wrinkles and dark spots etc. on the face.

The model preferred to work upon is EfficientNet which will detect signs of ageing like puffy eyes, wrinkles, dark spots on the image. It should overlay bounding boxes around detected ageing signs.

After unzipping the zipped folder you'll come across **dataset** folder which contains all the images on which our model has been trained. 

The **cascade** folder contains the haar cascades for face detection. 

The **shape-predictor** folder contains the dlib facial landmarks file.

The **Colab notebook** folder has the colab ipynb notebooks which will run on google colab (under a GPU runtime, ofcourse!)

The **Jupyter notebook** folder has the jupyter notebooks which will run on jupyter environment.

The **Python Script** folder has the python scripts which will run on PC with any OS with python  3.8 installed

The **requirements.txt** file is what you're supposed to run first(especially if you're working on the files in the **Python Script** folder cause google colab and jupyter come with most of the requirements pre-installed).

To download the requirements, run the following code in your terminal/shell:
pip install -r requirements.txt

Now getting to the FUN part!!.


There are 3 ways to run the detections on the images you want:
    1. By using the **Ageing_Sign_Detect.ipynb** file in the **Colab notebook** folder.
    2. By using the **Ageing_Sign_Detect.ipynb** file in the **Jupyter notebook** folder.
    3. By using the **Ageing_Sign_Detect.py** file in the **Python Script** folder.
    
    
*****************************************************************
## Colab Notebook


To work with the **Ageing_Sign_Detect.ipynb** file in the **Colab notebook** folder, follow the below steps:
1. Upload the whole unzipped folder on your google drive.

2. Double-click on the Ageing_Sign_Detect.ipynb file to open it in Google Colab.

3. Run the code in each segment by pressing shift+enter or simply click on the 'play' button next to the code.

4. In the second segment of the notebook the code will ask permission to access your google drive to be able to import necessary files from the unzipped folder you just uploaded.

5. Once you run that code, Google Colab will provide you with a link to that drive's access key. Copy it and paste it in the form provided.

6. Next segment of code will help you load the model.json file from the drive. **Note** :This is where you'll have to provide the path to the model.json file. See **Examples** sub-heading at the end of this section for more details.

7. Most of the code segments will run using shift+enter, without changing any part of the code.

8. The places where you have to type in the path of the specific file will be paired with a Comment specifying that you need to enter the path in that place.


#### Example 

**For path to model.json** the path will look like:
/content/gdrive/My Drive/Ageing Sign Batch 4 Major Project/model_weights/model.json

**For path to weights.h5** the path will look like:
/content/gdrive/My Drive/Ageing Sign Batch 4 Major Project/model_weights/weights.h5

**For path to cascade** the path will look like:
/content/gdrive/My Drive/Ageing Sign Batch 4 Major Project/cascade/haarcascade_frontalface_default.xml

**For path to shape-predictor** the path will look like:
/content/gdrive/My Drive/Ageing Sign Batch 4 Major Project/shape-predictor/shape_predictor_81_face_landmarks.dat


*****************************************************************
### Jypyter notebook


To work with the **Ageing_Sign_Detect.ipynb** file in the **Jupyter notebook** folder, follow the below steps:
1. Unzip the folder on your PC.

2. Open jupyter notebook via anconda-prompt or anaconda-navigator. Locate the directory where you've unzipped the folder.

3. Open the Ageing_Sign_Detect.ipynb file.

4. Run the code in each segment by pressing shift+enter or simply clicking on the 'run' button at the tool bar.

5. Next segment of code will help you load the model.json file from the drive. **Note** :This is where you'll have to provide the path to the model.json file. See **Examples** sub-heading at the end of this section for more details.

7. Most of the code segments will run using shift+enter(or 'run' tab in the tool bar), without changing any part of the code.

8. The places where you have to type in the path of the specific file will be paired with a Comment specifying that you need to enter the path in that place.


#### Example 

**For path to model.json** the path will look like:
../model_weights/model.json

**For path to weights.h5** the path will look like:
../model_weights/weights.h5

**For path to cascade** the path will look like:
../cascade/haarcascade_frontalface_default.xml

**For path to shape-predictor** the path will look like:
../shape-predictor/shape_predictor_81_face_landmarks.dat

**********************************************************************


### Python Script


To work with the **Ageing_Sign_Detect.py** file in the **Python Script** folder, follow the below steps:
1. Unzip the folder on your PC.

2. Make sure you've installed all the requirements.

3. Run the following command:
python Ageing_Sign_Detect.py --model ../model_weights/model.json --weight ../model_weights/weights.h5 --cascade ../cascade/haarcascade_frontalface_default.xml --shape-predictor ../shape-predictor/shape_predictor_81_face_landmarks.dat --image path_to_image 
4. Run the following command to see help text:
python Ageing_Sign_Detect.py --help


### For Developers


In each of the **Colab notebook**, **Python Script** and **Jupyter notebook** there is another file name **Ageing_Sign_Train**. This file is for developers who want to know how we built the model. They can run this file from any of the three(google colab, jupyter, pythonIDE) interfaces. The way to use them is very similar to the **Ageing_Sign_Detect** files shown above.Feel free to take a look at them:).




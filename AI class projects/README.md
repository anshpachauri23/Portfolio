# AI Class Projects Portfolio

A comprehensive collection of Artificial Intelligence coursework projects demonstrating fundamental AI algorithms, machine learning techniques, and practical implementations. These projects showcase expertise in search algorithms, machine learning, natural language processing, and neural network concepts.

## 🚀 Project Overview

This portfolio contains 4 major AI homework assignments, each focusing on different aspects of artificial intelligence and machine learning:

### 1. **Search Algorithms Implementation** (HW 1)
- **Technology**: Python, Search Algorithms, Graph Theory
- **Description**: Implementation of fundamental search algorithms using Pacman game framework
- **Key Features**:
  - **Depth-First Search (DFS)**: Graph traversal using stack-based exploration
  - **Breadth-First Search (BFS)**: Level-by-level exploration using queue data structure
  - **Uniform Cost Search (UCS)**: Optimal pathfinding with cost-based priority
  - **A* Search**: Heuristic-guided search with Manhattan distance optimization
  - **Graph Search Implementation**: Closed set management to avoid revisiting states
  - **Pacman Integration**: Real-time visualization of search algorithms in game environment
- **Skills Demonstrated**: Search Algorithms, Graph Theory, Data Structures, Heuristic Functions, Pathfinding

### 2. **Linear Regression & Machine Learning** (HW 2)
- **Technology**: Python, NumPy, Matplotlib, Machine Learning
- **Description**: Implementation of linear regression with polynomial feature transformation and overfitting analysis
- **Key Features**:
  - **Linear Regression Implementation**: Closed-form solution using normal equations
  - **Polynomial Feature Transformation**: Extending 1D features to polynomial representations
  - **Overfitting Analysis**: Training vs test error analysis with different polynomial degrees
  - **Data Visualization**: Matplotlib plots showing regression lines and data points
  - **Multiple Datasets**: Linear and quadratic data with different complexity levels
  - **Error Analysis**: MSE calculation for training and test sets
- **Skills Demonstrated**: Machine Learning, Linear Algebra, Statistical Analysis, Data Visualization, Overfitting Prevention

### 3. **Naive Bayes Classification** (HW 3)
- **Technology**: Python, Natural Language Processing, Text Classification
- **Description**: Sentiment analysis implementation using Naive Bayes classifier for Twitter data
- **Key Features**:
  - **Binary Bag of Words**: Text representation using binary word presence vectors
  - **Sentiment Classification**: Three-class classification (Positive, Negative, Neutral)
  - **Probability Estimation**: Prior and conditional probability calculations
  - **Log Probability**: Numerical stability using log probabilities
  - **Smoothing Techniques**: Laplace smoothing to handle zero probabilities
  - **Performance Optimization**: Improved accuracy through parameter tuning
- **Skills Demonstrated**: Natural Language Processing, Text Classification, Probability Theory, Sentiment Analysis, Feature Engineering

### 4. **Logistic Regression & Perceptron** (HW 4)
- **Technology**: Python, NumPy, Machine Learning, Brain-Computer Interface
- **Description**: Binary classification using logistic regression and perceptron algorithms on fMRI brain data
- **Key Features**:
  - **Logistic Regression**: Gradient descent implementation with sigmoid activation
  - **Perceptron Algorithm**: Linear classifier with iterative weight updates
  - **fMRI Data Processing**: Brain image classification (picture vs sentence viewing)
  - **Feature Engineering**: Data preprocessing and normalization
  - **Performance Comparison**: Accuracy analysis between different algorithms
  - **Neuroscience Application**: Real-world brain-computer interface data
- **Skills Demonstrated**: Neural Networks, Gradient Descent, Binary Classification, Neuroscience Data, Algorithm Comparison

## 🛠️ Technology Stack

### **Core Technologies**
- **Language**: Python 3.11
- **Libraries**: NumPy, Matplotlib, SciPy
- **Machine Learning**: Custom implementations (no scikit-learn)
- **Data Processing**: Array manipulation, statistical analysis
- **Visualization**: Matplotlib for data plotting and algorithm visualization

### **Specialized Technologies**
- **Search Algorithms**: Graph traversal, heuristic functions, priority queues
- **Machine Learning**: Linear regression, Naive Bayes, logistic regression, perceptron
- **Natural Language Processing**: Text preprocessing, bag of words, sentiment analysis
- **Neuroscience**: fMRI data processing, brain-computer interface applications
- **Mathematical Computing**: Linear algebra, probability theory, optimization

## 🏗️ Algorithm Implementations

### **Search Algorithms (HW 1)**
- **DFS**: Stack-based depth-first exploration with backtracking
- **BFS**: Queue-based breadth-first level exploration
- **UCS**: Priority queue with accumulated cost ordering
- **A***: Heuristic-guided search with f(n) = g(n) + h(n) evaluation

### **Machine Learning Algorithms (HW 2-4)**
- **Linear Regression**: Normal equation solution: w = (X^T X)^(-1) X^T y
- **Naive Bayes**: P(class|features) ∝ P(class) ∏ P(feature|class)
- **Logistic Regression**: Sigmoid activation with gradient descent
- **Perceptron**: Linear threshold function with weight updates

## 📈 Technical Achievements

### **Algorithm Implementation**
- **4 Search Algorithms**: Complete implementation from scratch
- **3 ML Algorithms**: Linear regression, Naive Bayes, logistic regression, perceptron
- **Custom Data Structures**: Priority queues, stacks, heaps for search algorithms
- **Mathematical Foundations**: Linear algebra, probability theory, optimization

### **Real-World Applications**
- **Game AI**: Pacman pathfinding with optimal search strategies
- **Sentiment Analysis**: Twitter data classification with 74%+ accuracy
- **Neuroscience**: Brain image classification using machine learning
- **Data Visualization**: Comprehensive plotting and analysis tools

### **Performance Optimization**
- **Numerical Stability**: Log probability calculations, gradient descent optimization
- **Overfitting Prevention**: Training vs test error analysis, regularization techniques
- **Algorithm Efficiency**: Optimal search strategies, convergence analysis
- **Memory Management**: Efficient data structures and array operations

## 📁 Project Organization

```
AI class projects/
├── HW_1_Programming/              # Search Algorithms
│   ├── py/                        # Python implementation files
│   │   ├── search.py              # Search algorithm implementations
│   │   ├── pacman.py              # Game framework
│   │   └── autograder.py          # Testing framework
│   ├── layouts/                   # Game layouts and mazes
│   ├── test_cases/                # Automated test cases
│   └── Readme.md                  # Detailed instructions
├── HW_2_Programming/              # Linear Regression
│   ├── LR.py                      # Linear regression implementation
│   ├── data/                      # Training and test datasets
│   ├── for_display/               # Visualization examples
│   └── Readme.md                  # Implementation guide
├── HW_3_Programming/              # Naive Bayes Classification
│   ├── NaiveBayes/                # Classification implementation
│   │   ├── NaiveBayes.py          # Basic implementation
│   │   ├── NaiveBayes_improved.py # Enhanced version
│   │   └── data-sentiment/        # Twitter sentiment data
│   └── Readme.md                  # Classification guide
├── HW_4_Programming/              # Logistic Regression & Perceptron
│   ├── Linear_Classifiers.py      # Binary classification algorithms
│   ├── Starplus.npz              # fMRI brain data
│   └── readme.md                  # Neural network guide
└── README.md                      # This comprehensive overview
```

## 🚀 Getting Started

### **Prerequisites**
- Python 3.11+
- NumPy for numerical computing
- Matplotlib for data visualization
- Basic understanding of machine learning concepts

### **Running Projects**
1. **Search Algorithms**: `python3 py/pacman.py -l mediumMaze -p SearchAgent -a fn=dfs`
2. **Linear Regression**: `python3 LR.py --data linear --polynomial 1 --display --save`
3. **Naive Bayes**: `python3 NaiveBayes.py`
4. **Logistic Regression**: `python3 Linear_Classifiers.py --data starplus --algorithm logistic`

### **Project Categories**
- **Search & Pathfinding**: HW 1 - Graph search algorithms
- **Supervised Learning**: HW 2, 4 - Regression and classification
- **Natural Language Processing**: HW 3 - Text classification and sentiment analysis
- **Neural Networks**: HW 4 - Perceptron and logistic regression

## 📄 Documentation

- **Code Documentation**: Comprehensive comments and docstrings
- **Algorithm Explanations**: Mathematical foundations and implementation details
- **Performance Analysis**: Accuracy metrics and error analysis
- **Visualization**: Data plots and algorithm demonstrations

## 🎯 Learning Outcomes

### **Technical Skills**
- **Search Algorithms**: Graph traversal, heuristic functions, optimal pathfinding
- **Machine Learning**: Linear regression, classification, feature engineering
- **Natural Language Processing**: Text preprocessing, sentiment analysis
- **Neural Networks**: Perceptron, logistic regression, gradient descent
- **Data Analysis**: Statistical analysis, visualization, performance evaluation

### **Mathematical Foundations**
- **Linear Algebra**: Matrix operations, eigenvalue decomposition
- **Probability Theory**: Bayesian inference, conditional probabilities
- **Optimization**: Gradient descent, convergence analysis
- **Statistics**: Error analysis, overfitting prevention

### **Practical Applications**
- **Game AI**: Pathfinding and decision making in games
- **Text Analysis**: Sentiment classification and natural language understanding
- **Neuroscience**: Brain-computer interface and fMRI data analysis
- **Data Science**: Statistical modeling and predictive analytics

## 🔗 Related Skills

- **Artificial Intelligence**: Search algorithms, machine learning, neural networks
- **Data Science**: Statistical analysis, data visualization, predictive modeling
- **Natural Language Processing**: Text classification, sentiment analysis, feature engineering
- **Neuroscience**: Brain-computer interface, fMRI data analysis
- **Mathematical Computing**: Linear algebra, probability theory, optimization
- **Software Engineering**: Algorithm implementation, testing, documentation

---

**This portfolio demonstrates comprehensive AI and machine learning expertise, covering fundamental algorithms, practical implementations, and real-world applications suitable for AI research, data science, and machine learning engineering positions.**

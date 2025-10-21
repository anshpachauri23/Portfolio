# Natural Number Calculator

A comprehensive Java application implementing a Reverse Polish Notation (RPN) calculator with a complete Model-View-Controller (MVC) architecture. This project demonstrates advanced software engineering principles, GUI development, and mathematical computation handling.

## 🎯 Project Overview

The Natural Number Calculator is a sophisticated desktop application that performs arithmetic operations on natural numbers using RPN (Reverse Polish Notation) methodology. It features a clean graphical user interface with comprehensive error handling and robust mathematical operations.

## ✨ Key Features

- **Reverse Polish Notation (RPN)**: Advanced mathematical computation methodology
- **MVC Architecture**: Clean separation of concerns with Model, View, and Controller components
- **Graphical User Interface**: Intuitive desktop application with Swing components
- **Mathematical Operations**: Addition, subtraction, multiplication, division with remainder handling
- **Error Handling**: Comprehensive input validation and error management
- **Memory Management**: Operand swapping and clearing functionality
- **Documentation**: Extensive Javadoc documentation and code comments

## 🛠️ Technology Stack

- **Language**: Java 8+
- **GUI Framework**: Java Swing
- **Architecture**: Model-View-Controller (MVC)
- **Build Tool**: Eclipse IDE integration
- **Documentation**: Javadoc
- **Testing**: JUnit test framework integration

## 🏗️ Architecture

### MVC Pattern Implementation

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      Model      │    │      View       │    │   Controller    │
│                 │    │                 │    │                 │
│ • NNCalcModel   │◄──►│ • NNCalcView    │◄──►│ • NNCalcController│
│ • Data Storage  │    │ • GUI Components│    │ • Event Handling│
│ • Calculations  │    │ • User Interface│    │ • Business Logic│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Core Components

- **NaturalNumberCalculator**: Main application entry point
- **NNCalcModel**: Data model and mathematical operations
- **NNCalcView**: User interface and display management
- **NNCalcController**: Event handling and business logic coordination

## 📊 Mathematical Operations

### Supported Operations
- **Addition**: `top + bottom`
- **Subtraction**: `top - bottom`
- **Multiplication**: `top × bottom`
- **Division**: `top ÷ bottom` (returns quotient and remainder)

### RPN Logic
- **Direct Entry**: Numbers entered to bottom operand
- **Clear**: Sets bottom operand to 0
- **Swap**: Exchanges top and bottom operand values
- **Enter**: Copies bottom operand to top operand
- **Operations**: Replace bottom with result, top with 0 (except division)

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher
- Eclipse IDE (recommended)
- Basic understanding of RPN notation

### Installation
1. Clone the repository
2. Import project into Eclipse IDE
3. Ensure Java 8+ is configured
4. Build the project

### Running the Application
```bash
# Compile the project
javac -cp . src/*.java

# Run the application
java -cp . src.NaturalNumberCalculator
```

## 📁 Project Structure

```
NaturalNumberCalculator/
├── src/
│   ├── NaturalNumberCalculator.java    # Main application
│   ├── NNCalcModel.java               # Data model
│   ├── NNCalcView.java                # User interface
│   ├── NNCalcController.java          # Event handling
│   └── NNCalcModel1.java              # Enhanced model
├── bin/                               # Compiled classes
├── doc/                               # Javadoc documentation
├── .project                           # Eclipse project file
├── .classpath                         # Classpath configuration
└── README.md                          # This file
```

## 🎮 Usage

### Basic Operations
1. **Enter Numbers**: Type natural numbers directly
2. **Clear**: Reset calculator to initial state
3. **Swap**: Exchange top and bottom operands
4. **Enter**: Move bottom operand to top
5. **Operations**: Perform mathematical calculations

### Example Calculation
```
Initial: Top=0, Bottom=0
Enter 5: Top=0, Bottom=5
Enter: Top=5, Bottom=5
Enter 3: Top=5, Bottom=3
Add: Top=0, Bottom=8 (result)
```

## 🔧 Development Features

### Code Quality
- **Comprehensive Documentation**: Extensive Javadoc comments
- **Clean Architecture**: Proper MVC separation
- **Error Handling**: Robust input validation
- **Memory Management**: Efficient operand handling

### Design Patterns
- **MVC Pattern**: Clear separation of concerns
- **Observer Pattern**: View updates based on model changes
- **Command Pattern**: Operation encapsulation

## 📈 Technical Achievements

- **MVC Architecture**: Demonstrates advanced software design principles
- **GUI Development**: Professional desktop application interface
- **Mathematical Logic**: Complex RPN calculation implementation
- **Error Handling**: Comprehensive input validation and error management
- **Documentation**: Extensive code documentation and user guides

## 🎯 Learning Outcomes

### Software Engineering Skills
- **Architecture Design**: MVC pattern implementation
- **GUI Development**: Java Swing framework usage
- **Event Handling**: User interaction management
- **Code Organization**: Modular and maintainable code structure

### Mathematical Concepts
- **RPN Logic**: Reverse Polish Notation understanding
- **Natural Numbers**: Mathematical computation handling
- **Algorithm Design**: Efficient calculation methods

## 🔗 Related Technologies

- **Java Swing**: GUI framework
- **MVC Pattern**: Software architecture
- **RPN**: Mathematical notation system
- **Eclipse IDE**: Development environment
- **Javadoc**: Documentation generation

## 📄 Documentation

- **User Guide**: Comprehensive usage instructions
- **API Documentation**: Javadoc-generated documentation
- **Architecture Guide**: MVC pattern explanation
- **Code Comments**: Inline documentation

## 🚀 Future Enhancements

- **Scientific Functions**: Advanced mathematical operations
- **History Feature**: Calculation history tracking
- **Custom Themes**: UI customization options
- **Keyboard Shortcuts**: Enhanced user experience
- **Export Functionality**: Result saving capabilities

---

**This project demonstrates advanced Java programming skills, software architecture design, and mathematical computation handling suitable for software engineering positions.**

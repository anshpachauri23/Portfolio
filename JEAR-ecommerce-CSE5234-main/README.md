# JEAR E-commerce Website

A modern, full-stack e-commerce platform built with React.js and AWS serverless architecture, featuring a complete shopping experience with real-time inventory management and order processing.

## 🚀 Features

### Frontend
- **Modern React UI** with Bootstrap 5 styling
- **Responsive Design** that works on all devices
- **Product Catalog** with 5 premium sweatshirt designs
- **Shopping Cart** with real-time updates and persistence
- **Advanced Checkout Form** with comprehensive validation
- **Order Confirmation** with professional receipt display
- **Dynamic Navigation** with cart counter
- **Smooth Animations** and hover effects

### Backend (AWS Serverless)
- **AWS Lambda Functions** - 6 serverless functions for all backend operations
- **API Gateway** - RESTful API endpoints with CORS support
- **AWS DynamoDB** - NoSQL database for orders, payments, inventory, contacts, and shipping
- **AWS EventBridge** - Event-driven architecture for shipping processing
- **Real-time Inventory Management** - Automatic stock tracking and updates
- **Sequential Order Numbers** - Auto-generated order IDs (AA00000001, AA00000002, etc.)
- **Synchronous Payment Processing** - Payment processed during order submission
- **PCI Compliance** - Payment data stored securely (no full card numbers or CVV)
- **Server-side Validation** - Comprehensive validation for orders and forms
- **Credit Card Validation** - Luhn algorithm validation
- **Country-State Logic** - Smart validation based on selected country
- **Health Check Endpoints** - Service monitoring and status checks

### Validation Features
- **Client-side & Server-side** validation
- **Email format** validation
- **Phone number** validation
- **Country-specific** state/province validation
- **Credit card formatting** (without Luhn algorithm)
- **ZIP code validation** for US addresses
- **Mandatory field** enforcement

## 🛠️ Tech Stack

### Frontend
- **React.js** - UI framework
- **React Router** - Client-side routing
- **React Context API** - State management
- **Bootstrap 5** - CSS framework
- **Bootstrap Icons** - Icon library

### Backend (AWS Serverless)
- **AWS Lambda** - Serverless compute functions
- **API Gateway** - RESTful API management and routing
- **AWS SDK v3** - Modern AWS SDK for cloud services
- **DynamoDB** - NoSQL database (Orders, Inventory, Contacts, Shipping)
- **EventBridge** - Event-driven architecture for async processing
- **Node.js 22.x** - Lambda runtime environment

## 📁 Project Structure

```
JEAR-ecommerce-CSE5234/
├── jear/                          # Frontend (React Application)
│   ├── public/
│   │   ├── ash.jpeg              # Product images
│   │   ├── earth.png
│   │   ├── forest.png
│   │   ├── ocean.png
│   │   ├── midnight.png
│   │   ├── ansh.jpeg             # Additional product images
│   │   ├── emily.jpg
│   │   ├── reuben.JPEG
│   │   ├── jearBanner.png        # Banner image
│   │   └── index.html
│   ├── src/
│   │   ├── components/           # React components
│   │   │   ├── Home.js          # Homepage with product grid
│   │   │   ├── ProductGrid.js   # Product catalog display
│   │   │   ├── Cart.js          # Shopping cart page
│   │   │   ├── Navbar.js        # Navigation bar with cart counter
│   │   │   ├── Footer.js        # Footer component
│   │   │   ├── AboutUs.js       # About Us page
│   │   │   └── purchase*.js     # Individual product pages (1-5)
│   │   ├── pages/               # Main application pages
│   │   │   ├── CheckoutPage.js  # Checkout form with validation
│   │   │   ├── ConfirmationPage.js # Order confirmation receipt
│   │   │   └── ContactPage.js   # Contact form page
│   │   ├── context/             # State management
│   │   │   └── CartContext.js   # Cart state provider
│   │   ├── config/              # API configuration
│   │   │   └── api.js           # AWS API Gateway endpoints
│   │   ├── styles/              # CSS stylesheets
│   │   │   ├── Home.css         # Homepage styles
│   │   │   ├── CheckoutPage.css # Checkout form styles
│   │   │   ├── ConfirmationPage.css # Confirmation page styles
│   │   │   ├── ContactPage.css  # Contact page styles
│   │   │   ├── AboutUs.css      # About Us page styles
│   │   │   └── purchase.css     # Product page styles
│   │   └── App.js               # Main app component with routing
│   └── package.json
├── aws-lambda-functions/         # AWS Serverless Backend (Reference Code)
│   │                              # These functions are deployed on AWS Lambda
│   │                              # and connected via API Gateway
│   ├── health-check.js          # Health check endpoint
│   ├── get-inventory.js          # Retrieve product inventory
│   ├── update-inventory.js      # Update stock levels
│   ├── order-processing.js      # Process orders with validation
│   ├── contact-form.js          # Handle contact form submissions
│   └── shipping-service.js      # EventBridge handler for shipping
└── order-backend/               # Legacy Backend (Reference Only)
    ├── server.js                # Express server (not in use)
    ├── validation.js            # Validation utilities
    └── package.json
```

## 🚀 Getting Started

### Prerequisites
- **Node.js** (version 14 or higher)
- **npm** (comes with Node.js)
- **AWS Account** with the following services configured:
  - Lambda Functions
  - API Gateway
  - DynamoDB
  - EventBridge (for shipping service)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/anshpachauri23/JEAR-ecommerce-CSE5234.git
   cd JEAR-ecommerce-CSE5234
   ```

2. **Frontend Setup**
   ```bash
   # Navigate to frontend directory
   cd jear
   
   # Install dependencies
   npm install
   
   # Start the React development server
   npm start
   ```

3. **AWS Backend Setup** (Already Deployed)
   - The backend is hosted on AWS using Lambda functions and API Gateway
   - API Base URL: `https://udnv1njlv1.execute-api.us-east-2.amazonaws.com/dev`
   - All Lambda functions are in the `aws-lambda-functions/` folder for reference
   - Functions are deployed and connected via API Gateway

### Access the Application
- **Frontend:** http://localhost:3000
- **Backend API:** https://udnv1njlv1.execute-api.us-east-2.amazonaws.com/dev

## 🎯 User Journey

1. **Homepage** - Browse featured sweatshirts with real-time inventory
2. **Product Pages** - View individual products with size selection and availability
3. **Add to Cart** - Add items with quantity selection (validates stock)
4. **Cart Management** - View, update, or remove items
5. **Checkout** - Fill out comprehensive order form with validation
6. **Payment Processing** - Credit card validated and processed synchronously
   - Payment token generated (PCI compliant)
   - Payment stored in PaymentTable
   - Order created with payment reference
7. **Order Confirmation** - Receive order number (AA00000001 format) and receipt
8. **Shipping** - Shipping event triggered asynchronously via EventBridge
9. **Contact** - Submit inquiries via contact form
10. **About Us** - Learn about the company

## 🛍️ Product Catalog

The website features 5 premium sweatshirt designs:
- **Ash** - Classic gray design
- **Earth** - Natural earth tones
- **Forest** - Deep green forest theme
- **Ocean** - Cool blue ocean vibes
- **Midnight** - Dark night theme

Each product comes in sizes: XS, S, M, L, XL

## 💳 Checkout Process

### Form Fields
- **Contact Information**: Email, Phone, First Name, Last Name
- **Shipping Address**: Address, Apartment, City, State, ZIP Code, Country
- **Payment Information**: Card Number, Cardholder Name, Expiry Date, CVV

### Validation Features
- **Email Format**: Valid email address required (client & server-side)
- **Phone Number**: 10-digit phone number validation
- **Country-State Logic**: State/province validation based on selected country
- **US States**: 50 US states with 2-letter codes
- **Canadian Provinces**: 13 provinces/territories
- **Mexican States**: 32 Mexican states
- **Australian States**: 8 states/territories
- **ZIP Code Validation**: US ZIP code format (12345 or 12345-6789)
- **Credit Card Validation**: Luhn algorithm validation (server-side)
- **Card Formatting**: Automatic spacing and formatting (client-side)
- **Expiry Date**: MM/YY format validation with future date check
- **CVV Validation**: 3-4 digit security code validation

## 🗄️ Database Schema

### Orders Table (DynamoDB)
**Table Name:** `orders`  
**Primary Key:** `orderID` (String)  
**Note:** Payment information is stored separately in PaymentTable for PCI compliance

```json
{
  "orderID": "unique_timestamp_id",
  "orderNumber": "AA00000001",
  "items": [
    {
      "productId": "ASH001",
      "size": "M",
      "quantity": 2
    }
  ],
  "customerInfo": {
    "email": "customer@example.com",
    "phone": "1234567890",
    "firstName": "John",
    "lastName": "Doe",
    "address": "123 Main St",
    "city": "Anytown",
    "state": "CA",
    "zipCode": "12345",
    "country": "United States"
  },
  "total": 59.98,
  "paymentToken": "uuid-v4-payment-token",
  "paymentStatus": "approved",
  "orderDate": "2025-10-12T22:52:59.374Z",
  "status": "confirmed",
  "createdAt": "2025-10-12T22:52:59.374Z"
}
```

### Payment Table (DynamoDB)
**Table Name:** `PaymentTable`  
**Primary Key:** `paymentToken` (String)  
**Purpose:** Stores payment information separately for PCI compliance  
**Security:** Full card numbers and CVV are NEVER stored

```json
{
  "paymentToken": "uuid-v4-payment-token",
  "orderId": "unique_timestamp_id",
  "orderNumber": "AA00000001",
  "cardholderName": "John Doe",
  "last4Digits": "9012",
  "maskedCard": "****-****-****-9012",
  "expiryDate": "12/25",
  "amount": 59.98,
  "status": "approved",
  "processedAt": "2025-10-12T22:52:59.374Z"
}
```

**PCI Compliance Notes:**
- Full `cardNumber` and `cvv` are **never stored** in any table
- Only last 4 digits and masked card number are stored
- Payment token links order to payment information
- Sensitive payment data is removed from order before storage

### Inventory Table (DynamoDB)
**Table Name:** `inventory`  
**Primary Key:** `productId` (String) + `size` (String)  
**Purpose:** Tracks inventory levels for each product size combination

```json
{
  "productId": "ASH001",
  "size": "M",
  "name": "Ash",
  "description": "Premium Quality Hoodie",
  "price": 29.99,
  "category": "Hoodies",
  "image": "/ash.jpeg",
  "quantity": 15,
  "createdAt": "2025-10-23T06:30:00.000Z",
  "lastUpdated": "2025-10-23T06:30:00.000Z"
}
```

### Contacts Table (DynamoDB)
**Table Name:** `contacts`  
**Primary Key:** `contactID` (String)  
**Purpose:** Stores contact form submissions

```json
{
  "contactID": "uuid-v4",
  "name": "John Doe",
  "email": "john@example.com",
  "subject": "Question about products",
  "message": "I have a question...",
  "createdAt": "2025-10-12T22:52:59.374Z"
}
```

### Shipping Table (DynamoDB)
**Table Name:** `ShippingTable`  
**Primary Key:** `shippingID` (String)  
**Purpose:** Stores shipping information triggered by EventBridge events

```json
{
  "shippingID": "uuid-v4",
  "businessId": "STORE-REGISTRATION-1234",
  "shipmentAddress": {
    "address": "123 Main St",
    "city": "Anytown",
    "state": "CA",
    "zipCode": "12345",
    "country": "United States"
  },
  "numPackets": 2,
  "weightPerPacket": 1.0,
  "status": "initiated",
  "createdAt": "2025-10-12T22:52:59.374Z"
}
```

## 🔧 API Endpoints

### AWS Serverless Endpoints (Production)
**Base URL:** `https://udnv1njlv1.execute-api.us-east-2.amazonaws.com/dev`

#### Health Check
- **GET** `/api/health` - Service health check and status
  - Returns: Service status, timestamp, version, runtime info

#### Inventory Management
- **GET** `/api/inventory` - Get all available products with stock levels
  - Returns: Array of products with size availability
- **POST** `/api/inventory/update` - Update inventory levels (admin)
  - Body: `{ items: [{ productId, size, quantity }] }`

#### Order Processing
- **POST** `/api/orders` - Submit new order with inventory management
  - Body: `{ items, customerInfo, total }`
  - Features:
    - Automatic inventory deduction
    - Order number generation (AA00000001 format)
    - Credit card validation (Luhn algorithm)
    - Comprehensive form validation
    - EventBridge event for shipping service

#### Contact Form
- **POST** `/api/contact` - Submit contact form
  - Body: `{ name, email, subject, message }`
  - Validates email format and required fields

### Lambda Functions

1. **health-check.js** - Health monitoring endpoint
   - Returns service status and runtime information
   - Handles CORS preflight requests

2. **get-inventory.js** - Retrieve product inventory
   - Scans DynamoDB inventory table
   - Groups products by ID with size availability
   - Filters out out-of-stock items

3. **update-inventory.js** - Update stock levels
   - Processes order items and deducts inventory
   - Validates stock availability
   - Atomic updates per product size

4. **order-processing.js** - Process orders with payment processing
   - Validates order data and customer information
   - **Synchronous payment processing** (Lab 9 requirement)
   - Credit card validation using Luhn algorithm
   - Expiry date validation
   - Generates unique payment token (PCI compliant)
   - Stores payment info in PaymentTable (no full card numbers stored)
   - Generates sequential order numbers (AA00000001 format)
   - Saves orders to DynamoDB (without sensitive payment data)
   - Publishes EventBridge events for shipping
   - Updates inventory automatically after payment approval

5. **contact-form.js** - Handle contact submissions
   - Validates contact form data
   - Saves submissions to DynamoDB contacts table
   - Email format validation

6. **shipping-service.js** - EventBridge handler for shipping
   - Triggered by EventBridge events from order processing
   - Creates shipping records in ShippingTable
   - Processes shipping information asynchronously

### Request/Response Format

#### Get Inventory
```javascript
// GET /api/inventory
// Response
{
  "success": true,
  "products": [
    {
      "productId": "ASH001",
      "name": "Ash",
      "price": 29.99,
      "sizes": {
        "S": { "quantity": 10, "available": true },
        "M": { "quantity": 15, "available": true }
      }
    }
  ]
}
```

#### Submit Order
```javascript
// POST /api/orders
{
  "items": [
    { "productId": "ASH001", "size": "M", "quantity": 2 }
  ],
  "customerInfo": {
    "email": "customer@example.com",
    "phone": "1234567890",
    "firstName": "John",
    "lastName": "Doe",
    "address": "123 Main St",
    "city": "Anytown",
    "state": "CA",
    "zipCode": "12345",
    "country": "United States",
    "cardNumber": "4532123456789012",
    "cardholderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123"
  },
  "total": 59.98
}

// Response
{
  "message": "Order saved successfully!",
  "orderID": "1760309579240",
  "orderNumber": "AA00000014",
  "paymentToken": "uuid-v4-payment-token",
  "paymentStatus": "approved",
  "status": "success",
  "inventoryUpdates": [...],
  "timestamp": "2025-10-12T22:52:59.374Z"
}
```

**Payment Processing:**
- Payment is processed **synchronously** during order submission
- Credit card validated using Luhn algorithm
- Payment token generated and stored in PaymentTable
- Full card number and CVV are **never stored** (PCI compliant)
- Only last 4 digits and masked card stored for reference

## 🎨 UI/UX Features

### Design Elements
- **Gradient Backgrounds** - Modern gradient designs
- **Card-based Layout** - Clean product cards with hover effects
- **Responsive Grid** - Mobile-first responsive design
- **Smooth Animations** - Hover effects and transitions
- **Professional Typography** - Clean, readable fonts
- **Color Scheme** - Dark navigation with light content

### Interactive Elements
- **Cart Counter** - Real-time cart item count
- **Product Hover** - Image zoom and card lift effects
- **Form Validation** - Real-time error messages
- **Success Animations** - Order confirmation celebrations

## 🧪 Testing

### Manual Testing Checklist
- [ ] Homepage loads with product grid
- [ ] Product pages display correctly
- [ ] Add items to cart functionality
- [ ] Cart counter updates in navbar
- [ ] Cart page shows items correctly
- [ ] Checkout form validation works
- [ ] Payment processing (credit card validation)
- [ ] Order submission to database
- [ ] Payment information stored in PaymentTable
- [ ] Order information stored without sensitive payment data
- [ ] Confirmation page displays order details
- [ ] Inventory updates after order
- [ ] Shipping event published to EventBridge
- [ ] Responsive design on mobile/tablet

### Test Scenarios
1. **Complete Purchase Flow** - Add items → Cart → Checkout → Payment → Confirmation
2. **Payment Processing** - Test with valid/invalid credit cards (Luhn algorithm)
3. **PCI Compliance** - Verify no full card numbers stored in Orders table
4. **Form Validation** - Test with invalid data
5. **Country-State Logic** - Test different country selections
6. **Cart Management** - Add, update, remove items
7. **Inventory Management** - Test with insufficient stock
8. **Mobile Responsiveness** - Test on different screen sizes

## 🚨 Troubleshooting

### Common Issues

**AWS Backend Issues:**
- **"API Gateway timeout"** → Check Lambda function logs in CloudWatch
- **"DynamoDB connection failed"** → Verify Lambda IAM permissions
- **"CORS errors"** → Ensure API Gateway CORS is configured
- **"Lambda function error"** → Check CloudWatch logs for detailed errors
- **"EventBridge not triggering"** → Verify EventBridge rule and permissions

**Frontend Issues:**
- **"Port 3000 already in use"** → Kill React processes or use different port
- **"Module not found"** → Run `npm install` in jear directory
- **"Cart not updating"** → Check CartContext implementation
- **"API calls failing"** → Verify API Gateway endpoint URL in `config/api.js`
- **"CORS errors"** → Backend should handle CORS, check API Gateway settings

**General Issues:**
- **"Backend not responding"** → Check API Gateway endpoint status
- **"Images not loading"** → Check if image files exist in public folder
- **"Styling broken"** → Verify Bootstrap is imported in App.js
- **"Inventory not updating"** → Check DynamoDB table permissions and Lambda logs

### Debug Commands
```bash
# Kill processes on specific ports
lsof -ti:3000 | xargs kill -9  # React
lsof -ti:4000 | xargs kill -9  # Backend

# Clear npm cache
npm cache clean --force

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

## 📝 AWS Configuration

### Lambda Function Environment Variables
Lambda functions use the following environment variables (configured in AWS Lambda console):
- `AWS_REGION` - AWS region (default: `us-east-2`)
- `NODE_ENV` - Environment (default: `production`)

### DynamoDB Tables Required
- `orders` - Stores order information (without payment details)
- `PaymentTable` - Stores payment information (PCI compliant)
- `inventory` - Stores product inventory
- `contacts` - Stores contact form submissions
- `ShippingTable` - Stores shipping information

### IAM Permissions Required
Lambda functions need the following IAM permissions:
- `dynamodb:PutItem` - Write to DynamoDB tables
- `dynamodb:GetItem` - Read from DynamoDB tables
- `dynamodb:UpdateItem` - Update DynamoDB items
- `dynamodb:Scan` - Scan DynamoDB tables
- `events:PutEvents` - Publish EventBridge events

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🏗️ Architecture

### System Architecture
```
Frontend (React)
    ↓
API Gateway (REST API)
    ↓
Lambda Functions (Serverless)
    ├── Health Check
    ├── Get Inventory
    ├── Update Inventory
    ├── Order Processing (Synchronous Payment)
    │   ├── Validate Order & Payment
    │   ├── Process Payment → PaymentTable
    │   ├── Save Order → Orders Table
    │   ├── Update Inventory
    │   └── Publish EventBridge Event
    ├── Contact Form
    └── Shipping Service (EventBridge Handler)
    ↓
DynamoDB (NoSQL Database)
    ├── Orders Table (no payment details)
    ├── PaymentTable (PCI compliant)
    ├── Inventory Table
    ├── Contacts Table
    └── Shipping Table
    ↓
EventBridge
    └── Shipping Events → Shipping Service Lambda
```

### Order Processing Workflow
1. **Order Submission**: User submits order with payment information
2. **Validation**: 
   - Order data validation (email, phone, address, etc.)
   - Inventory availability check
   - Credit card validation (Luhn algorithm)
   - Expiry date validation
3. **Synchronous Payment Processing**:
   - Generate unique payment token
   - Store payment info in PaymentTable (last 4 digits only, no CVV)
   - Mark payment as "approved"
4. **Order Creation**:
   - Remove sensitive payment data from order
   - Save order to Orders Table with paymentToken reference
   - Update inventory (deduct quantities)
5. **Shipping Event**:
   - Publish EventBridge event with shipping details
   - Shipping Service Lambda processes event asynchronously
   - Create shipping record in ShippingTable

### Event Flow
1. **Order Processing**: User submits order → Validate → Process payment → Save order → Update inventory → Publish shipping event
2. **Shipping Service**: EventBridge event → Shipping Lambda → Creates shipping record in DynamoDB
3. **Inventory Updates**: Order processing automatically deducts inventory after payment approval

## 👥 Team

- **Frontend Development** - React.js implementation
- **Backend Development** - AWS Serverless (Lambda + API Gateway)
- **Database Integration** - AWS DynamoDB
- **Event Processing** - AWS EventBridge
- **UI/UX Design** - Bootstrap 5 styling

## 🎉 Success Metrics

- ✅ **Full E-commerce Flow** - Complete user journey from browsing to order confirmation
- ✅ **AWS Serverless Architecture** - Lambda functions and API Gateway
- ✅ **Synchronous Payment Processing** - Payment processed during order submission (Lab 9)
- ✅ **PCI Compliance** - Secure payment handling (no full card numbers or CVV stored)
- ✅ **Real-time Inventory Management** - Stock tracking and automatic updates
- ✅ **Database Integration** - Orders, payments, inventory, contacts, and shipping saved to DynamoDB
- ✅ **Event-Driven Shipping** - Asynchronous shipping processing via EventBridge
- ✅ **Form Validation** - Client and server-side validation
- ✅ **Credit Card Validation** - Luhn algorithm implementation
- ✅ **Responsive Design** - Works on all devices
- ✅ **Professional UI** - Modern, clean design
- ✅ **Error Handling** - Comprehensive error management
- ✅ **Scalable Backend** - Serverless architecture for production

---

**Built for CSE 5234 - Modern Web Development**
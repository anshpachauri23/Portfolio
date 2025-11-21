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
- **AWS Lambda Functions** for serverless processing
- **API Gateway** for RESTful endpoints
- **AWS DynamoDB** for order and inventory storage
- **Real-time Inventory Management** with stock tracking
- **Sequential Order Numbers** (AA00000001, AA00000002, etc.)
- **Server-side Validation** for all form fields
- **Country-State Logic** with smart validation
- **Health Check Endpoints** for monitoring

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
- **AWS Lambda** - Serverless compute
- **API Gateway** - RESTful API management
- **AWS SDK v3** - Cloud services integration
- **DynamoDB** - NoSQL database for orders and inventory
- **Node.js 22.x** - Runtime environment

## 📁 Project Structure

```
JEAR-ecommerce-CSE5234/
├── jear/                          # Frontend (React)
│   ├── public/
│   │   ├── ash.jpeg              # Product images
│   │   ├── earth.png
│   │   ├── forest.png
│   │   ├── ocean.png
│   │   ├── midnight.png
│   │   └── index.html
│   ├── src/
│   │   ├── components/           # React components
│   │   │   ├── Home.js          # Homepage
│   │   │   ├── ProductGrid.js   # Product catalog
│   │   │   ├── Cart.js          # Shopping cart
│   │   │   ├── Navbar.js        # Navigation
│   │   │   └── purchase*.js     # Individual product pages
│   │   ├── pages/               # Main pages
│   │   │   ├── CheckoutPage.js  # Checkout form
│   │   │   └── ConfirmationPage.js # Order confirmation
│   │   ├── context/             # State management
│   │   │   └── CartContext.js   # Cart state
│   │   ├── config/              # API configuration
│   │   │   └── api.js           # API endpoints and helpers
│   │   ├── styles/              # CSS files
│   │   │   ├── Home.css         # Homepage styles
│   │   │   ├── CheckoutPage.css # Checkout styles
│   │   │   └── ConfirmationPage.css # Confirmation styles
│   │   └── App.js               # Main app component
│   └── package.json
├── aws-lambda-functions/         # AWS Serverless Backend
│   └── inventory-management/    # Inventory Lambda functions
│       ├── get-inventory.js     # Get available products
│       ├── update-inventory.js  # Update stock levels
│       ├── order-processing-with-inventory.js # Process orders
│       ├── setup-inventory-table.js # Populate sample data
│       ├── test-payloads.json   # Test data
│       └── INVENTORY_DEPLOYMENT_GUIDE.md # Deployment guide
└── order-backend/               # Legacy Backend (Node.js/Express)
    ├── server.js                # Main server file
    ├── validation.js            # Validation logic
    ├── .env                     # Environment variables
    └── package.json
```

## 🚀 Getting Started

### Prerequisites
- **Node.js** (version 14 or higher)
- **npm** (comes with Node.js)
- **AWS Account** with DynamoDB access
- **AWS Credentials** (Access Key ID and Secret Access Key)

### Installation

1. **Clone the repository**
   ```bash
   git clone [your-repository-url]
   cd JEAR-ecommerce-CSE5234
   ```

2. **Backend Setup** (Run First)
   ```bash
   # Navigate to backend directory
   cd order-backend
   
   # Install dependencies
   npm install
   
   # Create .env file with AWS credentials
   # Copy your .env file or create one with:
   # AWS_ACCESS_KEY_ID=your_access_key
   # AWS_SECRET_ACCESS_KEY=your_secret_key
   # AWS_REGION=us-east-2
   
   # Start the backend server
   node server.js
   ```

3. **Frontend Setup** (In a new terminal)
   ```bash
   # Open a new terminal window/tab
   # Navigate to frontend directory
   cd jear
   
   # Install dependencies
   npm install
   
   # Start the React development server
   npm start
   ```

### Access the Application
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:4000

## 🎯 User Journey

1. **Homepage** - Browse featured sweatshirts
2. **Product Pages** - View individual products with size selection
3. **Add to Cart** - Add items with quantity selection
4. **Cart Management** - View, update, or remove items
5. **Checkout** - Fill out comprehensive order form
6. **Order Confirmation** - Receive order number and receipt

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
- **Email Format**: Valid email address required
- **Phone Number**: 10-digit phone number validation
- **Country-State Logic**: State/province validation based on selected country
- **US States**: 50 US states with 2-letter codes
- **Canadian Provinces**: 13 provinces/territories
- **Mexican States**: 32 Mexican states
- **Australian States**: 8 states/territories
- **ZIP Code Validation**: US ZIP code format (12345 or 12345-6789)
- **Card Formatting**: Automatic spacing and formatting
- **Expiry Date**: MM/YY format validation

## 🗄️ Database Schema

### Orders Table (DynamoDB)
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
    "country": "United States",
    "cardNumber": "4532123456789012",
    "cardholderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123"
  },
  "total": 59.98,
  "createdAt": "2025-10-12T22:52:59.374Z"
}
```

### Inventory Table (DynamoDB)
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

**Primary Key**: `productId` (String) + `size` (String)
**Purpose**: Tracks inventory levels for each product size combination

## 🔧 API Endpoints

### AWS Serverless Endpoints (Primary)
- `GET /api/health` - Health check
- `GET /api/inventory` - Get available products with stock
- `POST /api/inventory/update` - Update inventory levels (admin)
- `POST /api/orders` - Submit new order with inventory management
- `POST /api/contact` - Submit contact form

### Legacy Backend Endpoints (Express)
- `GET /api/health` - Health check
- `POST /api/orders` - Submit new order
- `GET /api/orders` - Retrieve all orders (testing)
- `POST /api/contact` - Submit contact form

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
  "customerInfo": {...},
  "total": 59.98
}

// Response
{
  "message": "Order saved successfully!",
  "orderID": "1760309579240",
  "orderNumber": "AA00000014",
  "status": "success",
  "inventoryUpdates": [...]
}
```

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
- [ ] Order submission to database
- [ ] Confirmation page displays order details
- [ ] Responsive design on mobile/tablet

### Test Scenarios
1. **Complete Purchase Flow** - Add items → Cart → Checkout → Confirmation
2. **Form Validation** - Test with invalid data
3. **Country-State Logic** - Test different country selections
4. **Cart Management** - Add, update, remove items
5. **Mobile Responsiveness** - Test on different screen sizes

## 🚨 Troubleshooting

### Common Issues

**Backend Issues:**
- **"AWS credentials invalid"** → Check `.env` file
- **"DynamoDB connection failed"** → Verify AWS credentials and region
- **"Port 4000 already in use"** → Kill existing processes

**Frontend Issues:**
- **"Port 3000 already in use"** → Kill React processes
- **"Module not found"** → Run `npm install` in both directories
- **"Cart not updating"** → Check CartContext implementation

**General Issues:**
- **"Backend not responding"** → Ensure backend is running first
- **"Images not loading"** → Check if image files exist in public folder
- **"Styling broken"** → Verify Bootstrap CDN is loaded

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

## 📝 Environment Variables

### Backend (.env file)
```env
AWS_ACCESS_KEY_ID=your_access_key_here
AWS_SECRET_ACCESS_KEY=your_secret_key_here
AWS_REGION=us-east-2
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Team

- **Frontend Development** - React.js implementation
- **Backend Development** - Node.js/Express API
- **Database Integration** - AWS DynamoDB
- **UI/UX Design** - Bootstrap 5 styling

## 🎉 Success Metrics

- ✅ **Full E-commerce Flow** - Complete user journey
- ✅ **AWS Serverless Architecture** - Lambda functions and API Gateway
- ✅ **Real-time Inventory Management** - Stock tracking and updates
- ✅ **Database Integration** - Orders and inventory saved to DynamoDB
- ✅ **Form Validation** - Client and server-side validation
- ✅ **Responsive Design** - Works on all devices
- ✅ **Professional UI** - Modern, clean design
- ✅ **Error Handling** - Comprehensive error management
- ✅ **Scalable Backend** - Serverless architecture for production

---

**Built for CSE 5234 - Modern Web Development**
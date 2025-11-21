const express = require('express');
const AWS = require('aws-sdk');
const cors = require('cors');
const { validateOrder } = require('./validation');
require('dotenv').config();

const app = express();
app.use(express.json());
app.use(cors());

// Configure AWS with explicit credentials
AWS.config.update({ 
  region: process.env.AWS_REGION,
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY
});

// Create DynamoDB DocumentClient
const docClient = new AWS.DynamoDB.DocumentClient();

// Function to generate sequential order number
const generateOrderNumber = async () => {
  try {
    // Get the current count of orders
    const params = {
      TableName: 'orders',
      Select: 'COUNT'
    };
    const result = await docClient.scan(params).promise();
    const orderCount = result.Count || 0;
    
    // Generate order number: AA + 8-digit sequential number
    const sequentialNumber = (orderCount + 1).toString().padStart(8, '0');
    return `AA${sequentialNumber}`;
  } catch (error) {
    console.error('Error generating order number:', error);
    // Fallback to timestamp-based if there's an error
    return `AA${Date.now().toString().slice(-8)}`;
  }
};

// Test AWS connection
const testConnection = async () => {
  try {
    const result = await docClient.scan({ TableName: 'orders', Limit: 1 }).promise();
    console.log('✅ AWS DynamoDB connection successful');
  } catch (error) {
    console.error('❌ AWS DynamoDB connection failed:', error.message);
  }
};

testConnection();

app.post('/api/orders', async (req, res) => {
  console.log('📦 Received order:', JSON.stringify(req.body, null, 2));
  
  const orderData = req.body;
  
  // Validate the order data
  const validation = validateOrder(orderData);
  if (!validation.valid) {
    console.log('❌ Validation failed:', validation.errors);
    return res.status(400).json({
      error: 'Validation failed',
      details: validation.errors,
      status: 'error'
    });
  }
  
  const orderId = Date.now().toString();
  
  // Generate sequential order number
  const orderNumber = await generateOrderNumber();
  console.log('🔢 Generated order number:', orderNumber);
  
  const params = {
    TableName: 'orders',
    Item: {
      orderID: orderId,  // Internal timestamp-based ID
      orderNumber: orderNumber,  // Customer-facing sequential number
      ...orderData,
      createdAt: new Date().toISOString()
    }
  };
  
  try {
    console.log('💾 Saving to DynamoDB:', JSON.stringify(params, null, 2));
    await docClient.put(params).promise();
    console.log('✅ Order saved successfully with ID:', orderId, 'and Order Number:', orderNumber);
    res.status(201).json({ 
      message: 'Order saved successfully!', 
      orderID: orderId,
      orderNumber: orderNumber,  // Return the customer-facing number
      status: 'success'
    });
  } catch (err) {
    console.error('❌ Error saving order:', err);
    res.status(500).json({ 
      error: 'Failed to save order',
      details: err.message,
      status: 'error'
    });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    service: 'JEAR Order Backend'
  });
});

// Get all orders (for testing)
app.get('/api/orders', async (req, res) => {
  try {
    const params = {
      TableName: 'orders'
    };
    const result = await docClient.scan(params).promise();
    res.json({ 
      orders: result.Items,
      count: result.Count 
    });
  } catch (err) {
    console.error('❌ Error fetching orders:', err);
    res.status(500).json({ 
      error: 'Failed to fetch orders',
      details: err.message 
    });
  }
});

// Contact form endpoint
app.post('/api/contact', async (req, res) => {
  console.log('📧 Received contact form:', JSON.stringify(req.body, null, 2));
  
  const contactData = req.body;
  
  // Basic validation
  if (!contactData.name || !contactData.email || !contactData.subject || !contactData.message) {
    return res.status(400).json({
      error: 'Missing required fields',
      status: 'error'
    });
  }
  
  const contactId = Date.now().toString();
  
  const params = {
    TableName: 'contact_messages',
    Item: {
      contactID: contactId,
      name: contactData.name,
      email: contactData.email,
      subject: contactData.subject,
      message: contactData.message,
      createdAt: new Date().toISOString(),
      status: 'new' // new, read, replied
    }
  };
  
  try {
    console.log('💾 Saving contact message to DynamoDB:', JSON.stringify(params, null, 2));
    await docClient.put(params).promise();
    console.log('✅ Contact message saved successfully with ID:', contactId);
    res.status(201).json({ 
      message: 'Contact message sent successfully!', 
      contactID: contactId,
      status: 'success'
    });
  } catch (err) {
    console.error('❌ Error saving contact message:', err);
    res.status(500).json({ 
      error: 'Failed to send message',
      details: err.message,
      status: 'error'
    });
  }
});

// Get all contact messages (for admin)
app.get('/api/contact', async (req, res) => {
  try {
    const params = {
      TableName: 'contact_messages'
    };
    const result = await docClient.scan(params).promise();
    res.json({ 
      messages: result.Items,
      count: result.Count 
    });
  } catch (err) {
    console.error('❌ Error fetching contact messages:', err);
    res.status(500).json({ 
      error: 'Failed to fetch contact messages',
      details: err.message 
    });
  }
});

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => console.log(`🚀 Server running on port ${PORT}`));

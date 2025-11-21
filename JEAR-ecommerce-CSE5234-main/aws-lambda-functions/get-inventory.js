const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, ScanCommand } = require('@aws-sdk/lib-dynamodb');

const client = new DynamoDBClient({ region: process.env.AWS_REGION || 'us-east-2' });
const docClient = DynamoDBDocumentClient.from(client);

exports.handler = async (event) => {
  console.log('Inventory request:', JSON.stringify(event, null, 2));
  
  const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET, OPTIONS',
    'Content-Type': 'application/json'
  };

  // Handle CORS preflight
  if (event.httpMethod === 'OPTIONS') {
    return {
      statusCode: 200,
      headers,
      body: JSON.stringify({ message: 'CORS preflight successful' })
    };
  }

  try {
    // Scan the inventory table to get all products
    const params = {
      TableName: 'inventory'
    };

    const result = await docClient.send(new ScanCommand(params));
    
    // Process the inventory data to show only available items
    const inventory = result.Items || [];
    
    // Group products by ID and process availability
    const processedInventory = {};
    
    inventory.forEach(item => {
      const productId = item.productId;
      
      if (!processedInventory[productId]) {
        processedInventory[productId] = {
          productId: item.productId,
          name: item.name,
          description: item.description,
          price: item.price,
          category: item.category,
          image: item.image,
          sizes: {},
          totalStock: 0
        };
      }
      
      // Add size information
      processedInventory[productId].sizes[item.size] = {
        size: item.size,
        quantity: item.quantity,
        available: item.quantity > 0
      };
      
      processedInventory[productId].totalStock += item.quantity;
    });

    // Convert to array and filter out products with no stock
    const availableProducts = Object.values(processedInventory).filter(product => 
      product.totalStock > 0
    );

    console.log(`Retrieved ${availableProducts.length} products with inventory`);

    return {
      statusCode: 200,
      headers,
      body: JSON.stringify({
        success: true,
        products: availableProducts,
        totalProducts: availableProducts.length,
        timestamp: new Date().toISOString()
      })
    };

  } catch (error) {
    console.error('Error retrieving inventory:', error);
    
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({
        success: false,
        error: 'Failed to retrieve inventory',
        message: error.message,
        timestamp: new Date().toISOString()
      })
    };
  }
};

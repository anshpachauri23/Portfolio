const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, UpdateCommand, GetCommand } = require('@aws-sdk/lib-dynamodb');

const client = new DynamoDBClient({ region: process.env.AWS_REGION || 'us-east-2' });
const docClient = DynamoDBDocumentClient.from(client);

exports.handler = async (event) => {
  console.log('Update inventory request:', JSON.stringify(event, null, 2));
  
  const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
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
    const orderData = JSON.parse(event.body);
    const { items } = orderData;
    
    if (!items || !Array.isArray(items)) {
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({
          success: false,
          error: 'Invalid order data - items array required'
        })
      };
    }

    const updateResults = [];
    const errors = [];

    // Process each item in the order
    for (const item of items) {
      try {
        const { productId, size, quantity } = item;
        
        if (!productId || !size || !quantity) {
          errors.push(`Invalid item data: ${JSON.stringify(item)}`);
          continue;
        }

        // Get current inventory for this product/size
        const getParams = {
          TableName: 'inventory',
          Key: {
            productId: productId,
            size: size
          }
        };

        const currentItem = await docClient.send(new GetCommand(getParams));
        
        if (!currentItem.Item) {
          errors.push(`Product ${productId} size ${size} not found in inventory`);
          continue;
        }

        const currentQuantity = currentItem.Item.quantity;
        
        if (currentQuantity < quantity) {
          errors.push(`Insufficient stock for ${productId} size ${size}. Available: ${currentQuantity}, Requested: ${quantity}`);
          continue;
        }

        // Update inventory by subtracting the ordered quantity
        const updateParams = {
          TableName: 'inventory',
          Key: {
            productId: productId,
            size: size
          },
          UpdateExpression: 'SET quantity = quantity - :orderedQuantity, lastUpdated = :timestamp',
          ExpressionAttributeValues: {
            ':orderedQuantity': quantity,
            ':timestamp': new Date().toISOString()
          },
          ConditionExpression: 'quantity >= :orderedQuantity',
          ReturnValues: 'ALL_NEW'
        };

        const updateResult = await docClient.send(new UpdateCommand(updateParams));
        
        updateResults.push({
          productId,
          size,
          orderedQuantity: quantity,
          remainingQuantity: updateResult.Attributes.quantity,
          success: true
        });

        console.log(`Updated inventory for ${productId} size ${size}: ${quantity} units removed`);

      } catch (itemError) {
        console.error(`Error updating inventory for item:`, item, itemError);
        errors.push(`Failed to update ${item.productId} size ${item.size}: ${itemError.message}`);
      }
    }

    // If there were any errors, return partial success
    if (errors.length > 0) {
      return {
        statusCode: 207, // Multi-Status
        headers,
        body: JSON.stringify({
          success: false,
          message: 'Some inventory updates failed',
          updateResults,
          errors,
          timestamp: new Date().toISOString()
        })
      };
    }

    return {
      statusCode: 200,
      headers,
      body: JSON.stringify({
        success: true,
        message: 'Inventory updated successfully',
        updateResults,
        timestamp: new Date().toISOString()
      })
    };

  } catch (error) {
    console.error('Error updating inventory:', error);
    
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({
        success: false,
        error: 'Failed to update inventory',
        message: error.message,
        timestamp: new Date().toISOString()
      })
    };
  }
};

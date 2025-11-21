const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, PutCommand, UpdateCommand, GetCommand } = require('@aws-sdk/lib-dynamodb');

const client = new DynamoDBClient({ region: process.env.AWS_REGION || 'us-east-2' });
const docClient = DynamoDBDocumentClient.from(client);

// Validation functions (same as original)
const validateCreditCard = (cardNumber) => {
  const cleaned = cardNumber.replace(/\s/g, '');
  if (!/^\d{13,19}$/.test(cleaned)) return { valid: false, error: 'Invalid card number format' };
  
  let sum = 0;
  let isEven = false;
  
  for (let i = cleaned.length - 1; i >= 0; i--) {
    let digit = parseInt(cleaned[i]);
    
    if (isEven) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    
    sum += digit;
    isEven = !isEven;
  }
  
  return { valid: sum % 10 === 0, error: sum % 10 === 0 ? null : 'Invalid card number' };
};

const validateExpiryDate = (expiryDate) => {
  if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(expiryDate)) {
    return { valid: false, error: 'Invalid expiry date format' };
  }
  
  const [month, year] = expiryDate.split('/');
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear() % 100;
  const currentMonth = currentDate.getMonth() + 1;
  
  const expYear = parseInt(year);
  const expMonth = parseInt(month);
  
  if (expYear < currentYear) {
    return { valid: false, error: 'Card has expired' };
  }
  if (expYear === currentYear && expMonth < currentMonth) {
    return { valid: false, error: 'Card has expired' };
  }
  
  return { valid: true, error: null };
};

const validateOrder = (orderData) => {
  const errors = [];
  
  if (!orderData.customerInfo) {
    errors.push('Customer information is required');
    return { valid: false, errors };
  }
  
  const { customerInfo } = orderData;
  
  if (!customerInfo.email || !/\S+@\S+\.\S+/.test(customerInfo.email)) {
    errors.push('Valid email is required');
  }
  
  if (!customerInfo.phone || customerInfo.phone.replace(/\D/g, '').length < 10) {
    errors.push('Valid phone number is required');
  }
  
  if (!customerInfo.firstName) errors.push('First name is required');
  if (!customerInfo.lastName) errors.push('Last name is required');
  if (!customerInfo.address) errors.push('Address is required');
  if (!customerInfo.city) errors.push('City is required');
  if (!customerInfo.state) errors.push('State is required');
  if (!customerInfo.zipCode) errors.push('ZIP code is required');
  if (!customerInfo.country) errors.push('Country is required');
  
  if (customerInfo.cardNumber) {
    const cleanedCard = customerInfo.cardNumber.replace(/\s/g, '');
    if (cleanedCard.length < 13 || cleanedCard.length > 19) {
      errors.push('Card number must be 13-19 digits');
    }
  }
  
  if (customerInfo.expiryDate) {
    const expiryValidation = validateExpiryDate(customerInfo.expiryDate);
    if (!expiryValidation.valid) {
      errors.push(expiryValidation.error);
    }
  }
  
  if (customerInfo.cvv && (customerInfo.cvv.length < 3 || customerInfo.cvv.length > 4)) {
    errors.push('CVV must be 3 or 4 digits');
  }
  
  return {
    valid: errors.length === 0,
    errors: errors
  };
};

exports.handler = async (event) => {
  console.log('Order processing request:', JSON.stringify(event, null, 2));
  
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
    
    // Validate the order data
    const validation = validateOrder(orderData);
    if (!validation.valid) {
      console.log('❌ Validation failed:', validation.errors);
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({
          error: 'Validation failed',
          details: validation.errors,
          status: 'error'
        })
      };
    }

    // Check inventory availability before processing order
    const inventoryChecks = [];
    const inventoryErrors = [];

    for (const item of orderData.items) {
      try {
        const { productId, size, quantity } = item;
        
        // Get current inventory
         const getParams = {
           TableName: 'inventory',
          Key: {
            productId: productId,
            size: size
          }
        };

        const currentItem = await docClient.send(new GetCommand(getParams));
        
        if (!currentItem.Item) {
          inventoryErrors.push(`Product ${productId} size ${size} not found in inventory`);
          continue;
        }

        const availableQuantity = currentItem.Item.quantity;
        
         if (availableQuantity < quantity) {
           inventoryErrors.push(`Insufficient stock for ${productId} size ${size}. Available: ${availableQuantity}, Requested: ${quantity}`);
        } else {
          inventoryChecks.push({
            productId,
            size,
            requestedQuantity: quantity,
            availableQuantity,
            sufficient: true
          });
        }

      } catch (itemError) {
        console.error(`Error checking inventory for item:`, item, itemError);
        inventoryErrors.push(`Failed to check inventory for ${item.productId} size ${item.size}: ${itemError.message}`);
      }
    }

    // If there are inventory issues, return error
    if (inventoryErrors.length > 0) {
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({
          error: 'Insufficient inventory',
          details: inventoryErrors,
          status: 'error'
        })
      };
    }

    // Generate order ID and number
    const orderId = Date.now().toString();
    const orderNumber = `AA${(Math.floor(Math.random() * 90000000) + 10000000).toString()}`;

     // Save the order
     const orderParams = {
       TableName: 'orders',
      Item: {
        orderID: orderId,
        orderNumber: orderNumber,
        ...orderData,
        createdAt: new Date().toISOString(),
        status: 'confirmed'
      }
    };

    await docClient.send(new PutCommand(orderParams));
    console.log('✅ Order saved successfully with ID:', orderId, 'and Order Number:', orderNumber);

    // Update inventory after successful order
    const inventoryUpdates = [];
    
    for (const item of orderData.items) {
      try {
        const { productId, size, quantity } = item;
        
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
        
        inventoryUpdates.push({
          productId,
          size,
          orderedQuantity: quantity,
          remainingQuantity: updateResult.Attributes.quantity,
          success: true
        });

        console.log(`✅ Updated inventory for ${productId} size ${size}: ${quantity} units removed`);

      } catch (updateError) {
        console.error(`❌ Error updating inventory for item:`, item, updateError);
        inventoryUpdates.push({
          productId: item.productId,
          size: item.size,
          success: false,
          error: updateError.message
        });
      }
    }

    return {
      statusCode: 201,
      headers,
      body: JSON.stringify({
        message: 'Order saved successfully!',
        orderID: orderId,
        orderNumber: orderNumber,
        status: 'success',
        inventoryUpdates: inventoryUpdates,
        timestamp: new Date().toISOString()
      })
    };

  } catch (error) {
    console.error('❌ Error processing order:', error);
    
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({
        error: 'Failed to process order',
        details: error.message,
        status: 'error',
        timestamp: new Date().toISOString()
      })
    };
  }
};

// Contact Form Lambda Function - AWS SDK v3 (Modern Approach)
const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, PutCommand } = require('@aws-sdk/lib-dynamodb');

// Configure AWS SDK v3
const client = new DynamoDBClient({ 
  region: process.env.AWS_REGION || 'us-east-2'
});
const docClient = DynamoDBDocumentClient.from(client);

exports.handler = async (event) => {
  console.log('📧 Received contact form:', JSON.stringify(event, null, 2));
  
  // CORS headers
  const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
    'Content-Type': 'application/json'
  };
  
  // Handle preflight requests
  if (event.httpMethod === 'OPTIONS') {
    return {
      statusCode: 200,
      headers,
      body: JSON.stringify({ message: 'CORS preflight' })
    };
  }
  
  try {
    const contactData = JSON.parse(event.body);
    
    // Validate required fields
    if (!contactData.name || !contactData.email || !contactData.subject || !contactData.message) {
      console.log('❌ Validation failed: Missing required fields');
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({
          error: 'Validation failed',
          details: ['Name, email, subject, and message are required'],
          status: 'error'
        })
      };
    }
    
    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(contactData.email)) {
      console.log('❌ Validation failed: Invalid email format');
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({
          error: 'Validation failed',
          details: ['Invalid email format'],
          status: 'error'
        })
      };
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
        status: 'new'
      }
    };
    
    console.log('💾 Saving contact message to DynamoDB:', JSON.stringify(params, null, 2));
    await docClient.send(new PutCommand(params));
    console.log('✅ Contact message saved successfully with ID:', contactId);
    
    return {
      statusCode: 201,
      headers,
      body: JSON.stringify({ 
        message: 'Contact message saved successfully!', 
        contactID: contactId,
        status: 'success'
      })
    };
    
  } catch (err) {
    console.error('❌ Error processing contact form:', err);
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({ 
        error: 'Failed to process contact form',
        details: err.message,
        status: 'error'
      })
    };
  }
};

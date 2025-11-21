// Health Check Lambda Function - AWS SDK v3 (Modern Approach)
// No AWS SDK needed for health check, but keeping consistent structure

exports.handler = async (event) => {
  console.log('🏥 Health check requested:', JSON.stringify(event, null, 2));
  
  // CORS headers
  const headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET, OPTIONS',
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
    const healthData = {
      status: 'OK',
      timestamp: new Date().toISOString(),
      service: 'JEAR Serverless Backend',
      version: '1.0.0',
      nodeVersion: process.version,
      runtime: 'AWS Lambda',
      region: process.env.AWS_REGION || 'us-east-2',
      environment: process.env.NODE_ENV || 'production'
    };
    
    console.log('✅ Health check successful:', healthData);
    
    return {
      statusCode: 200,
      headers,
      body: JSON.stringify(healthData)
    };
    
  } catch (err) {
    console.error('❌ Health check failed:', err);
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({ 
        status: 'ERROR',
        timestamp: new Date().toISOString(),
        service: 'JEAR Serverless Backend',
        error: err.message
      })
    };
  }
};

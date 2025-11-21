const AWS = require('aws-sdk');
require('dotenv').config();

// Configure AWS with explicit credentials
AWS.config.update({
  region: process.env.AWS_REGION,
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY
});

const dynamodb = new AWS.DynamoDB();

const createContactMessagesTable = async () => {
  const params = {
    TableName: 'contact_messages',
    KeySchema: [
      { AttributeName: 'contactID', KeyType: 'HASH' } // Partition key
    ],
    AttributeDefinitions: [
      { AttributeName: 'contactID', AttributeType: 'S' },
      { AttributeName: 'createdAt', AttributeType: 'S' } // For GSI
    ],
    ProvisionedThroughput: {
      ReadCapacityUnits: 5,
      WriteCapacityUnits: 5
    },
    GlobalSecondaryIndexes: [
      {
        IndexName: 'createdAt-index',
        KeySchema: [
          { AttributeName: 'createdAt', KeyType: 'HASH' }
        ],
        Projection: {
          ProjectionType: 'ALL'
        },
        ProvisionedThroughput: {
          ReadCapacityUnits: 5,
          WriteCapacityUnits: 5
        }
      }
    ]
  };

  try {
    console.log('🔧 Creating contact_messages table...');
    await dynamodb.createTable(params).promise();
    console.log('✅ Contact messages table created successfully!');
    console.log('Table ARN:', (await dynamodb.describeTable({ TableName: 'contact_messages' }).promise()).Table.TableArn);
    
    // Wait for the table to become active
    console.log('⏳ Waiting for table to be active...');
    await dynamodb.waitFor('tableExists', { TableName: 'contact_messages' }).promise();
    console.log('✅ Table is now active and ready to use!');
    
    console.log('\n📋 Table Structure:');
    console.log('- Primary Key: contactID (String)');
    console.log('- Attributes: name, email, subject, message, createdAt, status');
    console.log('- GSI: createdAt-index for sorting by date');
    console.log('\n🔗 API Endpoints:');
    console.log('- POST /api/contact - Submit contact form');
    console.log('- GET /api/contact - Get all contact messages (admin)');
    
  } catch (error) {
    if (error.code === 'ResourceInUseException') {
      console.log('❌ Contact messages table already exists.');
    } else {
      console.error('❌ Error creating contact messages table:', error);
    }
  }
};

const checkTableExists = async () => {
  try {
    await dynamodb.describeTable({ TableName: 'contact_messages' }).promise();
    return true;
  } catch (error) {
    if (error.code === 'ResourceNotFoundException') {
      return false;
    }
    throw error;
  }
};

(async () => {
  console.log('🚀 Setting up contact messages table...');
  const tableExists = await checkTableExists();
  if (!tableExists) {
    await createContactMessagesTable();
  } else {
    console.log('❌ Contact messages table does not exist');
    await createContactMessagesTable();
  }
})();
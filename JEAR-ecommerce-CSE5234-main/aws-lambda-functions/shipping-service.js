const crypto = require('crypto');
const uuidv4 = () => crypto.randomUUID();
const { DynamoDBClient } = require("@aws-sdk/client-dynamodb");
const { DynamoDBDocumentClient, PutCommand } = require("@aws-sdk/lib-dynamodb");

const client = new DynamoDBClient({ region: process.env.AWS_REGION || "us-east-2" });
const docClient = DynamoDBDocumentClient.from(client);

const TABLE_NAME = "ShippingTable"; // Update if needed

exports.handler = async (event) => {
    console.log("📨 Shipping Event Received:", JSON.stringify(event, null, 2));

    const headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type',
        'Access-Control-Allow-Methods': 'POST, OPTIONS',
        'Content-Type': 'application/json'
    };

    try {
        // EventBridge payload
        const detail = event.detail;

        const { businessId, shipmentAddress, numPackets, weightPerPacket } = detail;

        if (!businessId || !shipmentAddress || !numPackets || !weightPerPacket) {
            return {
                statusCode: 400,
                body: JSON.stringify({ error: "Missing required shipping fields." })
            };
        }

        // Generate unique shipping ID (matches ShippingTable partition key)
        const shippingID = uuidv4();

        const item = {
            shippingID,  // Partition key for existing ShippingTable
            businessId,
            shipmentAddress,
            numPackets,
            weightPerPacket,
            status: "initiated",
            createdAt: new Date().toISOString()
        };

        await docClient.send(new PutCommand({
            TableName: TABLE_NAME,
            Item: item
        }));

        console.log("📦 Shipping persisted with ID:", shippingID);

        return {
            statusCode: 200,
            body: JSON.stringify({ 
                shippingID: shippingID,
                status: "initiated" 
            })
        };

    } catch (error) {
        console.error("Error:", error);

        return {
            statusCode: 500,
            headers,
            body: JSON.stringify({
                error: "Failed to process shipping request",
                details: error.message
            })
        };
    }
};


// API Configuration for JEAR E-commerce Application
// AWS Serverless Backend Integration

const API_BASE_URL = 'https://udnv1njlv1.execute-api.us-east-2.amazonaws.com/dev';

export const API_ENDPOINTS = {
  // Inventory
  INVENTORY: `${API_BASE_URL}/api/inventory`,

  // Order Processing
  ORDERS: `${API_BASE_URL}/api/orders`,
  
  // Contact Form
  CONTACT: `${API_BASE_URL}/api/contact`,
  
  // Health Check
  HEALTH: `${API_BASE_URL}/api/health`
};

export const API_CONFIG = {
  baseURL: API_BASE_URL,
  timeout: 10000, // 10 seconds
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
};

// Helper function to make API calls
export const apiCall = async (endpoint, options = {}) => {
  const defaultOptions = {
    headers: API_CONFIG.headers,
    ...options
  };

  try {
    const response = await fetch(endpoint, defaultOptions);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API call failed:', error);
    throw error;
  }
};

export default API_ENDPOINTS;

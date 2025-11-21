// Advanced validation utilities for the backend

// Credit card validation using Luhn algorithm
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

// Validate card type
const getCardType = (cardNumber) => {
  const cleaned = cardNumber.replace(/\s/g, '');
  
  if (/^4/.test(cleaned)) return 'Visa';
  if (/^5[1-5]/.test(cleaned)) return 'Mastercard';
  if (/^3[47]/.test(cleaned)) return 'American Express';
  if (/^6/.test(cleaned)) return 'Discover';
  
  return 'Unknown';
};

// Validate expiry date
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

  // US State validation
  const validateUSState = (state) => {
    const usStates = [
      'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA',
      'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
      'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
      'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
      'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY'
    ];
    return usStates.includes(state.toUpperCase());
  };

  // Canadian Province validation
  const validateCanadianProvince = (province) => {
    const canadianProvinces = [
      'AB', 'BC', 'MB', 'NB', 'NL', 'NS', 'ON', 'PE', 'QC', 'SK',
      'NT', 'NU', 'YT'
    ];
    return canadianProvinces.includes(province.toUpperCase());
  };

  // Mexican State validation
  const validateMexicanState = (state) => {
    const mexicanStates = [
      'AGU', 'BCN', 'BCS', 'CAM', 'CHP', 'CHH', 'COA', 'COL', 'DIF',
      'DUR', 'GUA', 'GRO', 'HID', 'JAL', 'MEX', 'MIC', 'MOR', 'NAY',
      'NLE', 'OAX', 'PUE', 'QUE', 'ROO', 'SLP', 'SIN', 'SON', 'TAB',
      'TAM', 'TLA', 'VER', 'YUC', 'ZAC'
    ];
    return mexicanStates.includes(state.toUpperCase());
  };

  // Australian State validation
  const validateAustralianState = (state) => {
    const australianStates = [
      'NSW', 'VIC', 'QLD', 'WA', 'SA', 'TAS', 'ACT', 'NT'
    ];
    return australianStates.includes(state.toUpperCase());
  };

// ZIP code validation for US
const validateUSZipCode = (zipCode) => {
  return /^\d{5}(-\d{4})?$/.test(zipCode);
};

// Comprehensive order validation
const validateOrder = (orderData) => {
  const errors = [];
  
  // Validate customer info
  if (!orderData.customerInfo) {
    errors.push('Customer information is required');
    return { valid: false, errors };
  }
  
  const { customerInfo } = orderData;
  
  // Email validation
  if (!customerInfo.email || !/\S+@\S+\.\S+/.test(customerInfo.email)) {
    errors.push('Valid email is required');
  }
  
  // Phone validation
  if (!customerInfo.phone || customerInfo.phone.replace(/\D/g, '').length < 10) {
    errors.push('Valid phone number is required');
  }
  
  // Address validation
  if (!customerInfo.firstName) errors.push('First name is required');
  if (!customerInfo.lastName) errors.push('Last name is required');
  if (!customerInfo.address) errors.push('Address is required');
  if (!customerInfo.city) errors.push('City is required');
  if (!customerInfo.state) errors.push('State is required');
  if (!customerInfo.zipCode) errors.push('ZIP code is required');
  if (!customerInfo.country) errors.push('Country is required');
  
  // Country-specific validations
  if (customerInfo.country && customerInfo.country.toLowerCase().includes('united states')) {
    // US-specific validations
    if (customerInfo.state && !validateUSState(customerInfo.state)) {
      errors.push('Invalid US state');
    }
    
    if (customerInfo.zipCode && !validateUSZipCode(customerInfo.zipCode)) {
      errors.push('Invalid US ZIP code');
    }
  } else if (customerInfo.country && customerInfo.country.toLowerCase().includes('canada')) {
    // Canada-specific validations
    if (customerInfo.state && !validateCanadianProvince(customerInfo.state)) {
      errors.push('Invalid Canadian province');
    }
  } else if (customerInfo.country && customerInfo.country.toLowerCase().includes('mexico')) {
    // Mexico-specific validations
    if (customerInfo.state && !validateMexicanState(customerInfo.state)) {
      errors.push('Invalid Mexican state');
    }
  } else if (customerInfo.country && customerInfo.country.toLowerCase().includes('australia')) {
    // Australia-specific validations
    if (customerInfo.state && !validateAustralianState(customerInfo.state)) {
      errors.push('Invalid Australian state/territory');
    }
  }
  
  // Basic payment validation (no Luhn check)
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
  
  // Basic CVV validation
  if (customerInfo.cvv && (customerInfo.cvv.length < 3 || customerInfo.cvv.length > 4)) {
    errors.push('CVV must be 3 or 4 digits');
  }
  
  return {
    valid: errors.length === 0,
    errors: errors
  };
};

module.exports = {
  validateCreditCard,
  getCardType,
  validateExpiryDate,
  validateUSState,
  validateUSZipCode,
  validateCanadianProvince,
  validateMexicanState,
  validateAustralianState,
  validateOrder
};

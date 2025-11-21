import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { API_ENDPOINTS, apiCall } from "../config/api";
import "../styles/CheckoutPage.css";

const CheckoutPage = () => {
  const navigate = useNavigate();
  const { cartItems, getCartTotal, clearCart } = useCart();

  // US States data
  const usStates = [
    { code: "AL", name: "Alabama" },
    { code: "AK", name: "Alaska" },
    { code: "AZ", name: "Arizona" },
    { code: "AR", name: "Arkansas" },
    { code: "CA", name: "California" },
    { code: "CO", name: "Colorado" },
    { code: "CT", name: "Connecticut" },
    { code: "DE", name: "Delaware" },
    { code: "FL", name: "Florida" },
    { code: "GA", name: "Georgia" },
    { code: "HI", name: "Hawaii" },
    { code: "ID", name: "Idaho" },
    { code: "IL", name: "Illinois" },
    { code: "IN", name: "Indiana" },
    { code: "IA", name: "Iowa" },
    { code: "KS", name: "Kansas" },
    { code: "KY", name: "Kentucky" },
    { code: "LA", name: "Louisiana" },
    { code: "ME", name: "Maine" },
    { code: "MD", name: "Maryland" },
    { code: "MA", name: "Massachusetts" },
    { code: "MI", name: "Michigan" },
    { code: "MN", name: "Minnesota" },
    { code: "MS", name: "Mississippi" },
    { code: "MO", name: "Missouri" },
    { code: "MT", name: "Montana" },
    { code: "NE", name: "Nebraska" },
    { code: "NV", name: "Nevada" },
    { code: "NH", name: "New Hampshire" },
    { code: "NJ", name: "New Jersey" },
    { code: "NM", name: "New Mexico" },
    { code: "NY", name: "New York" },
    { code: "NC", name: "North Carolina" },
    { code: "ND", name: "North Dakota" },
    { code: "OH", name: "Ohio" },
    { code: "OK", name: "Oklahoma" },
    { code: "OR", name: "Oregon" },
    { code: "PA", name: "Pennsylvania" },
    { code: "RI", name: "Rhode Island" },
    { code: "SC", name: "South Carolina" },
    { code: "SD", name: "South Dakota" },
    { code: "TN", name: "Tennessee" },
    { code: "TX", name: "Texas" },
    { code: "UT", name: "Utah" },
    { code: "VT", name: "Vermont" },
    { code: "VA", name: "Virginia" },
    { code: "WA", name: "Washington" },
    { code: "WV", name: "West Virginia" },
    { code: "WI", name: "Wisconsin" },
    { code: "WY", name: "Wyoming" },
  ];

  // Countries data
  const countries = [
    { code: "US", name: "United States" },
    { code: "CA", name: "Canada" },
    { code: "MX", name: "Mexico" },
    { code: "GB", name: "United Kingdom" },
    { code: "DE", name: "Germany" },
    { code: "FR", name: "France" },
    { code: "IT", name: "Italy" },
    { code: "ES", name: "Spain" },
    { code: "AU", name: "Australia" },
    { code: "JP", name: "Japan" },
    { code: "CN", name: "China" },
    { code: "IN", name: "India" },
    { code: "BR", name: "Brazil" },
    { code: "RU", name: "Russia" },
    { code: "KR", name: "South Korea" },
    { code: "NL", name: "Netherlands" },
    { code: "SE", name: "Sweden" },
    { code: "NO", name: "Norway" },
    { code: "DK", name: "Denmark" },
    { code: "FI", name: "Finland" },
  ];

  // Canadian Provinces
  const canadianProvinces = [
    { code: "AB", name: "Alberta" },
    { code: "BC", name: "British Columbia" },
    { code: "MB", name: "Manitoba" },
    { code: "NB", name: "New Brunswick" },
    { code: "NL", name: "Newfoundland and Labrador" },
    { code: "NS", name: "Nova Scotia" },
    { code: "ON", name: "Ontario" },
    { code: "PE", name: "Prince Edward Island" },
    { code: "QC", name: "Quebec" },
    { code: "SK", name: "Saskatchewan" },
    { code: "NT", name: "Northwest Territories" },
    { code: "NU", name: "Nunavut" },
    { code: "YT", name: "Yukon" },
  ];

  // Mexican States
  const mexicanStates = [
    { code: "AGU", name: "Aguascalientes" },
    { code: "BCN", name: "Baja California" },
    { code: "BCS", name: "Baja California Sur" },
    { code: "CAM", name: "Campeche" },
    { code: "CHP", name: "Chiapas" },
    { code: "CHH", name: "Chihuahua" },
    { code: "COA", name: "Coahuila" },
    { code: "COL", name: "Colima" },
    { code: "DIF", name: "Mexico City" },
    { code: "DUR", name: "Durango" },
    { code: "GUA", name: "Guanajuato" },
    { code: "GRO", name: "Guerrero" },
    { code: "HID", name: "Hidalgo" },
    { code: "JAL", name: "Jalisco" },
    { code: "MEX", name: "Mexico" },
    { code: "MIC", name: "Michoacan" },
    { code: "MOR", name: "Morelos" },
    { code: "NAY", name: "Nayarit" },
    { code: "NLE", name: "Nuevo Leon" },
    { code: "OAX", name: "Oaxaca" },
    { code: "PUE", name: "Puebla" },
    { code: "QUE", name: "Queretaro" },
    { code: "ROO", name: "Quintana Roo" },
    { code: "SLP", name: "San Luis Potosi" },
    { code: "SIN", name: "Sinaloa" },
    { code: "SON", name: "Sonora" },
    { code: "TAB", name: "Tabasco" },
    { code: "TAM", name: "Tamaulipas" },
    { code: "TLA", name: "Tlaxcala" },
    { code: "VER", name: "Veracruz" },
    { code: "YUC", name: "Yucatan" },
    { code: "ZAC", name: "Zacatecas" },
  ];

  // Australian States
  const australianStates = [
    { code: "NSW", name: "New South Wales" },
    { code: "VIC", name: "Victoria" },
    { code: "QLD", name: "Queensland" },
    { code: "WA", name: "Western Australia" },
    { code: "SA", name: "South Australia" },
    { code: "TAS", name: "Tasmania" },
    { code: "ACT", name: "Australian Capital Territory" },
    { code: "NT", name: "Northern Territory" },
  ];

  // Get states/provinces based on country
  const getStatesForCountry = (countryName) => {
    if (!countryName) return [];

    const country = countryName.toLowerCase();
    if (
      country.includes("united states") ||
      country.includes("usa") ||
      country.includes("us")
    ) {
      return usStates;
    } else if (country.includes("canada") || country.includes("ca")) {
      return canadianProvinces;
    } else if (country.includes("mexico") || country.includes("mx")) {
      return mexicanStates;
    } else if (country.includes("australia") || country.includes("au")) {
      return australianStates;
    }
    return []; // For other countries, no states/provinces
  };

  // State for all form fields
  const [formData, setFormData] = useState({
    // Contact Information
    email: "",
    phone: "",

    // Address Information
    firstName: "",
    lastName: "",
    address: "",
    apartment: "",
    city: "",
    state: "",
    zipCode: "",
    country: "United States",

    // Payment Information
    cardNumber: "",
    cardholderName: "",
    expiryDate: "",
    cvv: "",
  });

  // State for form errors
  const [errors, setErrors] = useState({});

  // Format card number with spaces
  const formatCardNumber = (value) => {
    const cleaned = value.replace(/\s/g, "");
    const formatted = cleaned.replace(/(\d{4})(?=\d)/g, "$1 ");
    return formatted;
  };

  // Format expiry date
  const formatExpiryDate = (value) => {
    const cleaned = value.replace(/\D/g, "");
    if (cleaned.length >= 2) {
      return cleaned.substring(0, 2) + "/" + cleaned.substring(2, 4);
    }
    return cleaned;
  };

  // Handle input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    let formattedValue = value;

    // Format specific fields
    if (name === "cardNumber") {
      formattedValue = formatCardNumber(value);
    } else if (name === "expiryDate") {
      formattedValue = formatExpiryDate(value);
    } else if (name === "state") {
      formattedValue = value.toUpperCase();
    }

    setFormData((prev) => {
      const newData = {
        ...prev,
        [name]: formattedValue,
      };

      // Clear state when country changes
      if (name === "country") {
        newData.state = "";
      }

      return newData;
    });

    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  // Validate card type (for display only, no validation)
  const getCardType = (cardNumber) => {
    const cleaned = cardNumber.replace(/\s/g, "");

    if (/^4/.test(cleaned)) return "Visa";
    if (/^5[1-5]/.test(cleaned)) return "Mastercard";
    if (/^3[47]/.test(cleaned)) return "American Express";
    if (/^6/.test(cleaned)) return "Discover";

    return "Card";
  };

  // Validate expiry date
  const validateExpiryDate = (expiryDate) => {
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(expiryDate)) return false;

    const [month, year] = expiryDate.split("/");
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear() % 100;
    const currentMonth = currentDate.getMonth() + 1;

    const expYear = parseInt(year);
    const expMonth = parseInt(month);

    if (expYear < currentYear) return false;
    if (expYear === currentYear && expMonth < currentMonth) return false;

    return true;
  };

  // US State validation
  const validateUSState = (state) => {
    const usStates = [
      "AL",
      "AK",
      "AZ",
      "AR",
      "CA",
      "CO",
      "CT",
      "DE",
      "FL",
      "GA",
      "HI",
      "ID",
      "IL",
      "IN",
      "IA",
      "KS",
      "KY",
      "LA",
      "ME",
      "MD",
      "MA",
      "MI",
      "MN",
      "MS",
      "MO",
      "MT",
      "NE",
      "NV",
      "NH",
      "NJ",
      "NM",
      "NY",
      "NC",
      "ND",
      "OH",
      "OK",
      "OR",
      "PA",
      "RI",
      "SC",
      "SD",
      "TN",
      "TX",
      "UT",
      "VT",
      "VA",
      "WA",
      "WV",
      "WI",
      "WY",
    ];
    return usStates.includes(state.toUpperCase());
  };

  // ZIP code validation for US
  const validateUSZipCode = (zipCode) => {
    return /^\d{5}(-\d{4})?$/.test(zipCode);
  };

  // Form validation
  const validateForm = () => {
    const newErrors = {};

    // Email validation
    if (!formData.email) {
      newErrors.email = "Email is required";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "Email is invalid";
    }

    // Phone validation
    if (!formData.phone) {
      newErrors.phone = "Phone number is required";
    } else if (formData.phone.replace(/\D/g, "").length < 10) {
      newErrors.phone = "Phone number must be at least 10 digits";
    }

    // Address validation - ALL REQUIRED
    if (!formData.firstName) newErrors.firstName = "First name is required";
    if (!formData.lastName) newErrors.lastName = "Last name is required";
    if (!formData.address) newErrors.address = "Address is required";
    if (!formData.city) newErrors.city = "City is required";
    if (!formData.state) newErrors.state = "State is required";
    if (!formData.zipCode) newErrors.zipCode = "ZIP code is required";
    if (!formData.country) newErrors.country = "Country is required";

    // Enhanced address validation
    if (
      formData.country &&
      formData.country.toLowerCase().includes("united states")
    ) {
      // US-specific validations
      if (formData.state && !validateUSState(formData.state)) {
        newErrors.state = "Please enter a valid US state (e.g., CA, NY, TX)";
      }

      if (formData.zipCode && !validateUSZipCode(formData.zipCode)) {
        newErrors.zipCode =
          "Please enter a valid US ZIP code (12345 or 12345-6789)";
      }
    } else if (
      formData.country &&
      formData.country.toLowerCase().includes("canada")
    ) {
      // Canada-specific validations
      const validCanadianProvinces = canadianProvinces.map((p) => p.code);
      if (
        formData.state &&
        !validCanadianProvinces.includes(formData.state.toUpperCase())
      ) {
        newErrors.state =
          "Please enter a valid Canadian province (e.g., ON, BC, QC)";
      }
    } else if (
      formData.country &&
      formData.country.toLowerCase().includes("mexico")
    ) {
      // Mexico-specific validations
      const validMexicanStates = mexicanStates.map((s) => s.code);
      if (
        formData.state &&
        !validMexicanStates.includes(formData.state.toUpperCase())
      ) {
        newErrors.state =
          "Please enter a valid Mexican state (e.g., JAL, NLE, CDMX)";
      }
    } else if (
      formData.country &&
      formData.country.toLowerCase().includes("australia")
    ) {
      // Australia-specific validations
      const validAustralianStates = australianStates.map((s) => s.code);
      if (
        formData.state &&
        !validAustralianStates.includes(formData.state.toUpperCase())
      ) {
        newErrors.state =
          "Please enter a valid Australian state/territory (e.g., NSW, VIC, QLD)";
      }
    } else if (formData.country && formData.state) {
      // For other countries, check if state is valid for that country
      const validStates = getStatesForCountry(formData.country);
      if (validStates.length > 0) {
        const validStateCodes = validStates.map((s) => s.code);
        if (!validStateCodes.includes(formData.state.toUpperCase())) {
          newErrors.state = `Please enter a valid state/province for ${formData.country}`;
        }
      }
    }

    // Basic payment validation (no Luhn check)
    if (!formData.cardNumber) {
      newErrors.cardNumber = "Card number is required";
    } else {
      const cleanedCard = formData.cardNumber.replace(/\s/g, "");
      if (cleanedCard.length < 13 || cleanedCard.length > 19) {
        newErrors.cardNumber = "Card number must be 13-19 digits";
      }
    }

    if (!formData.cardholderName)
      newErrors.cardholderName = "Cardholder name is required";
    if (!formData.expiryDate) newErrors.expiryDate = "Expiry date is required";
    if (!formData.cvv) newErrors.cvv = "CVV is required";

    // Basic CVV validation
    if (formData.cvv && (formData.cvv.length < 3 || formData.cvv.length > 4)) {
      newErrors.cvv = "CVV must be 3 or 4 digits";
    }

    // Enhanced expiry date validation
    if (formData.expiryDate) {
      if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(formData.expiryDate)) {
        newErrors.expiryDate = "Expiry date must be in MM/YY format";
      } else if (!validateExpiryDate(formData.expiryDate)) {
        newErrors.expiryDate = "Card has expired";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = (e) => {
    e.preventDefault();

    if (validateForm()) {
      // Create order object
      const orderData = {
        items: cartItems,
        customerInfo: formData,
        total: getCartTotal(),
        orderDate: new Date().toISOString(),
        // orderNumber will be generated by the backend
      };
      // Send order data to AWS serverless backend
      apiCall(API_ENDPOINTS.ORDERS, {
        method: "POST",
        body: JSON.stringify(orderData),
      })
        .then((data) => {
          console.log("Order saved:", data);
          // Clear the cart after successful order
          clearCart();
          // Navigate to confirmation with order details
          navigate("/confirmation", {
            state: {
              order: orderData,
              orderNumber: data.orderNumber,
              orderID: data.orderID,
            },
          });
        })
        .catch((err) => {
          // Handle error: show a message or log
          console.error("Error saving order:", err);
          alert("Sorry, there was an error processing your order. Please try again.");
        });
    }
  };

  return (
    <div className="checkout-container">
      <div className="checkout-layout">
        {/* Left Side - Forms */}
        <div className="checkout-left">
          <h1>Checkout</h1>

          <form onSubmit={handleSubmit}>
            {/* Contact Information Section */}
            <section className="form-section">
              <h2>Contact Information</h2>

              <div className="form-group">
                <label htmlFor="email">Email *</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className={errors.email ? "error" : ""}
                  placeholder="you@example.com"
                />
                {errors.email && (
                  <span className="error-message">{errors.email}</span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="phone">Phone Number *</label>
                <input
                  type="tel"
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  className={errors.phone ? "error" : ""}
                  placeholder="(123) 456-7890"
                />
                {errors.phone && (
                  <span className="error-message">{errors.phone}</span>
                )}
              </div>
            </section>

            {/* Delivery/Address Section */}
            <section className="form-section">
              <h2>Delivery Address</h2>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="firstName">First Name *</label>
                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    className={errors.firstName ? "error" : ""}
                  />
                  {errors.firstName && (
                    <span className="error-message">{errors.firstName}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="lastName">Last Name *</label>
                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    className={errors.lastName ? "error" : ""}
                  />
                  {errors.lastName && (
                    <span className="error-message">{errors.lastName}</span>
                  )}
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="address">Address *</label>
                <input
                  type="text"
                  id="address"
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  className={errors.address ? "error" : ""}
                  placeholder="Street address"
                />
                {errors.address && (
                  <span className="error-message">{errors.address}</span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="apartment">
                  Apartment, suite, etc. (optional)
                </label>
                <input
                  type="text"
                  id="apartment"
                  name="apartment"
                  value={formData.apartment}
                  onChange={handleInputChange}
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="city">City *</label>
                  <input
                    type="text"
                    id="city"
                    name="city"
                    value={formData.city}
                    onChange={handleInputChange}
                    className={errors.city ? "error" : ""}
                  />
                  {errors.city && (
                    <span className="error-message">{errors.city}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="state">
                    {formData.country &&
                    formData.country.toLowerCase().includes("canada")
                      ? "Province"
                      : formData.country &&
                        formData.country.toLowerCase().includes("australia")
                      ? "State/Territory"
                      : "State/Province"}{" "}
                    *
                  </label>
                  <div className="input-with-dropdown">
                    <input
                      type="text"
                      id="state"
                      name="state"
                      value={formData.state}
                      onChange={handleInputChange}
                      className={errors.state ? "error" : ""}
                      placeholder={
                        formData.country &&
                        formData.country.toLowerCase().includes("canada")
                          ? "e.g., ON, BC, QC"
                          : formData.country &&
                            formData.country.toLowerCase().includes("mexico")
                          ? "e.g., JAL, NLE, CDMX"
                          : formData.country &&
                            formData.country.toLowerCase().includes("australia")
                          ? "e.g., NSW, VIC, QLD"
                          : "e.g., CA, NY, TX"
                      }
                      list="state-options"
                    />
                    <datalist id="state-options">
                      {getStatesForCountry(formData.country).map((state) => (
                        <option key={state.code} value={state.code}>
                          {state.name}
                        </option>
                      ))}
                    </datalist>
                  </div>
                  {errors.state && (
                    <span className="error-message">{errors.state}</span>
                  )}
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="zipCode">ZIP Code *</label>
                  <input
                    type="text"
                    id="zipCode"
                    name="zipCode"
                    value={formData.zipCode}
                    onChange={handleInputChange}
                    className={errors.zipCode ? "error" : ""}
                    placeholder="12345 or 12345-6789"
                  />
                  {errors.zipCode && (
                    <span className="error-message">{errors.zipCode}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="country">Country *</label>
                  <div className="input-with-dropdown">
                    <input
                      type="text"
                      id="country"
                      name="country"
                      value={formData.country}
                      onChange={handleInputChange}
                      className={errors.country ? "error" : ""}
                      placeholder="e.g., United States"
                      list="country-options"
                    />
                    <datalist id="country-options">
                      {countries.map((country) => (
                        <option key={country.code} value={country.name}>
                          {country.name}
                        </option>
                      ))}
                    </datalist>
                  </div>
                  {errors.country && (
                    <span className="error-message">{errors.country}</span>
                  )}
                </div>
              </div>
            </section>

            {/* Payment Section */}
            <section className="form-section">
              <h2>Payment Information</h2>

              <div className="form-group">
                <label htmlFor="cardNumber">Card Number *</label>
                <div className="card-input-container">
                  <input
                    type="text"
                    id="cardNumber"
                    name="cardNumber"
                    value={formData.cardNumber}
                    onChange={handleInputChange}
                    className={errors.cardNumber ? "error" : ""}
                    placeholder="1234 5678 9012 3456"
                    maxLength="19"
                  />
                  {formData.cardNumber && (
                    <div className="card-type-indicator">
                      {getCardType(formData.cardNumber)}
                    </div>
                  )}
                </div>
                {errors.cardNumber && (
                  <span className="error-message">{errors.cardNumber}</span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="cardholderName">Cardholder Name *</label>
                <input
                  type="text"
                  id="cardholderName"
                  name="cardholderName"
                  value={formData.cardholderName}
                  onChange={handleInputChange}
                  className={errors.cardholderName ? "error" : ""}
                  placeholder="Name on card"
                />
                {errors.cardholderName && (
                  <span className="error-message">{errors.cardholderName}</span>
                )}
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="expiryDate">Expiry Date *</label>
                  <input
                    type="text"
                    id="expiryDate"
                    name="expiryDate"
                    value={formData.expiryDate}
                    onChange={handleInputChange}
                    className={errors.expiryDate ? "error" : ""}
                    placeholder="MM/YY"
                    maxLength="5"
                  />
                  {errors.expiryDate && (
                    <span className="error-message">{errors.expiryDate}</span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="cvv">CVV *</label>
                  <input
                    type="text"
                    id="cvv"
                    name="cvv"
                    value={formData.cvv}
                    onChange={handleInputChange}
                    className={errors.cvv ? "error" : ""}
                    placeholder="123"
                    maxLength="4"
                  />
                  {errors.cvv && (
                    <span className="error-message">{errors.cvv}</span>
                  )}
                </div>
              </div>
            </section>

            <button type="submit" className="place-order-btn">
              Place Order - ${getCartTotal().toFixed(2)}
            </button>
          </form>
        </div>

        {/* Right Side - Order Summary */}
        <div className="checkout-right">
          <div className="order-summary">
            <h2>Order Summary</h2>

            <div className="cart-items-summary">
              {cartItems.map((item) => (
                <div key={item.id} className="summary-item">
                  <div className="item-image">
                    <img src={item.image} alt={item.name} />
                    <span className="item-quantity">{item.quantity}</span>
                  </div>
                  <div className="item-details">
                    <h3>{item.name}</h3>
                    <p className="item-price">
                      ${(item.price * item.quantity).toFixed(2)}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            <div className="summary-divider"></div>

            <div className="summary-totals">
              <div className="summary-row">
                <span>Subtotal</span>
                <span>${getCartTotal().toFixed(2)}</span>
              </div>
              <div className="summary-row">
                <span>Shipping</span>
                <span>FREE</span>
              </div>
              <div className="summary-row total-row">
                <strong>Total</strong>
                <strong>${getCartTotal().toFixed(2)}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckoutPage;

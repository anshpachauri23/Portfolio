import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import '../styles/ConfirmationPage.css';

const ConfirmationPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // Get order data from navigation state
  const { order, orderNumber, orderID } = location.state || {};
  
  // If no order data, redirect to checkout
  if (!order) {
    navigate('/');
    return null;
  }

  const { items, customerInfo, total } = order;

  return (
    <div className="confirmation-container">
      <div className="confirmation-content">
        
        {/* Success Header */}
        <div className="success-header">
          <div className="success-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h1>Order Confirmed!</h1>
          <p className="success-message">
            Thank you for your order. We've received your payment and will process your order shortly.
          </p>
        </div>

        {/* Order Details */}
        <div className="order-details">
          <div className="order-info">
            <h2>Order Information</h2>
            <div className="info-grid">
              <div className="info-item">
                <span className="label">Order Number:</span>
                <span className="value order-number">{orderNumber}</span>
              </div>
              <div className="info-item">
                <span className="label">Order Date:</span>
                <span className="value">{new Date(order.orderDate).toLocaleDateString()}</span>
              </div>
              <div className="info-item">
                <span className="label">Total Amount:</span>
                <span className="value total-amount">${total.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Customer Information */}
          <div className="customer-info">
            <h3>Customer Information</h3>
            <div className="customer-details">
              <p><strong>Name:</strong> {customerInfo.firstName} {customerInfo.lastName}</p>
              <p><strong>Email:</strong> {customerInfo.email}</p>
              <p><strong>Phone:</strong> {customerInfo.phone}</p>
              <p><strong>Address:</strong> {customerInfo.address}</p>
              {customerInfo.apartment && <p><strong>Apt/Suite:</strong> {customerInfo.apartment}</p>}
              <p><strong>City:</strong> {customerInfo.city}</p>
              <p><strong>ZIP Code:</strong> {customerInfo.zipCode}</p>
            </div>
          </div>

          {/* Order Items */}
          <div className="order-items">
            <h3>Order Items</h3>
            <div className="items-list">
              {items && items.length > 0 ? (
                items.map((item, index) => (
                  <div key={index} className="item-row">
                    <div className="item-info">
                      <span className="item-name">{item.name}</span>
                      <span className="item-quantity">Qty: {item.quantity}</span>
                    </div>
                    <div className="item-price">
                      ${(item.price * item.quantity).toFixed(2)}
                    </div>
                  </div>
                ))
              ) : (
                <p className="no-items">No items in this order.</p>
              )}
            </div>
            
            <div className="order-total">
              <div className="total-row">
                <span>Subtotal:</span>
                <span>${total.toFixed(2)}</span>
              </div>
              <div className="total-row">
                <span>Shipping:</span>
                <span>FREE</span>
              </div>
              <div className="total-row final-total">
                <span>Total:</span>
                <span>${total.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="action-buttons">
          <button 
            className="btn-secondary" 
            onClick={() => navigate('/')}
          >
            Continue Shopping
          </button>
          <button 
            className="btn-primary" 
            onClick={() => window.print()}
          >
            Print Receipt
          </button>
        </div>

        {/* Additional Info */}
        <div className="additional-info">
          <h4>What's Next?</h4>
          <ul>
            <li>You'll receive an email confirmation shortly</li>
            <li>We'll send you tracking information once your order ships</li>
            <li>Expected delivery: 3-5 business days</li>
            <li>Questions? Contact us at support@jear.com</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationPage;

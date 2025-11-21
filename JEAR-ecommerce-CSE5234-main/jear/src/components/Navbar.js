// src/components/Navbar.js
import React from "react";
import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";

function Navbar() {
  const { cartItems } = useCart();
  const cartItemCount = cartItems.reduce(
    (total, item) => total + item.quantity,
    0
  );

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm modern-navbar">
      <div className="container-fluid">
        <div className="d-flex w-100 justify-content-between align-items-center">
          {/* Left Side - Brand and Navigation Links */}
          <div className="d-flex align-items-center">
            <Link className="navbar-brand fw-bold fs-3 brand-logo me-4" to="/">
              JEAR
            </Link>

            {/* Desktop Navigation - Right after logo */}
            <div className="d-flex align-items-center">
              <Link className="nav-link modern-nav-link me-3" to="/">
                <i className="bi bi-house-door me-1"></i> Home
              </Link>
              <Link className="nav-link modern-nav-link me-3" to="/aboutUs">
                <i className="bi bi-info-circle me-1"></i> About Us
              </Link>
              <div className="nav-item dropdown me-3">
                <button
                  className="nav-link dropdown-toggle modern-nav-link btn btn-link"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                >
                  <i className="bi bi-grid me-1"></i> Products
                </button>
                <ul className="dropdown-menu modern-dropdown">
                  <li>
                    <Link className="dropdown-item modern-dropdown-item" to="/">
                      <i className="bi bi-hoodie me-2"></i> Hoodies
                    </Link>
                  </li>
                  <li>
                    <hr className="dropdown-divider" />
                  </li>
                  <li>
                    <Link className="dropdown-item modern-dropdown-item" to="/">
                      <i className="bi bi-shirt me-2"></i> Shirts
                    </Link>
                  </li>
                </ul>
              </div>
              <Link className="nav-link modern-nav-link" to="/contact">
                <i className="bi bi-envelope me-1"></i> Contact
              </Link>
            </div>
          </div>

          {/* Right Side - Cart Button */}
          <div className="d-flex align-items-center">
            <Link
              to="/cart"
              className="btn btn-primary modern-cart-btn position-relative"
            >
              <i className="bi bi-cart3 me-1"></i> Cart
              {cartItemCount > 0 && (
                <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger modern-badge">
                  {cartItemCount}
                  <span className="visually-hidden">items in cart</span>
                </span>
              )}
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
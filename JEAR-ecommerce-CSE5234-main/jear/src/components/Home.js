import React from "react";
import ProductGrid from "./ProductGrid";
import "../styles/Home.css";

function Home() {
  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section bg-dark text-white py-5 mb-5">
        <div className="container">
          <div className="row justify-content-center">
            <div className="col-lg-8 text-center">
              <h1 className="display-4 fw-bold mb-3">JEAR Clothing</h1>
              <p className="lead">
                Premium hoodies and streetwear for your style.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Product Grid */}
      <ProductGrid />
    </div>
  );
}

export default Home;

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { API_ENDPOINTS, apiCall } from "../config/api";

const ProductCard = ({ product }) => (
  <div className="col-lg-4 col-md-6 mb-4">
    <Link to={`/purchase/${product.productId}`} className="text-decoration-none text-dark">
      <div className="card h-100 shadow-sm product-card">
        <div className="card-img-container">
          <img 
            src={product.image} 
            className="card-img-top" 
            alt={product.name}
            style={{ height: '300px', objectFit: 'cover' }}
          />
        </div>
        <div className="card-body d-flex flex-column">
          <h5 className="card-title">{product.name}</h5>
          <p className="card-text text-muted">{product.description || 'Premium Quality Hoodie'}</p>
          <div className="mt-auto">
            <p className="card-text fw-bold text-primary fs-4">
              ${product.price?.toFixed(2) || 'N/A'}
            </p>
          </div>
        </div>
      </div>
    </Link>
  </div>
);

const ProductGrid = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const data = await apiCall(API_ENDPOINTS.INVENTORY);
        setProducts(data.products || data);
      } catch (err) {
        console.error('Error fetching products:', err);
        setError('Failed to load products.');
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <p>Loading products...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container py-5 text-center">
        <p className="text-danger">{error}</p>
      </div>
    );
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center mb-5">
        <div className="col-lg-8 text-center">
          <h2 className="display-5 fw-bold mb-3">Featured Sweatshirts</h2>
          <p className="lead text-muted">Discover our premium collection of streetwear</p>
        </div>
      </div>
      <div className="row">
        {products.length > 0 ? (
          products.map(product => <ProductCard key={product.id} product={product} />)
        ) : (
          <div className="text-center">
            <p>No products available.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductGrid;
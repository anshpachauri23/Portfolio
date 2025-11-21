import React from 'react';
import { Link } from 'react-router-dom';

const products = [
  { id: 1, name: 'Ash', price: 29.99, image: `${process.env.PUBLIC_URL}/ash.jpeg` },
  { id: 2, name: 'Earth', price: 29.99, image: `${process.env.PUBLIC_URL}/earth.png` },
  { id: 3, name: 'Forest', price: 29.99, image: `${process.env.PUBLIC_URL}/forest.png` },
  { id: 4, name: 'Ocean', price: 29.99, image: `${process.env.PUBLIC_URL}/ocean.png` },
  { id: 5, name: 'Midnight', price: 29.99, image: `${process.env.PUBLIC_URL}/midnight.png` },
];

const ProductCard = ({ product }) => (
  <div className="col-lg-4 col-md-6 mb-4">
    <Link to={`/purchase${product.id}`} className="text-decoration-none text-dark">
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
          <p className="card-text text-muted">Premium Quality Hoodie</p>
          <div className="mt-auto">
            <p className="card-text fw-bold text-primary fs-4">${product.price}</p>
          </div>
        </div>
      </div>
    </Link>
  </div>
);

const ProductGrid = () => (
  <div className="container py-5">
    <div className="row justify-content-center mb-5">
      <div className="col-lg-8 text-center">
        <h2 className="display-5 fw-bold mb-3">Featured Sweatshirts</h2>
        <p className="lead text-muted">Discover our premium collection of streetwear</p>
      </div>
    </div>
    <div className="row">
      {products.map(product => <ProductCard key={product.id} product={product} />)}
    </div>
  </div>
);

export default ProductGrid;

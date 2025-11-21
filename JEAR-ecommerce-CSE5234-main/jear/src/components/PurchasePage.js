import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import "../styles/purchase.css";
import { useCart } from "../context/CartContext";
import { API_ENDPOINTS, apiCall } from "../config/api";

const PurchasePage = () => {
  const { productId } = useParams(); 
  const { addToCart } = useCart();

  const [product, setProduct] = useState(null);
  const [selectedSize, setSelectedSize] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const data = await apiCall(API_ENDPOINTS.INVENTORY, { method: "GET" });
        const items = data.products || [];
        const found = items.find(
          (item) => item.productId.trim().toUpperCase() === productId.toUpperCase()
        );

        if (found) {
          setProduct(found);
          const availableSizes = Object.values(found.sizes || {}).filter(
            (s) => s.available && s.quantity > 0
          );
          if (availableSizes.length > 0) {
            setSelectedSize(availableSizes[0].size);
          }
        } else {
          console.error("Product not found in inventory");
        }
      } catch (err) {
        console.error("Error fetching inventory:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [productId]);

  if (loading) return <div>Loading product...</div>;
  if (!product) return <div>Product not found.</div>;

  const handleAddToCart = () => {
    if (!product || !selectedSize) return;

    addToCart({
      ...product,
      size: selectedSize,
      quantity,
    });

    setAdded(true);
    setTimeout(() => setAdded(false), 4000);
  };

  return (
    <div className="purchase-container">
      <div className="product-left">
        <img
          src={product.image}
          alt={product.name}
          className="product-image"
        />
      </div>

      <div className="product-right">
        <h1 className="product-title">{product.name}</h1>
        <p className="product-description">{product.description}</p>
        <div className="product-price">${product.price.toFixed(2)}</div>

        <div className="product-sizes">
          <h3>Size</h3>
          <div className="size-options">
            {Object.values(product.sizes || {})
              .sort((a, b) => {
                const sizeOrder = { XS: 0, S: 1, M: 2, L: 3, XL: 4 };
                return (sizeOrder[a.size] ?? 999) - (sizeOrder[b.size] ?? 999);
              })
              .map((s) => (
                <button
                  key={s.size}
                  className={`size-btn ${
                    selectedSize === s.size ? "selected" : ""
                  }`}
                  onClick={() => setSelectedSize(s.size)}
                  disabled={!s.available || s.quantity <= 0}
                >
                  {s.size}
                  {!s.available || s.quantity <= 0 ? " (Out of stock)" : ""}
                </button>
              ))}
          </div>
        </div>

        <div className="quantity-selector">
          <button onClick={() => setQuantity(Math.max(1, quantity - 1))}>-</button>
          <span>{quantity}</span>
          <button onClick={() => setQuantity(quantity + 1)}>+</button>
        </div>

        <button
          className="add-to-cart-btn"
          onClick={handleAddToCart}
          disabled={!selectedSize}
        >
          ADD TO CART
        </button>

        {added && (
          <div className="text-success mt-2">
            {quantity} {product.name} ({selectedSize}) added to cart!
          </div>
        )}
      </div>
    </div>
  );
};

export default PurchasePage;

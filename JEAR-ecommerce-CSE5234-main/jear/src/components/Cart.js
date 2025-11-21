import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";

function Cart() {
  const navigate = useNavigate();
  const { cartItems, updateQuantity, removeFromCart, getCartTotal } = useCart();

  const handleCheckout = () => {
    if (cartItems.length === 0) {
      alert("Your cart is empty!");
    } else {
      navigate("/checkout");
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-lg-10">
          <div className="modern-card p-4">
            <h2
              className="text-center mb-4"
              style={{ color: "var(--text-primary)", fontWeight: "700" }}
            >
              Your Cart
            </h2>

            {cartItems.length === 0 ? (
              <div className="text-center py-5">
                <p className="lead" style={{ color: "var(--text-secondary)" }}>
                  Your cart is empty.
                </p>
                <Link to="/" className="btn btn-primary">
                  Shop now
                </Link>
              </div>
            ) : (
              <>
                <div className="table-responsive">
                  <table className="table align-middle">
                    <thead>
                      <tr style={{ borderBottom: "2px solid var(--border-color)" }}>
                        <th style={{ color: "var(--text-primary)", fontWeight: "600" }}>
                          Product
                        </th>
                        <th style={{ color: "var(--text-primary)", fontWeight: "600" }}>
                          Size
                        </th>
                        <th style={{ color: "var(--text-primary)", fontWeight: "600" }}>
                          Price
                        </th>
                        <th style={{ color: "var(--text-primary)", fontWeight: "600" }}>
                          Quantity
                        </th>
                        <th style={{ color: "var(--text-primary)", fontWeight: "600" }}>
                          Subtotal
                        </th>
                        <th></th>
                      </tr>
                    </thead>

                    <tbody>
                      {cartItems.map((item) => (
                        <tr
                          key={`${item.id}-${item.size}`}
                          style={{ borderBottom: "1px solid var(--border-light)" }}
                        >
                          <td>
                            <div className="d-flex align-items-center">
                              <img
                                src={item.image}
                                alt={item.name}
                                style={{
                                  width: "60px",
                                  height: "60px",
                                  objectFit: "cover",
                                  marginRight: "15px",
                                  borderRadius: "var(--radius-md)",
                                  boxShadow: "var(--shadow-light)",
                                }}
                              />
                              <span
                                style={{
                                  color: "var(--text-primary)",
                                  fontWeight: "500",
                                }}
                              >
                                {item.name}
                              </span>
                            </div>
                          </td>

                          <td style={{ color: "var(--text-secondary)" }}>{item.size}</td>
                          <td
                            style={{
                              color: "var(--primary-color)",
                              fontWeight: "600",
                            }}
                          >
                            ${item.price}
                          </td>

                          <td>
                            <div className="d-flex align-items-center">
                              <button
                                className="btn btn-sm btn-outline-primary me-2"
                                onClick={() =>
                                  updateQuantity(item.id, item.size, item.quantity - 1)
                                }
                                style={{ borderRadius: "var(--radius-sm)" }}
                                disabled={item.quantity <= 1}
                              >
                                -
                              </button>

                              <span
                                style={{
                                  color: "var(--text-primary)",
                                  fontWeight: "600",
                                  minWidth: "30px",
                                  textAlign: "center",
                                }}
                              >
                                {item.quantity}
                              </span>

                              <button
                                className="btn btn-sm btn-outline-primary ms-2"
                                onClick={() =>
                                  updateQuantity(item.id, item.size, item.quantity + 1)
                                }
                                style={{ borderRadius: "var(--radius-sm)" }}
                              >
                                +
                              </button>
                            </div>
                          </td>

                          <td
                            style={{
                              color: "var(--primary-color)",
                              fontWeight: "600",
                            }}
                          >
                            ${(item.price * item.quantity).toFixed(2)}
                          </td>

                          <td>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => removeFromCart(item.id, item.size)}
                              style={{ borderRadius: "var(--radius-sm)" }}
                            >
                              Remove
                            </button>
                          </td>
                        </tr>
                      ))}

                      <tr style={{ borderTop: "2px solid var(--border-color)" }}>
                        <td
                          colSpan="4"
                          className="text-end fw-bold"
                          style={{ color: "var(--text-primary)", fontSize: "1.1rem" }}
                        >
                          Total:
                        </td>
                        <td
                          className="fw-bold"
                          style={{
                            color: "var(--primary-color)",
                            fontSize: "1.2rem",
                          }}
                        >
                          ${getCartTotal().toFixed(2)}
                        </td>
                        <td></td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div className="text-end mt-4">
                  <button
                    className="btn btn-primary btn-lg"
                    onClick={handleCheckout}
                    style={{
                      borderRadius: "var(--radius-md)",
                      padding: "12px 30px",
                    }}
                  >
                    Checkout
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Cart;

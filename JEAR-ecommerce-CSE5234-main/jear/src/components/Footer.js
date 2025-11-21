import { Link } from "react-router-dom";
import { Facebook, Instagram, Twitter } from "react-bootstrap-icons"; 

function Footer() {
  return (
    <footer className="bg-dark text-light pt-5 pb-3 mt-auto">
      <div className="container">
        <div className="row gy-4 text-center"> {/* Center all column content */}
          
          <div className="col-md-4">
            <h5 className="fw-bold mb-3 text-uppercase">JEAR Clothing</h5>
            <p className="text-secondary small">
              Your destination for premium streetwear — crafted for comfort,
              designed for confidence. Shop our exclusive collection today.
            </p>
          </div>

          <div className="col-md-4">
            <h6 className="fw-bold mb-3 text-uppercase">Quick Links</h6>
            <ul className="list-unstyled">
              <li>
                <Link to="/home" className="text-light text-decoration-none">
                  Home
                </Link>
              </li>
              <li>
                <Link to="/home" className="text-light text-decoration-none">
                  Products
                </Link>
              </li>
              <li>
                <Link to="/aboutUs" className="text-light text-decoration-none">
                  About Us
                </Link>
              </li>
              <li>
                <Link to="/contact" className="text-light text-decoration-none">
                  Contact
                </Link>
              </li>
            </ul>
          </div>

          <div className="col-md-4">
            <h6 className="fw-bold mb-3 text-uppercase">Stay Connected</h6>
            <p className="text-secondary small mb-2">
              <strong>Email:</strong> support@jearclothing.com
            </p>
            <p className="text-secondary small mb-3">
              <strong>Phone:</strong> (123) 456-7890
            </p>
            <div className="d-flex justify-content-center gap-3">
              <a
                href="https://instagram.com"
                target="_blank"
                rel="noopener noreferrer"
                className="text-light fs-5"
              >
                <Instagram />
              </a>
              <a
                href="https://facebook.com"
                target="_blank"
                rel="noopener noreferrer"
                className="text-light fs-5"
              >
                <Facebook />
              </a>
              <a
                href="https://twitter.com"
                target="_blank"
                rel="noopener noreferrer"
                className="text-light fs-5"
              >
                <Twitter />
              </a>
            </div>
          </div>
        </div>

        <hr className="border-secondary my-4" />

        <div className="text-center small text-secondary">
          © {new Date().getFullYear()} JEAR Clothing. All rights reserved.
        </div>
      </div>
    </footer>
  );
}

export default Footer;
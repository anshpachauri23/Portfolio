import React, { useState } from 'react';
import '../styles/ContactPage.css';
import { API_ENDPOINTS, apiCall } from '../config/api';

const ContactPage = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: ''
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      await apiCall(API_ENDPOINTS.CONTACT, {
        method: 'POST',
        body: JSON.stringify(formData),
      });

      alert('Thank you for your message! We\'ll get back to you soon.');
      setFormData({ name: '', email: '', subject: '', message: '' });
    } catch (error) {
      console.error('Error submitting form:', error);
      alert('Sorry, there was an error sending your message. Please try again.');
    }
  };

  return (
    <div className="contact-page">
      {/* Hero Section */}
      <section className="contact-hero text-center">
        <div className="container">
          <h1 className="display-4 fw-bold mb-3">Get in Touch</h1>
          <p className="lead">We'd love to hear from you! Send us a message.</p>
        </div>
      </section>

      {/* Contact Content */}
      <section className="contact-content py-5">
        <div className="container">
          <div className="row">
            <div className="col-lg-7 mb-4 mb-lg-0">
              <div className="contact-form-container">
                <h3 className="mb-4">Send Us a Message</h3>
                <form className="contact-form" onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label htmlFor="name" className="form-label">Your Name</label>
                    <input
                      type="text"
                      className="form-control"
                      id="name"
                      name="name"
                      value={formData.name}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="email" className="form-label">Your Email</label>
                    <input
                      type="email"
                      className="form-control"
                      id="email"
                      name="email"
                      value={formData.email}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="subject" className="form-label">Subject</label>
                    <input
                      type="text"
                      className="form-control"
                      id="subject"
                      name="subject"
                      value={formData.subject}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label htmlFor="message" className="form-label">Message</label>
                    <textarea
                      className="form-control"
                      id="message"
                      name="message"
                      rows="5"
                      value={formData.message}
                      onChange={handleChange}
                      required
                    ></textarea>
                  </div>
                  <button type="submit" className="btn btn-primary w-100">Send Message</button>
                </form>
              </div>
            </div>
            <div className="col-lg-5">
              <div className="contact-info">
                <h3 className="mb-4">Contact Information</h3>
                <div className="contact-item">
                  <div className="contact-icon">
                    <i className="bi bi-geo-alt-fill"></i>
                  </div>
                  <div className="contact-details">
                    <h5>Our Office</h5>
                    <p>123 Main Street, Anytown, USA 12345</p>
                  </div>
                </div>
                <div className="contact-item">
                  <div className="contact-icon">
                    <i className="bi bi-envelope-fill"></i>
                  </div>
                  <div className="contact-details">
                    <h5>Email Us</h5>
                    <p>info@jearclothing.com</p>
                  </div>
                </div>
                <div className="contact-item">
                  <div className="contact-icon">
                    <i className="bi bi-phone-fill"></i>
                  </div>
                  <div className="contact-details">
                    <h5>Call Us</h5>
                    <p>+1 (555) 123-4567</p>
                  </div>
                </div>
                <div className="social-links mt-4">
                  <h5>Follow Us</h5>
                  <div className="d-flex gap-3">
                    <a href="https://facebook.com" target="_blank" rel="noopener noreferrer" className="social-link"><i className="bi bi-facebook"></i></a>
                    <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" className="social-link"><i className="bi bi-twitter"></i></a>
                    <a href="https://instagram.com" target="_blank" rel="noopener noreferrer" className="social-link"><i className="bi bi-instagram"></i></a>
                    <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" className="social-link"><i className="bi bi-linkedin"></i></a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="faq-section py-5">
        <div className="container">
          <h2 className="text-center fw-bold mb-5">Frequently Asked Questions</h2>
          <div className="accordion" id="faqAccordion">
            <div className="accordion-item">
              <h2 className="accordion-header" id="headingOne">
                <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseOne" aria-expanded="false" aria-controls="collapseOne">
                  What are your business hours?
                </button>
              </h2>
              <div id="collapseOne" className="accordion-collapse collapse" aria-labelledby="headingOne" data-bs-parent="#faqAccordion">
                <div className="accordion-body">
                  Our customer service is available Monday to Friday, 9 AM to 5 PM EST. You can reach us via email or phone during these hours.
                </div>
              </div>
            </div>
            <div className="accordion-item">
              <h2 className="accordion-header" id="headingTwo">
                <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
                  How can I track my order?
                </button>
              </h2>
              <div id="collapseTwo" className="accordion-collapse collapse" aria-labelledby="headingTwo" data-bs-parent="#faqAccordion">
                <div className="accordion-body">
                  Once your order is shipped, you will receive an email with a tracking number and a link to track your package.
                </div>
              </div>
            </div>
            <div className="accordion-item">
              <h2 className="accordion-header" id="headingThree">
                <button className="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
                  What is your return policy?
                </button>
              </h2>
              <div id="collapseThree" className="accordion-collapse collapse" aria-labelledby="headingThree" data-bs-parent="#faqAccordion">
                <div className="accordion-body">
                  We offer a 30-day return policy for all unworn and unwashed items. Please visit our Returns page for more details.
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default ContactPage;
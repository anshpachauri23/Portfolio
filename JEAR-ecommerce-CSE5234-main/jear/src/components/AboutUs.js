import React from "react";
import "../styles/AboutUs.css";

const AboutUs = () => {
  return (
    
    <div className="about-container">
        <div className="about-banner mb-5">
            <img
            src="/jearBanner.png"
            alt="JEAR Premium Hoodie"
            className="img-fluid w-100"
            />
        </div>

      <div className="about-header">
        <h1>About JEAR</h1>
        <p>Premium comfort. Effortless streetwear style. The hoodie reinvented.</p>
      </div>

      <div className="row mb-5">
        <div className="col-md-6 mb-4">
          <div className="about-section">
            <h3>Our Mission</h3>
            <p>
              At <strong>JEAR</strong>, our mission is to craft premium hoodies
              that merge luxury comfort with modern design. We aim to create
              apparel that makes people feel confident, connected, and
              effortlessly stylish — every day, everywhere.
            </p>
          </div>
        </div>
        <div className="col-md-6 mb-4">
          <div className="about-section">
            <h3>Our Vision</h3>
            <p>
              To redefine what a hoodie can be — not just a piece of clothing,
              but a statement of identity and quality. JEAR envisions a world
              where sustainability, comfort, and fashion coexist seamlessly.
            </p>
          </div>
        </div>
      </div>

      <div className="about-strategy">
        <h3>Our Strategy</h3>
        <p>
          Our strategy is simple — <strong>premium craftsmanship, ethical
          production, and timeless design.</strong> Every JEAR hoodie is made
          from carefully sourced materials designed to provide unmatched comfort
          and durability. We focus on quality over quantity, community over
          competition, and long-term value over short-term trends.
        </p>
      </div>

      <div className="executives-header">
        <h2>Meet Our Executives</h2>
        <p>
          The passionate minds driving JEAR’s vision for sustainable luxury.
        </p>
      </div>

      <div className="row g-4">
        <div className="col-md-4">
          <div className="card executive-card h-100">
            <img
              src="/reuben.JPEG"
              alt="CEO"
              className="card-img-top"
            />
            <div className="card-body text-center">
              <h5>Reuben Simiyu</h5>
              <p className="text-primary">Co-Founder & Chief Executive Officer</p>
              <p>
                Reuben co-founded JEAR with a simple idea — that a hoodie could be
                both elegant and comfortable. With a background in fashion
                innovation and years at luxury brands, Reuben leads JEAR with a
                vision to merge sustainability with streetwear sophistication.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card executive-card h-100">
            <img
              src="/emily.jpg"
              alt="COO"
              className="card-img-top"
            />
            <div className="card-body text-center">
              <h5>Emily Carter</h5>
              <p className="text-primary">Co-Founder & Chief Operations Officer</p>
              <p>
                Emily ensures every stitch meets the highest standards of
                quality and sustainability. With expertise in textile sourcing
                and global logistics, Emily brings precision and care to every
                JEAR product.
              </p>
            </div>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card executive-card h-100">
            <img
              src="/ansh.jpeg"
              alt="CMO"
              className="card-img-top"
            />
            <div className="card-body text-center">
              <h5>Ansh Pachauri</h5>
              <p className="text-primary">Co-Founder & Chief Marketing Officer</p>
              <p>
                Ansh blends creativity and data to elevate JEAR’s presence in
                the fashion world. With a passion for storytelling and branding,
                Ansh ensures that every campaign reflects JEAR’s premium
                quality and authentic voice.
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="about-closing">
        <h4>We are JEAR.</h4>
        <p>
          More than just hoodies — we’re a movement built on craftsmanship,
          confidence, and community. Every design is a promise: premium comfort
          with purpose.
        </p>
      </div>
    </div>
  );
};

export default AboutUs;

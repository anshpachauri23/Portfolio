import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { CartProvider } from './context/CartContext';
import CheckoutPage from './pages/CheckoutPage';
import ConfirmationPage from './pages/ConfirmationPage';
import ContactPage from './pages/ContactPage';
import Home from './components/Home';
import Cart from './components/Cart';
import Navbar from './components/Navbar';
import Purchase1 from './components/purchase1';
import Purchase2 from './components/purchase2';
import Purchase3 from './components/purchase3';
import Purchase4 from './components/purchase4';
import Purchase5 from './components/purchase5';
import AboutUs from './components/AboutUs';
import Footer from './components/Footer';

function App() {
  return (
    <CartProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/home" element={<Home />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/contact" element={<ContactPage />} />
          <Route path="/purchase1" element={<Purchase1 />} />
          <Route path="/purchase2" element={<Purchase2 />} />
          <Route path="/purchase3" element={<Purchase3 />} />
          <Route path="/purchase4" element={<Purchase4 />} />
          <Route path="/purchase5" element={<Purchase5 />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/confirmation" element={<ConfirmationPage />} />
          <Route path="/aboutUs" element={<AboutUs />} />
        </Routes>
        <Footer />
      </BrowserRouter>
    </CartProvider>
  );
}


export default App;

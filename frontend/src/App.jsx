import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import NotFound from "./pages/NotFound";

import GuestSearch from "./pages/GuestSearch";
import PropertyDetails from "./pages/PropertyDetails";
import MyBookings from "./pages/MyBookings";
import GuestProfile from "./pages/GuestProfile";

import OwnerDashboard from "./pages/OwnerDashboard";
import OwnerPropertyForm from "./pages/OwnerPropertyForm";
import OwnerPropertyDetail from "./pages/OwnerPropertyDetail";
import OwnerDiscountPlans from "./pages/OwnerDiscountPlans";
import OwnerBookings from "./pages/OwnerBookings";
import OwnerProfile from "./pages/OwnerProfile";

import AdminApprovals from "./pages/AdminApprovals";
import AdminLocations from "./pages/AdminLocations";
import AdminOverview from "./pages/AdminOverview";

import "./styles/global.css";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="app-shell">
          <Navbar />
          <main className="main-content">
            <Routes>
              {/* Public */}
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/search" element={<GuestSearch />} />
              <Route path="/property/:id" element={<PropertyDetails />} />

              {/* Guest */}
              <Route path="/my-bookings" element={
                <ProtectedRoute roles={["GUEST"]}><MyBookings /></ProtectedRoute>
              } />
              <Route path="/guest/profile" element={
                <ProtectedRoute roles={["GUEST"]}><GuestProfile /></ProtectedRoute>
              } />

              {/* Owner */}
              <Route path="/owner" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerDashboard /></ProtectedRoute>
              } />
              <Route path="/owner/properties/new" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerPropertyForm /></ProtectedRoute>
              } />
              <Route path="/owner/properties/:id/edit" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerPropertyForm /></ProtectedRoute>
              } />
              <Route path="/owner/properties/:id" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerPropertyDetail /></ProtectedRoute>
              } />
              <Route path="/owner/properties/:id/discount-plans" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerDiscountPlans /></ProtectedRoute>
              } />
              <Route path="/owner/bookings" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerBookings /></ProtectedRoute>
              } />
              <Route path="/owner/profile" element={
                <ProtectedRoute roles={["OWNER"]}><OwnerProfile /></ProtectedRoute>
              } />

              {/* Admin */}
              <Route path="/admin" element={
                <ProtectedRoute roles={["ADMIN"]}><AdminApprovals /></ProtectedRoute>
              } />
              <Route path="/admin/locations" element={
                <ProtectedRoute roles={["ADMIN"]}><AdminLocations /></ProtectedRoute>
              } />
              <Route path="/admin/overview" element={
                <ProtectedRoute roles={["ADMIN"]}><AdminOverview /></ProtectedRoute>
              } />

              <Route path="*" element={<NotFound />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

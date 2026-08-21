import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const roleHome = () => {
    if (!user) return "/";
    if (user.role === "ADMIN") return "/admin";
    if (user.role === "OWNER") return "/owner";
    return "/search";
  };

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <Link to={roleHome()} className="brand" onClick={() => setOpen(false)}>
          <span className="brand-mark">S</span>
          Staylist
        </Link>

        <button className="hamburger" onClick={() => setOpen((o) => !o)} aria-label="Toggle menu">
          ☰
        </button>

        <nav className={`nav-links ${open ? "open" : ""}`}>
          {!user && (
            <>
              <Link to="/search" onClick={() => setOpen(false)}>Find a PG</Link>
              <Link to="/login" onClick={() => setOpen(false)}>Log in</Link>
              <Link to="/register" onClick={() => setOpen(false)}>Sign up</Link>
            </>
          )}

          {user && user.role === "ADMIN" && (
            <>
              <Link to="/admin" onClick={() => setOpen(false)}>Approvals</Link>
              <Link to="/admin/locations" onClick={() => setOpen(false)}>Locations</Link>
              <Link to="/admin/overview" onClick={() => setOpen(false)}>Overview</Link>
            </>
          )}

          {user && user.role === "OWNER" && (
            <>
              <Link to="/owner" onClick={() => setOpen(false)}>My Properties</Link>
              <Link to="/owner/bookings" onClick={() => setOpen(false)}>Booking Requests</Link>
              <Link to="/owner/profile" onClick={() => setOpen(false)}>Profile</Link>
            </>
          )}

          {user && user.role === "GUEST" && (
            <>
              <Link to="/search" onClick={() => setOpen(false)}>Find a PG</Link>
              <Link to="/my-bookings" onClick={() => setOpen(false)}>My Bookings</Link>
              <Link to="/guest/profile" onClick={() => setOpen(false)}>Profile</Link>
            </>
          )}

          {user && (
            <>
              <span className="badge-role">{user.role}</span>
              <button className="link-btn" onClick={handleLogout}>Log out</button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

import React from "react";
import { Link } from "react-router-dom";

export default function Home() {
  return (
    <div className="container">
      <div style={{ textAlign: "center", padding: "60px 0 30px" }}>
        <span style={{ color: "var(--sage-500)", fontFamily: "var(--font-mono)", fontSize: "0.78rem", textTransform: "uppercase", letterSpacing: "0.08em" }}>
          Shared houses, sorted properly
        </span>
        <h1 style={{ fontSize: "2.6rem", marginTop: 10 }}>Find your next PG, minus the guesswork</h1>
        <p style={{ maxWidth: 560, margin: "0 auto 26px", fontSize: "1.05rem" }}>
          Every property on Staylist is checked by our team before it goes live, and every
          bed is confirmed by the owner before you pay a rupee.
        </p>
        <div style={{ display: "flex", gap: 12, justifyContent: "center", flexWrap: "wrap" }}>
          <Link to="/search" className="btn btn-accent">Browse PGs</Link>
          <Link to="/register" className="btn btn-outline">List your property</Link>
        </div>
      </div>

      <div className="grid grid-3" style={{ marginTop: 50 }}>
        <div className="card">
          <h3 style={{ fontSize: "1.05rem" }}>1. Owners list, we verify</h3>
          <p>Owners submit property + room details. Our admin team reviews the listing before it appears in search.</p>
        </div>
        <div className="card">
          <h3 style={{ fontSize: "1.05rem" }}>2. You book a specific bed</h3>
          <p>Search by name or location, pick a private room or a bed in a shared room, and send a request.</p>
        </div>
        <div className="card">
          <h3 style={{ fontSize: "1.05rem" }}>3. Owner confirms, you pay monthly</h3>
          <p>The owner checks your ID and approves the stay. Rent is then collected automatically each month via Razorpay.</p>
        </div>
      </div>
    </div>
  );
}

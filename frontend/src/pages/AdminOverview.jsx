import React, { useEffect, useState } from "react";
import { getAllPropertiesAdmin, getAllBookingsAdmin, getAllPaymentsAdmin } from "../api/endpoints";
import Loader from "../components/Loader";
import EmptyState from "../components/EmptyState";
import StatusBadge from "../components/StatusBadge";

export default function AdminOverview() {
  const [tab, setTab] = useState("properties");
  const [properties, setProperties] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getAllPropertiesAdmin(), getAllBookingsAdmin(), getAllPaymentsAdmin()])
      .then(([propRes, bookRes, payRes]) => {
        setProperties(propRes.data);
        setBookings(bookRes.data);
        setPayments(payRes.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Platform-wide</span>
        <h1>Overview</h1>
      </div>

      <div className="tabs">
        <button className={`tab ${tab === "properties" ? "active" : ""}`} onClick={() => setTab("properties")}>
          Properties ({properties.length})
        </button>
        <button className={`tab ${tab === "bookings" ? "active" : ""}`} onClick={() => setTab("bookings")}>
          Bookings ({bookings.length})
        </button>
        <button className={`tab ${tab === "payments" ? "active" : ""}`} onClick={() => setTab("payments")}>
          Payments ({payments.length})
        </button>
      </div>

      {tab === "properties" && (
        properties.length === 0 ? <EmptyState title="No properties yet" /> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Name</th><th>Owner</th><th>Location</th><th>Status</th></tr>
              </thead>
              <tbody>
                {properties.map((p) => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td>{p.owner?.name}</td>
                    <td>{p.location?.area}, {p.location?.city}</td>
                    <td><StatusBadge status={p.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      {tab === "bookings" && (
        bookings.length === 0 ? <EmptyState title="No bookings yet" /> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Guest</th><th>Bed</th><th>Property</th><th>Check-in</th><th>Status</th></tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id}>
                    <td>{b.guest?.name}</td>
                    <td>{b.bed?.label}</td>
                    <td>{b.bed?.room?.property?.name}</td>
                    <td>{b.checkInDate}</td>
                    <td><StatusBadge status={b.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      {tab === "payments" && (
        payments.length === 0 ? <EmptyState title="No payments yet" /> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Amount</th><th>Billing month</th><th>Status</th><th>Paid at</th></tr>
              </thead>
              <tbody>
                {payments.map((tx) => (
                  <tr key={tx.id}>
                    <td>₹{Number(tx.amount).toLocaleString("en-IN")}</td>
                    <td>{tx.billingMonth}</td>
                    <td><StatusBadge status={tx.status} /></td>
                    <td>{tx.paidAt || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}
    </div>
  );
}

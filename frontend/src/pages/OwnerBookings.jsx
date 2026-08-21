import React, { useEffect, useState } from "react";
import { getOwnerBookings, approveBooking, rejectBooking } from "../api/endpoints";
import Loader from "../components/Loader";
import EmptyState from "../components/EmptyState";
import StatusBadge from "../components/StatusBadge";
import Alert from "../components/Alert";

export default function OwnerBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [rejectingId, setRejectingId] = useState(null);
  const [reason, setReason] = useState("");
  const [busyId, setBusyId] = useState(null);

  const load = () => {
    setLoading(true);
    getOwnerBookings()
      .then((res) => setBookings(res.data))
      .catch(() => setError("Could not load booking requests."))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleApprove = async (id) => {
    setError("");
    setBusyId(id);
    try {
      await approveBooking(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not approve booking.");
    } finally {
      setBusyId(null);
    }
  };

  const handleReject = async (id) => {
    if (!reason.trim()) return;
    setError("");
    setBusyId(id);
    try {
      await rejectBooking(id, reason.trim());
      setRejectingId(null);
      setReason("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not reject booking.");
    } finally {
      setBusyId(null);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Guest requests</span>
        <h1>Booking Requests</h1>
        <p>Verify the guest's govt ID before approving a stay.</p>
      </div>

      <Alert type="error">{error}</Alert>

      {bookings.length === 0 ? (
        <EmptyState title="No booking requests yet" />
      ) : (
        <div className="grid grid-2">
          {bookings.map((b) => (
            <div key={b.id} className="card">
              <div className="section-title-row">
                <h3 style={{ margin: 0, fontSize: "1rem" }}>{b.guest?.name}</h3>
                <StatusBadge status={b.status} />
              </div>
              <p style={{ margin: "4px 0" }}>{b.guest?.email} · {b.guest?.phone}</p>
              <p style={{ margin: "4px 0" }}>
                <strong>Govt ID:</strong> {b.guest?.govtIdType} — {b.guest?.govtIdNumber}
                {b.guest?.govtIdDocUrl && (
                  <> (<a href={b.guest.govtIdDocUrl} target="_blank" rel="noreferrer">view document</a>)</>
                )}
              </p>
              <p style={{ margin: "4px 0" }}>
                <strong>Bed:</strong> {b.bed?.label} — ₹{Number(b.bed?.monthlyRent).toLocaleString("en-IN")}/month
              </p>
              <p style={{ margin: "4px 0" }}><strong>Check-in:</strong> {b.checkInDate}</p>
              <p style={{ margin: "4px 0" }}>
                <strong>Plan:</strong>{" "}
                {b.planDurationMonths
                  ? `${b.planDurationMonths} months · ${Number(b.discountPercent)}% off`
                  : "Month-to-month"}
              </p>

              {b.status === "REQUESTED" && (
                <div style={{ marginTop: 12 }}>
                  {rejectingId === b.id ? (
                    <div style={{ display: "flex", gap: 8 }}>
                      <input
                        placeholder="Reason for rejection"
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        style={{ flex: 1, padding: "8px 10px", border: "1px solid var(--sand-200)", borderRadius: 6 }}
                      />
                      <button className="btn btn-danger btn-sm" disabled={busyId === b.id} onClick={() => handleReject(b.id)}>
                        Confirm
                      </button>
                      <button className="btn btn-outline btn-sm" onClick={() => { setRejectingId(null); setReason(""); }}>
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <div style={{ display: "flex", gap: 8 }}>
                      <button className="btn btn-primary btn-sm" disabled={busyId === b.id} onClick={() => handleApprove(b.id)}>
                        {busyId === b.id ? "Approving..." : "Approve"}
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => setRejectingId(b.id)}>
                        Reject
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

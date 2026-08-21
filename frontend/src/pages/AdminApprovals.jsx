import React, { useEffect, useState } from "react";
import { getPendingProperties, approveProperty, rejectProperty } from "../api/endpoints";
import Loader from "../components/Loader";
import EmptyState from "../components/EmptyState";
import Alert from "../components/Alert";

export default function AdminApprovals() {
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [reason, setReason] = useState("");

  const load = () => {
    setLoading(true);
    getPendingProperties()
      .then((res) => setProperties(res.data))
      .catch(() => setError("Could not load pending properties."))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleApprove = async (id) => {
    setBusyId(id);
    setError("");
    try {
      await approveProperty(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not approve property.");
    } finally {
      setBusyId(null);
    }
  };

  const handleReject = async (id) => {
    if (!reason.trim()) return;
    setBusyId(id);
    setError("");
    try {
      await rejectProperty(id, reason.trim());
      setRejectingId(null);
      setReason("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not reject property.");
    } finally {
      setBusyId(null);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Review queue</span>
        <h1>Pending Property Approvals</h1>
      </div>

      <Alert type="error">{error}</Alert>

      {properties.length === 0 ? (
        <EmptyState title="Nothing pending" subtitle="All caught up — no property submissions waiting for review." />
      ) : (
        <div className="grid grid-2">
          {properties.map((p) => (
            <div key={p.id} className="card">
              <h3 style={{ marginBottom: 4 }}>{p.name}</h3>
              <p style={{ margin: "4px 0" }}>{p.location?.area}, {p.location?.city}</p>
              <p style={{ margin: "4px 0" }}><strong>Owner:</strong> {p.owner?.name} ({p.owner?.email})</p>
              <p style={{ margin: "4px 0" }}>
                <strong>Owner ID:</strong> {p.owner?.govtIdType || "not submitted"} {p.owner?.govtIdNumber}
                {p.owner?.govtIdDocUrl && (
                  <> (<a href={p.owner.govtIdDocUrl} target="_blank" rel="noreferrer">view document</a>)</>
                )}
              </p>
              <p style={{ margin: "8px 0" }}>{p.description}</p>
              {p.facilities?.length > 0 && (
                <div className="pill-row" style={{ marginBottom: 10 }}>
                  {p.facilities.map((f) => <span key={f.id} className="pill">{f.name}</span>)}
                </div>
              )}

              {rejectingId === p.id ? (
                <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
                  <input
                    placeholder="Reason for rejection"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    style={{ flex: 1, padding: "8px 10px", border: "1px solid var(--sand-200)", borderRadius: 6 }}
                  />
                  <button className="btn btn-danger btn-sm" disabled={busyId === p.id} onClick={() => handleReject(p.id)}>Confirm</button>
                  <button className="btn btn-outline btn-sm" onClick={() => { setRejectingId(null); setReason(""); }}>Cancel</button>
                </div>
              ) : (
                <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
                  <button className="btn btn-primary btn-sm" disabled={busyId === p.id} onClick={() => handleApprove(p.id)}>
                    {busyId === p.id ? "Approving..." : "Approve"}
                  </button>
                  <button className="btn btn-danger btn-sm" onClick={() => setRejectingId(p.id)}>Reject</button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

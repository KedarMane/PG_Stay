import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  getMyProperties, getDiscountPlans, createDiscountPlan,
  updateDiscountPlan, toggleDiscountPlan, deleteDiscountPlan,
} from "../api/endpoints";
import Loader from "../components/Loader";
import Alert from "../components/Alert";
import EmptyState from "../components/EmptyState";

const emptyForm = { durationMonths: "", discountPercent: "" };

export default function OwnerDiscountPlans() {
  const { id } = useParams(); // property id
  const [property, setProperty] = useState(null);
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null); // plan id currently being edited, or null for "add new"
  const [saving, setSaving] = useState(false);
  const [busyId, setBusyId] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      const [propsRes, plansRes] = await Promise.all([getMyProperties(), getDiscountPlans(id)]);
      setProperty(propsRes.data.find((p) => String(p.id) === String(id)));
      setPlans(plansRes.data);
    } catch (e) {
      setError("Could not load discount plans.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [id]);

  const handleFormChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const startEdit = (plan) => {
    setEditingId(plan.id);
    setForm({ durationMonths: String(plan.durationMonths), discountPercent: String(plan.discountPercent) });
    setError("");
    setSuccess("");
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const durationMonths = Number(form.durationMonths);
    const discountPercent = Number(form.discountPercent);
    if (!durationMonths || durationMonths < 1) {
      setError("Duration must be at least 1 month.");
      return;
    }
    if (discountPercent < 0 || discountPercent > 100 || Number.isNaN(discountPercent)) {
      setError("Discount must be between 0 and 100%.");
      return;
    }

    setSaving(true);
    try {
      const payload = { durationMonths, discountPercent };
      if (editingId) {
        await updateDiscountPlan(id, editingId, payload);
        setSuccess("Plan updated.");
      } else {
        await createDiscountPlan(id, payload);
        setSuccess("Plan added.");
      }
      cancelEdit();
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not save plan.");
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (planId) => {
    setBusyId(planId);
    setError("");
    try {
      await toggleDiscountPlan(id, planId);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update plan.");
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async (planId) => {
    setBusyId(planId);
    setError("");
    try {
      await deleteDiscountPlan(id, planId);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not delete plan.");
    } finally {
      setBusyId(null);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;
  if (!property) return <div className="container"><Alert type="error">Property not found.</Alert></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Pricing</span>
        <h1>Discount plans — {property.name}</h1>
        <p>
          Reward guests who commit to a longer stay upfront. Set a duration (in months) and a
          discount — the guest's rate is locked in the moment they book, so changing this later
          never affects anyone who already signed up.
        </p>
        <Link to={`/owner/properties/${id}`} className="btn btn-outline btn-sm" style={{ marginTop: 8 }}>
          ← Back to property
        </Link>
      </div>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <div className="grid grid-2" style={{ marginTop: 20, alignItems: "start" }}>
        <div className="card">
          <h3>{editingId ? "Edit plan" : "Add a plan"}</h3>
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label>Duration (months)</label>
              <input
                name="durationMonths" type="number" min="1" required
                value={form.durationMonths} onChange={handleFormChange}
                placeholder="e.g. 3"
              />
            </div>
            <div className="field">
              <label>Discount (%)</label>
              <input
                name="discountPercent" type="number" min="0" max="100" step="0.01" required
                value={form.discountPercent} onChange={handleFormChange}
                placeholder="e.g. 10"
              />
            </div>
            <div style={{ display: "flex", gap: 10 }}>
              <button className="btn btn-primary" type="submit" disabled={saving}>
                {saving ? "Saving..." : editingId ? "Save changes" : "Add plan"}
              </button>
              {editingId && (
                <button type="button" className="btn btn-outline" onClick={cancelEdit}>Cancel</button>
              )}
            </div>
          </form>
        </div>

        <div className="card">
          <h3>Current plans</h3>
          {plans.length === 0 ? (
            <EmptyState title="No plans yet" subtitle="Add one on the left — e.g. 3 months for 5% off." />
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {plans.map((p) => (
                <div
                  key={p.id}
                  style={{
                    display: "flex", justifyContent: "space-between", alignItems: "center",
                    padding: "10px 14px", background: "var(--sand-100)", borderRadius: "var(--radius-sm)",
                    opacity: p.active ? 1 : 0.55,
                  }}
                >
                  <div>
                    <strong>{p.durationMonths} months</strong> — {Number(p.discountPercent)}% off
                    <div style={{ fontSize: "0.78rem", color: "var(--ink-soft)" }}>
                      {p.active ? "Visible to guests" : "Hidden (inactive)"}
                    </div>
                  </div>
                  <div style={{ display: "flex", gap: 6 }}>
                    <button className="btn btn-outline btn-sm" onClick={() => startEdit(p)} disabled={busyId === p.id}>
                      Edit
                    </button>
                    <button className="btn btn-outline btn-sm" onClick={() => handleToggle(p.id)} disabled={busyId === p.id}>
                      {p.active ? "Deactivate" : "Activate"}
                    </button>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(p.id)} disabled={busyId === p.id}>
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

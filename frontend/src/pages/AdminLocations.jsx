import React, { useEffect, useState } from "react";
import { getLocations, createLocation, updateLocation, deactivateLocation } from "../api/endpoints";
import Loader from "../components/Loader";
import Alert from "../components/Alert";
import EmptyState from "../components/EmptyState";

export default function AdminLocations() {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [form, setForm] = useState({ city: "", area: "", pincode: "" });
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);

  const load = () => {
    setLoading(true);
    getLocations()
      .then((res) => setLocations(res.data))
      .catch(() => setError("Could not load locations."))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const resetForm = () => {
    setForm({ city: "", area: "", pincode: "" });
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSaving(true);
    try {
      if (editingId) {
        await updateLocation(editingId, form);
        setSuccess("Location updated.");
      } else {
        await createLocation(form);
        setSuccess("Location added.");
      }
      resetForm();
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not save location.");
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (loc) => {
    setEditingId(loc.id);
    setForm({ city: loc.city, area: loc.area, pincode: loc.pincode || "" });
  };

  const handleDeactivate = async (id) => {
    setError("");
    try {
      await deactivateLocation(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not deactivate location.");
    }
  };

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Master data</span>
        <h1>Locations</h1>
        <p>Owners can only list properties under locations you've added here.</p>
      </div>

      <div className="grid grid-2" style={{ alignItems: "start" }}>
        <div className="card">
          <h3>{editingId ? "Edit location" : "Add a location"}</h3>
          <Alert type="error">{error}</Alert>
          <Alert type="success">{success}</Alert>
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label>City</label>
              <input name="city" required value={form.city} onChange={handleChange} placeholder="Pune" />
            </div>
            <div className="field">
              <label>Area</label>
              <input name="area" required value={form.area} onChange={handleChange} placeholder="Kothrud" />
            </div>
            <div className="field">
              <label>Pincode</label>
              <input name="pincode" value={form.pincode} onChange={handleChange} placeholder="411038" />
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <button className="btn btn-primary" type="submit" disabled={saving}>
                {saving ? "Saving..." : editingId ? "Save changes" : "Add location"}
              </button>
              {editingId && (
                <button className="btn btn-outline" type="button" onClick={resetForm}>Cancel</button>
              )}
            </div>
          </form>
        </div>

        <div className="card">
          <h3>All locations</h3>
          {locations.length === 0 ? (
            <EmptyState title="No locations yet" />
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {locations.map((l) => (
                <div key={l.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "10px 12px", background: "var(--sand-100)", borderRadius: "var(--radius-sm)" }}>
                  <div>
                    <strong>{l.area}, {l.city}</strong>
                    <div style={{ fontSize: "0.8rem", color: "var(--ink-soft)" }}>{l.pincode} {!l.active && "— inactive"}</div>
                  </div>
                  <div style={{ display: "flex", gap: 6 }}>
                    <button className="btn btn-outline btn-sm" onClick={() => handleEdit(l)}>Edit</button>
                    {l.active && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleDeactivate(l.id)}>Deactivate</button>
                    )}
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

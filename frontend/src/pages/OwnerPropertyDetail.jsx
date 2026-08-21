import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  getMyProperties, getOwnerRooms, addRoom, addFacility, removeFacility,
} from "../api/endpoints";
import Loader from "../components/Loader";
import Alert from "../components/Alert";
import StatusBadge from "../components/StatusBadge";

export default function OwnerPropertyDetail() {
  const { id } = useParams();
  const [property, setProperty] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [newFacility, setNewFacility] = useState("");
  const [roomForm, setRoomForm] = useState({
    type: "PRIVATE", roomNumber: "", description: "", monthlyRent: "", beds: "",
  });
  const [showRoomForm, setShowRoomForm] = useState(false);
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const propsRes = await getMyProperties();
      const found = propsRes.data.find((p) => String(p.id) === String(id));
      setProperty(found);
      if (found && found.status === "APPROVED") {
        const roomsRes = await getOwnerRooms(id);
        setRooms(roomsRes.data);
      }
    } catch (e) {
      setError("Could not load property.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [id]);

  const handleAddFacility = async (e) => {
    e.preventDefault();
    if (!newFacility.trim()) return;
    try {
      await addFacility(id, { name: newFacility.trim() });
      setNewFacility("");
      setSuccess("Facility added.");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not add facility.");
    }
  };

  const handleRemoveFacility = async (facilityId) => {
    try {
      await removeFacility(id, facilityId);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not remove facility.");
    }
  };

  const handleRoomFormChange = (e) => setRoomForm({ ...roomForm, [e.target.name]: e.target.value });

  const handleAddRoom = async (e) => {
    e.preventDefault();
    setError("");
    setSaving(true);
    try {
      const payload = {
        type: roomForm.type,
        roomNumber: roomForm.roomNumber,
        description: roomForm.description,
      };
      if (roomForm.type === "PRIVATE") {
        payload.monthlyRent = Number(roomForm.monthlyRent);
      } else {
        // beds format: "Bed A:2000, Bed B:2000"
        payload.beds = roomForm.beds.split(",").map((entry) => {
          const [label, rent] = entry.split(":").map((s) => s.trim());
          return { label, monthlyRent: Number(rent) };
        }).filter((b) => b.label);
      }
      await addRoom(id, payload);
      setSuccess("Room added.");
      setShowRoomForm(false);
      setRoomForm({ type: "PRIVATE", roomNumber: "", description: "", monthlyRent: "", beds: "" });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not add room.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;
  if (!property) return <div className="container"><Alert type="error">Property not found.</Alert></div>;

  return (
    <div className="container">
      <div className="section-title-row">
        <div>
          <span className="eyebrow">Manage property</span>
          <h1 style={{ margin: 0 }}>{property.name}</h1>
          <div style={{ marginTop: 8 }}><StatusBadge status={property.status} /></div>
        </div>
        <Link to={`/owner/properties/${id}/edit`} className="btn btn-outline">Edit details</Link>
        <Link to={`/owner/properties/${id}/discount-plans`} className="btn btn-outline" style={{ marginLeft: 8 }}>
          Discount plans
        </Link>
      </div>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      {property.status === "REJECTED" && (
        <div className="alert alert-error">
          Rejected by admin: {property.rejectionReason}. Edit the details above and resubmit — it'll go back into the review queue.
        </div>
      )}
      {property.status === "PENDING" && (
        <div className="alert alert-success" style={{ background: "var(--sand-200)", color: "var(--ink)", borderColor: "var(--sand-200)" }}>
          Waiting on admin approval. Rooms can be added once this property is approved.
        </div>
      )}

      <div className="grid grid-2" style={{ marginTop: 20, alignItems: "start" }}>
        <div className="card">
          <h3>Facilities</h3>
          <div className="pill-row">
            {property.facilities?.map((f) => (
              <span key={f.id} className="pill" style={{ display: "flex", alignItems: "center", gap: 6 }}>
                {f.name}
                <button
                  onClick={() => handleRemoveFacility(f.id)}
                  style={{ background: "none", border: "none", cursor: "pointer", color: "var(--teal-700)", fontWeight: 700 }}
                  aria-label={`Remove ${f.name}`}
                >
                  ×
                </button>
              </span>
            ))}
          </div>
          <form onSubmit={handleAddFacility} style={{ display: "flex", gap: 8, marginTop: 14 }}>
            <input placeholder="e.g. Swimming Pool" value={newFacility} onChange={(e) => setNewFacility(e.target.value)} />
            <button className="btn btn-outline btn-sm" type="submit">Add</button>
          </form>
          <p className="field-hint">Facility changes go live immediately — no admin re-approval needed.</p>
        </div>

        <div className="card">
          <h3>Property info</h3>
          <p style={{ marginBottom: 4 }}><strong>Location:</strong> {property.location?.area}, {property.location?.city}</p>
          <p style={{ marginBottom: 4 }}><strong>Address:</strong> {property.address}</p>
          <p style={{ marginBottom: 4 }}><strong>Gender preference:</strong> {property.genderPreference}</p>
          <p style={{ margin: 0 }}>{property.description}</p>
        </div>
      </div>

      {property.status === "APPROVED" && (
        <>
          <div className="section-title-row" style={{ marginTop: 34 }}>
            <h2 style={{ margin: 0 }}>Rooms</h2>
            <button className="btn btn-accent" onClick={() => setShowRoomForm((s) => !s)}>
              {showRoomForm ? "Cancel" : "+ Add room"}
            </button>
          </div>

          {showRoomForm && (
            <div className="card form-card" style={{ maxWidth: 520, marginLeft: 0 }}>
              <form onSubmit={handleAddRoom}>
                <div className="field">
                  <label>Room type</label>
                  <select name="type" value={roomForm.type} onChange={handleRoomFormChange}>
                    <option value="PRIVATE">Private</option>
                    <option value="SHARED">Shared</option>
                  </select>
                </div>
                <div className="field">
                  <label>Room number / label</label>
                  <input name="roomNumber" value={roomForm.roomNumber} onChange={handleRoomFormChange} placeholder="e.g. 101" />
                </div>
                <div className="field">
                  <label>Description</label>
                  <input name="description" value={roomForm.description} onChange={handleRoomFormChange} />
                </div>
                {roomForm.type === "PRIVATE" ? (
                  <div className="field">
                    <label>Monthly rent (₹)</label>
                    <input name="monthlyRent" type="number" required value={roomForm.monthlyRent} onChange={handleRoomFormChange} />
                  </div>
                ) : (
                  <div className="field">
                    <label>Beds</label>
                    <input
                      name="beds"
                      required
                      value={roomForm.beds}
                      onChange={handleRoomFormChange}
                      placeholder="Bed A:2000, Bed B:2000, Bed C:1800"
                    />
                    <div className="field-hint">Format: label:rent, separated by commas. Each bed is booked individually.</div>
                  </div>
                )}
                <button className="btn btn-primary" type="submit" disabled={saving}>
                  {saving ? "Adding..." : "Add room"}
                </button>
              </form>
            </div>
          )}

          <div className="grid grid-2" style={{ marginTop: 16 }}>
            {rooms.map((room) => (
              <div key={room.id} className="card">
                <div className="section-title-row">
                  <h3 style={{ margin: 0, fontSize: "1rem" }}>
                    {room.type} {room.roomNumber ? `— ${room.roomNumber}` : ""}
                  </h3>
                </div>
                {room.beds.map((bed) => (
                  <div key={bed.id} style={{ display: "flex", justifyContent: "space-between", padding: "8px 0", borderBottom: "1px solid var(--sand-100)" }}>
                    <span>{bed.label} — ₹{Number(bed.monthlyRent).toLocaleString("en-IN")}</span>
                    <StatusBadge status={bed.status} />
                  </div>
                ))}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

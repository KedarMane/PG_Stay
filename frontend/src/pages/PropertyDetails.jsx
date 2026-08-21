import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getPropertyById, getPropertyRooms, getPublicDiscountPlans, createBooking } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Loader from "../components/Loader";
import Alert from "../components/Alert";

export default function PropertyDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [property, setProperty] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [plans, setPlans] = useState([]);
  const [activeImage, setActiveImage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selectedBed, setSelectedBed] = useState(null);
  const [checkInDate, setCheckInDate] = useState("");
  const [durationMonths, setDurationMonths] = useState(""); // "" = month-to-month
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [booking, setBooking] = useState(false);

  useEffect(() => {
    Promise.all([getPropertyById(id), getPropertyRooms(id), getPublicDiscountPlans(id)])
      .then(([propRes, roomsRes, plansRes]) => {
        setProperty(propRes.data);
        setRooms(roomsRes.data);
        setPlans(plansRes.data);
      })
      .catch(() => setError("Could not load this property."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleBookClick = (bed) => {
    setError("");
    setSuccess("");
    if (!user) {
      navigate("/login");
      return;
    }
    if (user.role !== "GUEST") {
      setError("Only guest accounts can book a bed.");
      return;
    }
    setDurationMonths("");
    setSelectedBed(bed);
  };

  const discountedRent = (bed, plan) => {
    if (!plan) return Number(bed.monthlyRent);
    return Number(bed.monthlyRent) * (1 - Number(plan.discountPercent) / 100);
  };

  const submitBooking = async (e) => {
    e.preventDefault();
    setError("");
    setBooking(true);
    try {
      const payload = { bedId: selectedBed.id, checkInDate };
      if (durationMonths) payload.durationMonths = Number(durationMonths);
      await createBooking(payload);
      setSuccess("Booking request sent! The owner will review your details and respond soon.");
      setSelectedBed(null);
      // refresh rooms to reflect bed now pending
      const roomsRes = await getPropertyRooms(id);
      setRooms(roomsRes.data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not send booking request.");
    } finally {
      setBooking(false);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;
  if (!property) return <div className="container"><Alert type="error">Property not found.</Alert></div>;

  const images = property.imageUrls && property.imageUrls.length > 0 ? property.imageUrls : [];
  const selectedPlan = plans.find((p) => String(p.durationMonths) === String(durationMonths));

  return (
    <div className="container">
      <div className="property-card-image" style={{ borderRadius: "var(--radius-lg)", height: 320, marginBottom: 10, overflow: "hidden" }}>
        {images.length > 0 ? (
          <img src={images[activeImage]} alt={property.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
        ) : (
          <span>{property.name}</span>
        )}
      </div>

      {images.length > 1 && (
        <div style={{ display: "flex", gap: 8, marginBottom: 24, overflowX: "auto" }}>
          {images.map((url, i) => (
            <button
              key={i}
              onClick={() => setActiveImage(i)}
              style={{
                width: 72, height: 56, flexShrink: 0, padding: 0, border: i === activeImage ? "2px solid var(--teal-700)" : "2px solid transparent",
                borderRadius: "var(--radius-sm)", overflow: "hidden", cursor: "pointer", background: "none",
              }}
              aria-label={`Show photo ${i + 1}`}
            >
              <img src={url} alt={`${property.name} ${i + 1}`} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
            </button>
          ))}
        </div>
      )}

      <h1>{property.name}</h1>
      <p>{property.location?.area}, {property.location?.city} &middot; {property.address}</p>
      <p>{property.description}</p>

      {property.facilities && property.facilities.length > 0 && (
        <div className="pill-row" style={{ marginBottom: 24 }}>
          {property.facilities.map((f) => (
            <span key={f.id} className="pill">{f.name}</span>
          ))}
        </div>
      )}

      {plans.length > 0 && (
        <div className="pill-row" style={{ marginBottom: 24 }}>
          {plans.map((p) => (
            <span key={p.id} className="pill">{p.durationMonths} mo stay: {Number(p.discountPercent)}% off</span>
          ))}
        </div>
      )}

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      <h2 style={{ marginTop: 30 }}>Available rooms</h2>
      {rooms.length === 0 ? (
        <p>No rooms have been added to this property yet.</p>
      ) : (
        <div className="grid grid-2">
          {rooms.map((room) => (
            <div key={room.id} className="card">
              <div className="section-title-row">
                <h3 style={{ fontSize: "1.05rem", margin: 0 }}>
                  {room.type === "PRIVATE" ? "Private Room" : "Shared Room"} {room.roomNumber ? `— ${room.roomNumber}` : ""}
                </h3>
                <span className="pill">{room.type}</span>
              </div>
              {room.description && <p>{room.description}</p>}
              <div style={{ display: "flex", flexDirection: "column", gap: 10, marginTop: 12 }}>
                {room.beds.map((bed) => (
                  <div key={bed.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "10px 14px", background: "var(--sand-100)", borderRadius: "var(--radius-sm)" }}>
                    <div>
                      <strong>{bed.label}</strong>
                      <div style={{ fontSize: "0.85rem", color: "var(--ink-soft)" }}>₹{Number(bed.monthlyRent).toLocaleString("en-IN")} / month</div>
                    </div>
                    {bed.status === "AVAILABLE" ? (
                      <button className="btn btn-accent btn-sm" onClick={() => handleBookClick(bed)}>Request to book</button>
                    ) : (
                      <span className="badge badge-booked">{bed.status}</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {selectedBed && (
        <div className="card form-card">
          <h3>Confirm your booking</h3>
          <p>Booking <strong>{selectedBed.label}</strong> — ₹{Number(selectedBed.monthlyRent).toLocaleString("en-IN")}/month</p>
          <form onSubmit={submitBooking}>
            <div className="field">
              <label>Check-in date</label>
              <input type="date" required value={checkInDate} onChange={(e) => setCheckInDate(e.target.value)} />
            </div>

            {plans.length > 0 && (
              <div className="field">
                <label>Stay length</label>
                <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 4 }}>
                  <label style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 400 }}>
                    <input
                      type="radio" name="durationMonths" value=""
                      checked={durationMonths === ""} onChange={() => setDurationMonths("")}
                    />
                    Month-to-month — ₹{Number(selectedBed.monthlyRent).toLocaleString("en-IN")}/month, no fixed term
                  </label>
                  {plans.map((p) => (
                    <label key={p.id} style={{ display: "flex", alignItems: "center", gap: 8, fontWeight: 400 }}>
                      <input
                        type="radio" name="durationMonths" value={p.durationMonths}
                        checked={String(durationMonths) === String(p.durationMonths)}
                        onChange={() => setDurationMonths(p.durationMonths)}
                      />
                      {p.durationMonths} months — {Number(p.discountPercent)}% off, ₹{discountedRent(selectedBed, p).toLocaleString("en-IN", { maximumFractionDigits: 0 })}/month
                    </label>
                  ))}
                </div>
              </div>
            )}

            <div className="field-hint" style={{ marginBottom: 12 }}>
              Your stay is open-ended once approved — a chosen plan just locks in your monthly rate
              {selectedPlan ? ` for ${selectedPlan.durationMonths} months` : ""}.
            </div>

            <div style={{ display: "flex", gap: 10 }}>
              <button className="btn btn-primary" type="submit" disabled={booking}>
                {booking ? "Sending..." : "Send booking request"}
              </button>
              <button className="btn btn-outline" type="button" onClick={() => setSelectedBed(null)}>Cancel</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

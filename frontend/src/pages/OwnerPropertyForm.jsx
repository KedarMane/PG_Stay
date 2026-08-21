import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getPublicLocations, createProperty, updateProperty, getMyProperties } from "../api/endpoints";
import Alert from "../components/Alert";
import Loader from "../components/Loader";

const MIN_PHOTOS = 4;
const MAX_PHOTOS = 10;

function extractErrorMessage(err, fallback) {
  const data = err.response?.data;
  if (!data) return fallback;
  if (data.message) return data.message;
  if (data.errors) {
    const first = Object.values(data.errors)[0];
    if (first) return first;
  }
  return fallback;
}

export default function OwnerPropertyForm() {
  const { id } = useParams(); // present when editing
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [locations, setLocations] = useState([]);
  const [form, setForm] = useState({
    name: "", description: "", address: "", genderPreference: "ANY",
    locationId: "", facilities: "",
  });
  const [imageUrls, setImageUrls] = useState(Array(MIN_PHOTOS).fill(""));
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadAll = async () => {
      try {
        const locRes = await getPublicLocations();
        setLocations(locRes.data.filter((l) => l.active));

        if (isEdit) {
          const propsRes = await getMyProperties();
          const existing = propsRes.data.find((p) => String(p.id) === String(id));
          if (existing) {
            setForm({
              name: existing.name || "",
              description: existing.description || "",
              address: existing.address || "",
              genderPreference: existing.genderPreference || "ANY",
              locationId: existing.location?.id || "",
              facilities: (existing.facilities || []).map((f) => f.name).join(", "),
            });
            const existingImages = existing.imageUrls || [];
            const padded = existingImages.length >= MIN_PHOTOS
              ? existingImages
              : [...existingImages, ...Array(MIN_PHOTOS - existingImages.length).fill("")];
            setImageUrls(padded);
          }
        }
      } catch (e) {
        setError("Could not load form data.");
      } finally {
        setLoading(false);
      }
    };
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleImageChange = (index, value) => {
    setImageUrls((prev) => {
      const next = [...prev];
      next[index] = value;
      return next;
    });
  };

  const addPhotoField = () => {
    setImageUrls((prev) => (prev.length >= MAX_PHOTOS ? prev : [...prev, ""]));
  };

  const removePhotoField = (index) => {
    setImageUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const filledPhotoCount = imageUrls.filter((u) => u.trim()).length;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const cleanedImages = imageUrls.map((u) => u.trim()).filter(Boolean);
    if (cleanedImages.length < MIN_PHOTOS) {
      setError(`Please add at least ${MIN_PHOTOS} photos (you have ${cleanedImages.length}).`);
      return;
    }

    setSaving(true);
    try {
      const payload = {
        name: form.name,
        description: form.description,
        address: form.address,
        genderPreference: form.genderPreference,
        locationId: Number(form.locationId),
        imageUrls: cleanedImages,
        facilities: form.facilities ? form.facilities.split(",").map((s) => s.trim()).filter(Boolean) : [],
      };
      if (isEdit) {
        await updateProperty(id, payload);
        navigate(`/owner/properties/${id}`);
      } else {
        const res = await createProperty(payload);
        navigate(`/owner/properties/${res.data.id}`);
      }
    } catch (err) {
      setError(extractErrorMessage(err, "Could not save property."));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">{isEdit ? "Edit listing" : "New listing"}</span>
        <h1>{isEdit ? "Edit property" : "Add a property"}</h1>
        <p>Submitted properties go to the admin for review before they appear in search.</p>
      </div>

      <div className="card form-card" style={{ maxWidth: 560 }}>
        <Alert type="error">{error}</Alert>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Property name</label>
            <input name="name" required value={form.name} onChange={handleChange} placeholder="Sunrise PG for Men" />
          </div>
          <div className="field">
            <label>Location</label>
            <select name="locationId" required value={form.locationId} onChange={handleChange}>
              <option value="">Select a location...</option>
              {locations.map((l) => (
                <option key={l.id} value={l.id}>{l.area}, {l.city}</option>
              ))}
            </select>
            <div className="field-hint">You can only list under locations the admin has already added.</div>
          </div>
          <div className="field">
            <label>Address</label>
            <input name="address" value={form.address} onChange={handleChange} placeholder="Street, landmark" />
          </div>
          <div className="field">
            <label>Gender preference</label>
            <select name="genderPreference" value={form.genderPreference} onChange={handleChange}>
              <option value="ANY">Any</option>
              <option value="MALE">Male only</option>
              <option value="FEMALE">Female only</option>
            </select>
          </div>
          <div className="field">
            <label>Description</label>
            <textarea name="description" rows={4} value={form.description} onChange={handleChange} placeholder="Tell guests about the property..." />
          </div>

          <div className="field">
            <label>
              Property photos{" "}
              <span style={{ fontWeight: 400, color: filledPhotoCount < MIN_PHOTOS ? "var(--coral-500)" : "var(--ink-soft)" }}>
                ({filledPhotoCount}/{MIN_PHOTOS} minimum)
              </span>
            </label>
            <div className="field-hint" style={{ marginBottom: 10 }}>
              Add at least {MIN_PHOTOS} photo links so guests can see the place clearly before booking.
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {imageUrls.map((url, index) => (
                <div key={index} style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <div
                    style={{
                      width: 48, height: 48, borderRadius: "var(--radius-sm)", flexShrink: 0,
                      background: "var(--sand-100)", overflow: "hidden",
                      display: "flex", alignItems: "center", justifyContent: "center",
                      fontSize: "0.7rem", color: "var(--ink-soft)",
                    }}
                  >
                    {url.trim() ? (
                      <img
                        src={url}
                        alt={`Photo ${index + 1}`}
                        style={{ width: "100%", height: "100%", objectFit: "cover" }}
                        onError={(e) => { e.target.style.display = "none"; }}
                      />
                    ) : (
                      `#${index + 1}`
                    )}
                  </div>
                  <input
                    value={url}
                    onChange={(e) => handleImageChange(index, e.target.value)}
                    placeholder={`Photo ${index + 1} URL`}
                    style={{ flex: 1 }}
                  />
                  {imageUrls.length > MIN_PHOTOS && (
                    <button
                      type="button"
                      onClick={() => removePhotoField(index)}
                      className="btn btn-outline btn-sm"
                      aria-label={`Remove photo ${index + 1}`}
                    >
                      ×
                    </button>
                  )}
                </div>
              ))}
            </div>

            {imageUrls.length < MAX_PHOTOS && (
              <button type="button" className="btn btn-outline btn-sm" style={{ marginTop: 10 }} onClick={addPhotoField}>
                + Add another photo
              </button>
            )}
          </div>

          <div className="field">
            <label>Facilities</label>
            <input name="facilities" value={form.facilities} onChange={handleChange} placeholder="e.g. WiFi, Laundry, Swimming Pool (comma-separated)" />
            <div className="field-hint">You can add or remove individual facilities later from the property page — no re-approval needed.</div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={saving}>
            {saving ? "Saving..." : isEdit ? "Save changes" : "Submit for approval"}
          </button>
        </form>
      </div>
    </div>
  );
}

import React, { useEffect, useState } from "react";
import { getMyProfile, updateProfile } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Alert from "../components/Alert";
import Loader from "../components/Loader";

export default function GuestProfile() {
  const { setUser } = useAuth();
  const [form, setForm] = useState({ phone: "", govtIdType: "", govtIdNumber: "", govtIdDocUrl: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    getMyProfile()
      .then((res) => {
        const p = res.data;
        setForm({
          phone: p.phone || "",
          govtIdType: p.govtIdType || "",
          govtIdNumber: p.govtIdNumber || "",
          govtIdDocUrl: p.govtIdDocUrl || "",
        });
      })
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSaving(true);
    try {
      const res = await updateProfile(form);
      setSuccess("Profile updated.");
      setUser((prev) => ({ ...prev, profileCompleted: res.data.profileCompleted }));
    } catch (err) {
      setError(err.response?.data?.message || "Could not update profile.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Verification</span>
        <h1>Complete your profile</h1>
        <p>We need a government ID on file before you can request a booking — this is what the owner checks before approving you.</p>
      </div>

      <div className="card form-card">
        <Alert type="error">{error}</Alert>
        <Alert type="success">{success}</Alert>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Phone number</label>
            <input name="phone" required value={form.phone} onChange={handleChange} />
          </div>
          <div className="field">
            <label>Govt ID type</label>
            <select name="govtIdType" required value={form.govtIdType} onChange={handleChange}>
              <option value="">Select...</option>
              <option value="AADHAAR">Aadhaar</option>
              <option value="PAN">PAN Card</option>
              <option value="PASSPORT">Passport</option>
              <option value="DRIVING_LICENSE">Driving License</option>
            </select>
          </div>
          <div className="field">
            <label>Govt ID number</label>
            <input name="govtIdNumber" required value={form.govtIdNumber} onChange={handleChange} />
          </div>
          <div className="field">
            <label>ID document URL</label>
            <input name="govtIdDocUrl" required value={form.govtIdDocUrl} onChange={handleChange} placeholder="Link to uploaded scan/photo" />
            <div className="field-hint">In production this would be a direct file upload; for now paste a hosted image/PDF link.</div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={saving}>
            {saving ? "Saving..." : "Save profile"}
          </button>
        </form>
      </div>
    </div>
  );
}

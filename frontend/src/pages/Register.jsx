import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Alert from "../components/Alert";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", phone: "", role: "GUEST" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const userData = await register(form);
      navigate(userData.role === "OWNER" ? "/owner/profile" : "/guest/profile");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <span style={{ color: "var(--sage-500)", fontFamily: "var(--font-mono)", fontSize: "0.72rem", textTransform: "uppercase", letterSpacing: "0.08em" }}>
          Join Staylist
        </span>
        <h1>Create your account</h1>
        <Alert type="error">{error}</Alert>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>I am a...</label>
            <select name="role" value={form.role} onChange={handleChange}>
              <option value="GUEST">Guest — looking for a PG</option>
              <option value="OWNER">Owner — listing a property</option>
            </select>
          </div>
          <div className="field">
            <label>Full name</label>
            <input name="name" required value={form.name} onChange={handleChange} placeholder="Jane Doe" />
          </div>
          <div className="field">
            <label>Email</label>
            <input type="email" name="email" required value={form.email} onChange={handleChange} placeholder="you@example.com" />
          </div>
          <div className="field">
            <label>Phone</label>
            <input name="phone" value={form.phone} onChange={handleChange} placeholder="+91 9XXXXXXXXX" />
          </div>
          <div className="field">
            <label>Password</label>
            <input type="password" name="password" required value={form.password} onChange={handleChange} placeholder="At least 8 characters" />
          </div>
          <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
            {loading ? "Creating account..." : "Sign up"}
          </button>
        </form>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}

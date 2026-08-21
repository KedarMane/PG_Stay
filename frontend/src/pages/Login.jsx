import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Alert from "../components/Alert";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const userData = await login(form.email, form.password);
      if (userData.role === "ADMIN") navigate("/admin");
      else if (userData.role === "OWNER") navigate("/owner");
      else navigate("/search");
    } catch (err) {
      setError(err.response?.data?.message || "Login failed. Check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <span className="eyebrow" style={{ color: "var(--sage-500)", fontFamily: "var(--font-mono)", fontSize: "0.72rem", textTransform: "uppercase", letterSpacing: "0.08em" }}>
          Welcome back
        </span>
        <h1>Log in to Staylist</h1>
        <Alert type="error">{error}</Alert>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Email</label>
            <input type="email" name="email" required value={form.email} onChange={handleChange} placeholder="you@example.com" />
          </div>
          <div className="field">
            <label>Password</label>
            <input type="password" name="password" required value={form.password} onChange={handleChange} placeholder="••••••••" />
          </div>
          <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
            {loading ? "Logging in..." : "Log in"}
          </button>
        </form>
        <p className="auth-switch">
          New here? <Link to="/register">Create an account</Link>
        </p>
        <p className="auth-switch" style={{ fontSize: "0.78rem" }}>
          Admin demo login: admin@pgms.com / Admin@123
        </p>
      </div>
    </div>
  );
}

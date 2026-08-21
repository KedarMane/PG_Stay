import React from "react";
import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState";

export default function NotFound() {
  return (
    <div className="container">
      <EmptyState title="Page not found" subtitle="The page you're looking for doesn't exist." />
      <div style={{ textAlign: "center" }}>
        <Link to="/" className="btn btn-primary">Go home</Link>
      </div>
    </div>
  );
}

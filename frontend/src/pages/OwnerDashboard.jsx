import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyProperties } from "../api/endpoints";
import Loader from "../components/Loader";
import EmptyState from "../components/EmptyState";
import StatusBadge from "../components/StatusBadge";

export default function OwnerDashboard() {
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyProperties()
      .then((res) => setProperties(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="section-title-row">
        <div>
          <span className="eyebrow">Owner dashboard</span>
          <h1 style={{ margin: 0 }}>My Properties</h1>
        </div>
        <Link to="/owner/properties/new" className="btn btn-accent">+ Add property</Link>
      </div>

      {properties.length === 0 ? (
        <EmptyState title="No properties yet" subtitle="Add your first PG to get started — it'll need admin approval before guests can see it." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Location</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {properties.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.location?.area}, {p.location?.city}</td>
                  <td>
                    <StatusBadge status={p.status} />
                    {p.status === "REJECTED" && p.rejectionReason && (
                      <div style={{ fontSize: "0.78rem", color: "var(--coral-500)", marginTop: 4 }}>{p.rejectionReason}</div>
                    )}
                  </td>
                  <td>
                    <Link to={`/owner/properties/${p.id}`} className="btn btn-outline btn-sm">Manage</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

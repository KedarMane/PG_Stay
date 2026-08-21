import React, { useEffect, useState } from "react";
import { searchProperties } from "../api/endpoints";
import PropertyCard from "../components/PropertyCard";
import Loader from "../components/Loader";
import EmptyState from "../components/EmptyState";

export default function GuestSearch() {
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);

  const runSearch = async (params = {}) => {
    setLoading(true);
    try {
      const res = await searchProperties(params);
      setProperties(res.data);
    } catch (e) {
      setProperties([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runSearch();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = {};
    if (name) params.name = name;
    if (location) params.location = location;
    runSearch(params);
  };

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Browse listings</span>
        <h1>Find a PG</h1>
      </div>

      <form onSubmit={handleSearch} className="card" style={{ display: "flex", gap: 12, flexWrap: "wrap", marginBottom: 30 }}>
        <div style={{ flex: "1 1 220px" }}>
          <input placeholder="Search by PG name" value={name} onChange={(e) => setName(e.target.value)} />
        </div>
        <div style={{ flex: "1 1 220px" }}>
          <input placeholder="Search by city or area" value={location} onChange={(e) => setLocation(e.target.value)} />
        </div>
        <button className="btn btn-primary" type="submit">Search</button>
      </form>

      {loading ? (
        <Loader />
      ) : properties.length === 0 ? (
        <EmptyState title="No PGs found" subtitle="Try a different name or location." />
      ) : (
        <div className="grid grid-3">
          {properties.map((p) => (
            <PropertyCard key={p.id} property={p} />
          ))}
        </div>
      )}
    </div>
  );
}

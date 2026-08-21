import React from "react";
import { Link } from "react-router-dom";

export default function PropertyCard({ property }) {
  return (
    <Link to={`/property/${property.id}`} style={{ textDecoration: "none", color: "inherit" }}>
      <div className="card card-hover property-card">
        <div className="property-card-image">
          {property.imageUrls && property.imageUrls.length > 0 ? (
            <img
              src={property.imageUrls[0]}
              alt={property.name}
              style={{ width: "100%", height: "100%", objectFit: "cover" }}
            />
          ) : (
            <span>{property.name}</span>
          )}
        </div>
        <div className="property-card-body">
          <h3 style={{ fontSize: "1.15rem", marginBottom: 4 }}>{property.name}</h3>
          <p style={{ margin: 0, fontSize: "0.88rem" }}>
            {property.location ? `${property.location.area}, ${property.location.city}` : ""}
          </p>
          {property.facilities && property.facilities.length > 0 && (
            <div className="pill-row">
              {property.facilities.slice(0, 3).map((f) => (
                <span key={f.id} className="pill">{f.name}</span>
              ))}
              {property.facilities.length > 3 && (
                <span className="pill">+{property.facilities.length - 3} more</span>
              )}
            </div>
          )}
        </div>
      </div>
    </Link>
  );
}

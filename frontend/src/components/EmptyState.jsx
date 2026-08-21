import React from "react";

export default function EmptyState({ title, subtitle }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      {subtitle && <p>{subtitle}</p>}
    </div>
  );
}

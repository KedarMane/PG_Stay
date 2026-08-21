import React, { useCallback, useEffect, useState } from "react";
import { getMyBookings, createPaymentOrder, verifyPayment, getPaymentsForBooking } from "../api/endpoints";
import { useAuth } from "../context/AuthContext";
import Loader from "../components/Loader";
import EmptyState from "../components/EmptyState";
import StatusBadge from "../components/StatusBadge";
import Alert from "../components/Alert";

function currentBillingMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export default function MyBookings() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  // bookingId -> "paid" | "unpaid"  (for the current billing month, APPROVED bookings only)
  const [paidStatus, setPaidStatus] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [payingId, setPayingId] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getMyBookings();
      const list = res.data;
      setBookings(list);

      const thisMonth = currentBillingMonth();
      const approved = list.filter((b) => b.status === "APPROVED");
      const entries = await Promise.all(
        approved.map(async (b) => {
          try {
            const payRes = await getPaymentsForBooking(b.id);
            const paidThisMonth = payRes.data.some((p) => p.billingMonth === thisMonth && p.status === "PAID");
            return [b.id, paidThisMonth ? "paid" : "unpaid"];
          } catch {
            return [b.id, "unpaid"];
          }
        })
      );
      setPaidStatus(Object.fromEntries(entries));
    } catch {
      setError("Could not load your bookings.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handlePay = async (bookingId) => {
    setError("");
    setSuccess("");
    setPayingId(bookingId);
    try {
      if (!window.Razorpay) {
        throw new Error("Payment library didn't load — check your connection and try again.");
      }

      const orderRes = await createPaymentOrder(bookingId);
      const order = orderRes.data;

      const rzp = new window.Razorpay({
        key: order.razorpayKeyId,
        order_id: order.razorpayOrderId,
        amount: Math.round(order.amount * 100),
        currency: order.currency,
        name: "Staylist",
        description: `Rent for ${order.billingMonth} — ₹${order.amount}`,
        prefill: {
          name: user?.name || "",
          email: user?.email || "",
          contact: user?.phone || "",
        },
        theme: { color: "#0F5257" },
        handler: async (response) => {
          try {
            await verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            setSuccess(`Payment received for ${order.billingMonth}.`);
            load();
          } catch (err) {
            setError(err.response?.data?.message || "We couldn't verify that payment. Please contact support before trying again.");
          } finally {
            setPayingId(null);
          }
        },
        modal: {
          ondismiss: () => setPayingId(null),
        },
      });

      rzp.on("payment.failed", () => {
        setError("Payment failed or was cancelled. You can try again anytime.");
        setPayingId(null);
      });

      rzp.open();
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Could not start payment.");
      setPayingId(null);
    }
  };

  if (loading) return <div className="container"><Loader /></div>;

  return (
    <div className="container">
      <div className="page-header">
        <span className="eyebrow">Your stays</span>
        <h1>My Bookings</h1>
      </div>

      <Alert type="error">{error}</Alert>
      <Alert type="success">{success}</Alert>

      {bookings.length === 0 ? (
        <EmptyState title="No bookings yet" subtitle="Once you request a bed, it'll show up here." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Bed</th>
                <th>Room</th>
                <th>Check-in</th>
                <th>Plan</th>
                <th>Status</th>
                <th>This month's rent</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.id}>
                  <td>{b.bed?.label}</td>
                  <td>{b.bed?.room?.type}</td>
                  <td>{b.checkInDate}</td>
                  <td>
                    {b.planDurationMonths ? (
                      <span className="pill">{b.planDurationMonths} mo · {Number(b.discountPercent)}% off</span>
                    ) : (
                      <span style={{ color: "var(--ink-soft)", fontSize: "0.85rem" }}>Month-to-month</span>
                    )}
                  </td>
                  <td><StatusBadge status={b.status} /></td>
                  <td>
                    {b.status === "APPROVED" ? (
                      <StatusBadge status={paidStatus[b.id] === "paid" ? "PAID" : "PENDING"} />
                    ) : (
                      <span style={{ color: "var(--ink-soft)" }}>—</span>
                    )}
                  </td>
                  <td>
                    {b.status === "APPROVED" && paidStatus[b.id] !== "paid" && (
                      <button
                        className="btn btn-accent btn-sm"
                        onClick={() => handlePay(b.id)}
                        disabled={payingId === b.id}
                      >
                        {payingId === b.id ? "Processing..." : "Pay rent"}
                      </button>
                    )}
                    {b.status === "APPROVED" && paidStatus[b.id] === "paid" && (
                      <span style={{ fontSize: "0.82rem", color: "var(--sage-500)" }}>✓ Paid for this month</span>
                    )}
                    {b.status === "REJECTED" && b.rejectionReason && (
                      <span style={{ fontSize: "0.82rem", color: "var(--coral-500)" }}>{b.rejectionReason}</span>
                    )}
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

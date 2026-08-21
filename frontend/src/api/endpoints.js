import api from "./axiosConfig";

// ---- Auth ----
export const registerUser = (data) => api.post("/auth/register", data);
export const loginUser = (data) => api.post("/auth/login", data);

// ---- Profile ----
export const getMyProfile = () => api.get("/profile/me");
export const updateProfile = (data) => api.put("/profile", data);

// ---- Locations ----
// Admin-only management (create/update/deactivate) lives under /admin/locations.
// getPublicLocations is the read-only endpoint any logged-in role (owner/guest) can call
// to populate a location dropdown.
export const getLocations = () => api.get("/admin/locations");
export const createLocation = (data) => api.post("/admin/locations", data);
export const updateLocation = (id, data) => api.put(`/admin/locations/${id}`, data);
export const deactivateLocation = (id) => api.delete(`/admin/locations/${id}`);
export const getPublicLocations = () => api.get("/locations");

// ---- Admin property approvals ----
export const getPendingProperties = () => api.get("/admin/properties/pending");
export const getAllPropertiesAdmin = () => api.get("/admin/properties");
export const approveProperty = (id) => api.put(`/admin/properties/${id}/approve`);
export const rejectProperty = (id, reason) => api.put(`/admin/properties/${id}/reject`, { reason });
export const getAllBookingsAdmin = () => api.get("/admin/bookings");
export const getAllPaymentsAdmin = () => api.get("/admin/payments");

// ---- Owner properties ----
export const createProperty = (data) => api.post("/owner/properties", data);
export const getMyProperties = () => api.get("/owner/properties");
export const updateProperty = (id, data) => api.put(`/owner/properties/${id}`, data);
export const addFacility = (propertyId, data) => api.post(`/owner/properties/${propertyId}/facilities`, data);
export const removeFacility = (propertyId, facilityId) =>
  api.delete(`/owner/properties/${propertyId}/facilities/${facilityId}`);
export const addRoom = (propertyId, data) => api.post(`/owner/properties/${propertyId}/rooms`, data);
export const getOwnerRooms = (propertyId) => api.get(`/owner/properties/${propertyId}/rooms`);

// ---- Owner bookings ----
export const getOwnerBookings = () => api.get("/owner/bookings");
export const approveBooking = (id) => api.put(`/owner/bookings/${id}/approve`);
export const rejectBooking = (id, reason) => api.put(`/owner/bookings/${id}/reject`, { reason });

// ---- Guest browsing ----
export const searchProperties = (params) => api.get("/guest/properties/search", { params });
export const getPropertyById = (id) => api.get(`/guest/properties/${id}`);
export const getPropertyRooms = (id) => api.get(`/guest/properties/${id}/rooms`);

// ---- Guest bookings ----
export const createBooking = (data) => api.post("/guest/bookings", data);
export const getMyBookings = () => api.get("/guest/bookings");

// ---- Payments ----
export const createPaymentOrder = (bookingId) =>
  api.post(`/payments/bookings/${bookingId}/create-order`);
export const verifyPayment = (data) => api.post("/payments/verify", data);
export const getPaymentsForBooking = (bookingId) => api.get(`/payments/bookings/${bookingId}`);

// ---- Discount plans (owner) ----
export const getDiscountPlans = (propertyId) => api.get(`/owner/properties/${propertyId}/discount-plans`);
export const createDiscountPlan = (propertyId, data) => api.post(`/owner/properties/${propertyId}/discount-plans`, data);
export const updateDiscountPlan = (propertyId, planId, data) => api.put(`/owner/properties/${propertyId}/discount-plans/${planId}`, data);
export const toggleDiscountPlan = (propertyId, planId) => api.put(`/owner/properties/${propertyId}/discount-plans/${planId}/toggle`);
export const deleteDiscountPlan = (propertyId, planId) => api.delete(`/owner/properties/${propertyId}/discount-plans/${planId}`);

// ---- Discount plans (guest, public read-only) ----
export const getPublicDiscountPlans = (propertyId) => api.get(`/guest/properties/${propertyId}/discount-plans`);

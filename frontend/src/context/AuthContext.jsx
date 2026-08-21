import React, { createContext, useContext, useEffect, useState } from "react";
import { loginUser, registerUser, getMyProfile } from "../api/endpoints";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem("pgms_user");
    const token = localStorage.getItem("pgms_token");
    if (stored && token) {
      setUser(JSON.parse(stored));
      // refresh profile in background (picks up profileCompleted etc.)
      getMyProfile()
        .then((res) => {
          const merged = { ...JSON.parse(stored), ...res.data };
          setUser(merged);
          localStorage.setItem("pgms_user", JSON.stringify(merged));
        })
        .catch(() => {});
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const res = await loginUser({ email, password });
    const { token, ...userData } = res.data;
    localStorage.setItem("pgms_token", token);
    localStorage.setItem("pgms_user", JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const register = async (payload) => {
    const res = await registerUser(payload);
    const { token, ...userData } = res.data;
    localStorage.setItem("pgms_token", token);
    localStorage.setItem("pgms_user", JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem("pgms_token");
    localStorage.removeItem("pgms_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

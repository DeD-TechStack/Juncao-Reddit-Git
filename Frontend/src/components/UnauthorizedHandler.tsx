import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { UNAUTHORIZED_EVENT } from "../lib/apiFetch";

export default function UnauthorizedHandler() {
  const { logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    function handleUnauthorized() {
      if (!isAuthenticated) return;
      logout();
      navigate("/login");
    }

    window.addEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
  }, [logout, navigate, isAuthenticated]);

  return null;
}

import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "../contexts/AuthContext";
import { IdeasProvider } from "../contexts/IdeasContext";
import ProtectedRoute from "../components/ProtectedRoute";

import LandingPage from "../pages/LandingPage";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import DashboardPage from "../pages/DashboardPage";
import CreateIdeaPage from "../pages/CreateIdeaPage";
import EditIdeaPage from "../pages/EditIdeaPage";
import IdeasListPage from "../pages/IdeasListPage";
import ViewIdeaPage from "../pages/ViewIdeaPage";
import ProfilePage from "../pages/ProfilePage";
import ErrorPage from "../pages/ErrorPage";

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <IdeasProvider>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/ideas" element={<ProtectedRoute><IdeasListPage /></ProtectedRoute>} />
            <Route path="/create-idea" element={<ProtectedRoute><CreateIdeaPage /></ProtectedRoute>} />
            <Route path="/edit-idea/:id" element={<ProtectedRoute><EditIdeaPage /></ProtectedRoute>} />
            <Route path="/view-idea/:id" element={<ProtectedRoute><ViewIdeaPage /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />

            <Route path="/403" element={<ErrorPage type="403" />} />
            <Route path="*" element={<ErrorPage type="404" />} />
          </Routes>
        </IdeasProvider>
      </AuthProvider>
    </Router>
  );
}

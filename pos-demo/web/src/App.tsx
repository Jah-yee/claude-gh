import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { Dashboard } from './pages/Dashboard';
import { Register } from './pages/Register';

export function App() {
  return (
    <>
      <header className="topbar">
        <strong>StoreLite POS</strong>
        <nav>
          <NavLink to="/register">Register</NavLink>
          <NavLink to="/dashboard">Dashboard</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="*" element={<Navigate to="/register" replace />} />
        </Routes>
      </main>
    </>
  );
}

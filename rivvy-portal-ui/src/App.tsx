import { Routes, Route } from 'react-router-dom';
import RootLayout from './layouts/RootLayout';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import DashboardPage from './pages/DashboardPage';
import NewBriefPage from './pages/NewBriefPage';
import VaultPage from './pages/VaultPage';
import ScreeningPage from './pages/ScreeningPage';
import VideoDetailPage from './pages/VideoDetailPage';
import AdminPage from './pages/AdminPage';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RootLayout />}>
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/new-brief" element={<NewBriefPage />} />
        <Route path="/vault" element={<VaultPage />} />
        <Route path="/screening" element={<ScreeningPage />} />
        <Route path="/screening/:videoId" element={<VideoDetailPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Route>
    </Routes>
  );
}

export default App;

import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { authApi } from '../api/authApi';
import Alert from '../components/Alert';

export default function DashboardPage() {
  const { user, logout, refreshUser } = useAuth();
  const { theme, toggleTheme } = useTheme();

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [tfaSetup, setTfaSetup] = useState(null);
  const [tfaCode, setTfaCode] = useState('');
  const [loading, setLoading] = useState(false);

  const handleEnableTfa = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const response = await authApi.enableTfa();
      setTfaSetup(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to enable 2FA');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyTfa = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await authApi.verifyTfa({ code: tfaCode });
      setSuccess(response.data.message);
      setTfaSetup(null);
      setTfaCode('');
      await refreshUser();
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid code');
    } finally {
      setLoading(false);
    }
  };

  const handleDisableTfa = async () => {
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const response = await authApi.disableTfa();
      setSuccess(response.data.message);
      await refreshUser();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to disable 2FA');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-layout">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            className="btn btn-secondary"
            style={{ width: 'auto' }}
            onClick={toggleTheme}
          >
            {theme === 'light' ? '🌙 Dark' : '☀️ Light'}
          </button>
          <button
            className="btn btn-secondary"
            style={{ width: 'auto' }}
            onClick={logout}
          >
            Sign out
          </button>
        </div>
      </div>

      <Alert type="error" message={error} />
      <Alert type="success" message={success} />

      <div className="dashboard-card">
        <h2>Profile</h2>
        <div className="info-row">
          <span className="label">Name</span>
          <span>{user?.firstName} {user?.lastName}</span>
        </div>
        <div className="info-row">
          <span className="label">Email</span>
          <span>{user?.email}</span>
        </div>
        <div className="info-row">
          <span className="label">Roles</span>
          <span>{user?.roles?.join(', ')}</span>
        </div>
      </div>

      <div className="dashboard-card">
        <h2>Two-Factor Authentication</h2>
        <div className="info-row">
          <span className="label">Status</span>
          <span style={{ color: user?.tfaEnabled ? 'var(--success)' : 'var(--text-secondary)' }}>
            {user?.tfaEnabled ? 'Enabled' : 'Disabled'}
          </span>
        </div>

        {!user?.tfaEnabled && !tfaSetup && (
          <button
            className="btn btn-primary"
            style={{ marginTop: '1rem' }}
            onClick={handleEnableTfa}
            disabled={loading}
          >
            {loading ? 'Setting up...' : 'Enable 2FA'}
          </button>
        )}

        {tfaSetup && (
          <div style={{ marginTop: '1rem' }}>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
              {tfaSetup.message}
            </p>
            <div className="qr-container">
              <img src={tfaSetup.qrCodeUri} alt="QR Code" />
            </div>
            <div className="secret-code">
              Secret: {tfaSetup.secret}
            </div>
            <form onSubmit={handleVerifyTfa} style={{ marginTop: '1rem' }}>
              <div className="form-group">
                <label htmlFor="tfaCode">Verification Code</label>
                <input
                  type="text"
                  id="tfaCode"
                  placeholder="Enter 6-digit code"
                  value={tfaCode}
                  onChange={(e) => setTfaCode(e.target.value)}
                  maxLength={6}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Verifying...' : 'Verify & Enable'}
              </button>
            </form>
          </div>
        )}

        {user?.tfaEnabled && (
          <button
            className="btn btn-danger"
            style={{ marginTop: '1rem' }}
            onClick={handleDisableTfa}
            disabled={loading}
          >
            {loading ? 'Disabling...' : 'Disable 2FA'}
          </button>
        )}
      </div>
    </div>
  );
}

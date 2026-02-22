import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ThemeToggle from '../components/ThemeToggle';
import Alert from '../components/Alert';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false,
    tfaCode: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [tfaRequired, setTfaRequired] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await login(formData);
      if (result.tfaRequired) {
        setTfaRequired(true);
        setLoading(false);
        return;
      }
      navigate('/dashboard');
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please try again.';
      setError(message);
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <ThemeToggle />
        <h1>Welcome back</h1>
        <p className="subtitle">Sign in to your account</p>

        <Alert type="error" message={error} />

        <form onSubmit={handleSubmit}>
          {!tfaRequired ? (
            <>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="flex-between">
                <div className="checkbox-group">
                  <input
                    type="checkbox"
                    id="rememberMe"
                    name="rememberMe"
                    checked={formData.rememberMe}
                    onChange={handleChange}
                  />
                  <label htmlFor="rememberMe">Remember me</label>
                </div>
                <Link to="/forgot-password" className="auth-link">
                  Forgot password?
                </Link>
              </div>
            </>
          ) : (
            <div className="form-group">
              <label htmlFor="tfaCode">Two-Factor Authentication Code</label>
              <input
                type="text"
                id="tfaCode"
                name="tfaCode"
                placeholder="Enter 6-digit code"
                value={formData.tfaCode}
                onChange={handleChange}
                maxLength={6}
                autoFocus
                required
              />
            </div>
          )}

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Signing in...' : (tfaRequired ? 'Verify' : 'Sign in')}
          </button>
        </form>

        <div className="auth-footer">
          Don't have an account? <Link to="/register">Sign up</Link>
        </div>
      </div>
    </div>
  );
}

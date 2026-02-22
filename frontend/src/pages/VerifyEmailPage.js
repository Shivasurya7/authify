import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { authApi } from '../api/authApi';
import ThemeToggle from '../components/ThemeToggle';
import Alert from '../components/Alert';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      setError('Invalid verification link');
      setLoading(false);
      return;
    }

    const verify = async () => {
      try {
        const response = await authApi.verifyEmail(token);
        setSuccess(response.data.message);
      } catch (err) {
        setError(err.response?.data?.message || 'Verification failed.');
      } finally {
        setLoading(false);
      }
    };

    verify();
  }, [token]);

  return (
    <div className="auth-layout">
      <div className="auth-card">
        <ThemeToggle />
        <h1>Email Verification</h1>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '2rem 0' }}>
            <div className="spinner" style={{ margin: '0 auto' }}></div>
            <p className="subtitle" style={{ marginTop: '1rem' }}>Verifying your email...</p>
          </div>
        ) : (
          <>
            <Alert type="error" message={error} />
            <Alert type="success" message={success} />

            <div className="auth-footer" style={{ marginTop: '1rem' }}>
              <Link to="/login">Go to sign in</Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

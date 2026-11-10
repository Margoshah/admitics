import { useState } from 'react';
import api from './api';

export default function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('ivanov'); // по умолчанию подставляем оператора из DataInitializer
  const [password, setPassword] = useState('operator123');
  const [error, setError] = useState(null);

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/auth/login', { username, password });
      localStorage.setItem('token', response.data.access_token);
      localStorage.setItem('role', response.data.role);
      localStorage.setItem('username', response.data.username);
      onLoginSuccess(); // Перенаправляем на дашборд
    } catch (err) {
      setError('Неверный логин или пароль');
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: '#F8FAFC', fontFamily: 'Inter, sans-serif' }}>
      <form onSubmit={handleLogin} style={{ background: '#FFFFFF', padding: '32px', borderRadius: '16px', border: '1px solid #E2E8F0', width: '380px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.05)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
          <div style={{ width: '40px', height: '40px', background: '#2563EB', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 'bold' }}>A</div>
          <div>
            <h2 style={{ fontSize: '18px', margin: 0, color: '#172033' }}>Admitics</h2>
            <p style={{ fontSize: '12px', color: '#64748B', margin: 0 }}>Вход в систему приёмной комиссии</p>
          </div>
        </div>

        {error && <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '10px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' }}>{error}</div>}

        <div style={{ marginBottom: '16px' }}>
          <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: '#64748B', marginBottom: '6px' }}>Имя пользователя</label>
          <input 
            type="text" 
            value={username} 
            onChange={(e) => setUsername(e.target.value)} 
            style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1.5px solid #CBD5E1', fontSize: '14px', outline: 'none' }}
            required 
          />
        </div>

        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: '#64748B', marginBottom: '6px' }}>Пароль</label>
          <input 
            type="password" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1.5px solid #CBD5E1', fontSize: '14px', outline: 'none' }}
            required 
          />
        </div>

        <button type="submit" style={{ width: '100%', background: '#2563EB', color: '#fff', padding: '11px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', border: 'none', cursor: 'pointer' }}>
          Войти в систему
        </button>
      </form>
    </div>
  );
}
import { useEffect, useState } from 'react';
import api from './api';

export default function Dashboard({ onLogout }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get('/dashboard/summary')
      .then(response => {
        setData(response.data);
        setLoading(false);
      })
      .catch(err => {
        setError('Ошибка загрузки данных с бэкенда');
        setLoading(false);
      });
  }, []);

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Загрузка аналитики приёмной кампании...</div>;
  if (error) return <div style={{ padding: '40px', color: '#DC2626' }}>{error}</div>;

  const { kpi, top5Directions, geography, recentEvents } = data;

  return (
    <div className="dashboard">
      {/* SIDEBAR */}
      <aside className="sidebar" id="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <div className="sidebar-logo-icon">
              <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 24 L16 7 L23 24" fill="none" stroke="#FFFFFF" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M12 18 L20 18" fill="none" stroke="#FFFFFF" strokeWidth="2.6" strokeLinecap="round"/>
              </svg>
            </div>
            <div>
              <div className="sidebar-logo-name">Admitics</div>
              <div className="sidebar-logo-sub">Аналитика приёмной кампании</div>
            </div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <a href="#" className="sidebar-link active">Главная</a>
          <a href="#" className="sidebar-link">Динамика</a>
          <a href="#" className="sidebar-link">Направления</a>
          <a href="#" className="sidebar-link">Абитуриенты</a>
          <div className="sidebar-section-title">Аналитика</div>
          <a href="#" className="sidebar-link">Прогнозы</a>
          <a href="#" className="sidebar-link">Аналитика и риски <span className="sidebar-link-badge">7</span></a>
          <div className="sidebar-section-title">Система</div>
          <a href="#" className="sidebar-link" onClick={onLogout} style={{ color: '#DC2626' }}>Выйти из системы</a>
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="sidebar-avatar">ИИ</div>
            <div className="sidebar-user-info">
              <div className="sidebar-user-name">{localStorage.getItem('username') || 'Оператор'}</div>
              <div className="sidebar-user-role">{localStorage.getItem('role')}</div>
            </div>
          </div>
        </div>
      </aside>

      {/* MAIN */}
      <div className="main">
        <header className="topbar">
          <div className="topbar-left">
            <div className="topbar-titles">
              <div className="topbar-title">Главная (Дашборд)</div>
              <div className="topbar-sub">{data.campaignName} ({data.campaignYear})</div>
            </div>
          </div>
        </header>

        <main className="page">
          {/* KPI CARDS */}
          <div className="kpi-grid">
            <div className="kpi-card">
              <div className="kpi-label">Всего заявлений</div>
              <div className="kpi-value">{kpi.totalApplicants}</div>
              <div className="kpi-sub">активных анкет в базе</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-label">Конкурс на место</div>
              <div className="kpi-value">{kpi.competitionPerPlace}</div>
              <div className="kpi-sub">заявлений на 1 место</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-label">Средний балл</div>
              <div className="kpi-value">{kpi.averageScore}</div>
              <div className="kpi-sub">сумма баллов ЦТ + аттестат</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-label">Заполнение КЦП</div>
              <div className="kpi-value">{kpi.kcpFillPercentage}<span className="unit">%</span></div>
              <div className="kpi-sub"><strong>{kpi.totalApplicants}</strong> из {kpi.totalPlaces} мест</div>
            </div>
            <div className="kpi-card">
              <div className="kpi-label">Рисковые направления</div>
              <div className="kpi-value" style={{ color: '#DC2626' }}>{kpi.riskyDirectionsCount}</div>
              <div className="kpi-sub">требуют внимания</div>
            </div>
          </div>

          {/* TWO COLUMNS: TOP-5 & GEOGRAPHY */}
          <div className="two-col">
            <div className="card">
              <div className="card-head">
                <div className="card-title">Топ-5 популярных направлений</div>
              </div>
              <div className="top-list">
                {top5Directions && top5Directions.map((spec, index) => (
                  <div className="top-item" key={spec.specialtyId}>
                    <div className="top-item-head">
                      <div className="top-item-name">
                        <span className={`top-item-rank ${index === 0 ? 'top1' : ''}`}>{index + 1}</span>
                        {spec.name} ({spec.faculty})
                      </div>
                      <div className="top-item-value">{spec.applicationsCount} заявок</div>
                    </div>
                    <div className="top-bar-bg">
                      <div className="top-bar-fill" style={{ width: `${Math.min(100, (spec.applicationsCount / spec.places) * 30)}%` }}></div>
                    </div>
                    <div className="top-item-sub">
                      <span>Мест: {spec.places}</span>
                      <span>Конкурс: {spec.competition}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <div className="card-head">
                <div className="card-title">География поступающих (Области Беларуси)</div>
              </div>
              <ul style={{ paddingLeft: '20px', lineHeight: '2' }}>
                {geography && Object.entries(geography).map(([region, count]) => (
                  <li key={region}><strong>{region}:</strong> {count} абитуриентов</li>
                ))}
              </ul>
            </div>
          </div>

          {/* RECENT EVENTS / FEED */}
          <div className="card">
            <div className="card-head">
              <div className="card-title">Последние уведомления и риски</div>
            </div>
            <div className="feed-list">
              {recentEvents && recentEvents.map((ev, idx) => (
                <div className="feed-item warning" key={idx}>
                  <div className="feed-body">
                    <div className="feed-text"><strong>{ev.title}</strong>: {ev.message}</div>
                    <div className="feed-time">{ev.type}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
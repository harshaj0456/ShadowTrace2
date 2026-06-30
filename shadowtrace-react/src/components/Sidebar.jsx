import './Sidebar.css'

const NAV = [
  { id: 'dashboard', icon: '⬡', label: 'Overview',       sub: 'Threat summary' },
  { id: 'spread',    icon: '⟳', label: 'Spread Sim',     sub: 'BFS · DSU' },
  { id: 'scoring',   icon: '▲', label: 'Risk Scoring',   sub: 'Heap · Bloom' },
  { id: 'removal',   icon: '✕', label: 'Removal Plan',   sub: "Topo · Tarjan's" },
  { id: 'custom',    icon: '⚙', label: 'Custom Sim',     sub: 'Build topology' },
]

export default function Sidebar({ page, setPage }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        Shadow<span>Trace</span>
        <div className="sidebar-badge">LIVE</div>
      </div>

      <nav className="sidebar-nav">
        {NAV.map(n => (
          <button
            key={n.id}
            className={`nav-item ${page === n.id ? 'active' : ''}`}
            onClick={() => setPage(n.id)}
          >
            <span className="nav-icon">{n.icon}</span>
            <span className="nav-text">
              <span className="nav-label">{n.label}</span>
              <span className="nav-sub">{n.sub}</span>
            </span>
            {page === n.id && <span className="nav-dot" />}
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="footer-status">
          <span className="pulse-dot" />
          <span>Backend :8080</span>
        </div>
        <div className="footer-tag">Project</div>
      </div>
    </aside>
  )
}
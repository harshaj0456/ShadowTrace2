import './StatCard.css'

export default function StatCard({ label, value, color, sub, icon }) {
  return (
    <div className={`stat-card sc-${color}`}>
      <div className="sc-top">
        <span className="sc-label">{label}</span>
        {icon && <span className="sc-icon">{icon}</span>}
      </div>
      <div className={`sc-value text-${color}`}>{value ?? '–'}</div>
      {sub && <div className="sc-sub">{sub}</div>}
    </div>
  )
}
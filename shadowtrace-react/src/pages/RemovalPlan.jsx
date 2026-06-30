import { useEffect, useState } from 'react'
import axios from 'axios'
import './Page.css'
import './RemovalPlan.css'

const API = 'http://localhost:8080/api'

export default function RemovalPlan() {
  const [data, setData]           = useState(null)
  const [executed, setExecuted]   = useState(0)   // how many steps have been executed
  const [secured, setSecured]     = useState(false)
  const [animatingStep, setAnimatingStep] = useState(false)

  useEffect(() => {
    axios.get(`${API}/removal`).then(r => setData(r.data)).catch(() => {})
  }, [])

  const totalSteps = data?.removalSteps?.length || 0

  const executeNext = () => {
    if (executed >= totalSteps || animatingStep) return
    setAnimatingStep(true)
    setTimeout(() => {
      const next = executed + 1
      setExecuted(next)
      setAnimatingStep(false)
      if (next >= totalSteps) {
        setTimeout(() => setSecured(true), 500)
      }
    }, 500)
  }

  const resetPlan = () => {
    setExecuted(0)
    setSecured(false)
  }

  // articulation points get highlighted progressively as steps execute
  const apProgress = totalSteps > 0 ? executed / totalSteps : 0

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Removal Plan</h1>
          <p className="page-sub">Safe backdoor eradication sequence</p>
        </div>
        <div className="algo-chips">
          {["Kahn's Topo Sort", "Tarjan's SCC", 'Articulation Points'].map(a => (
            <span key={a} className="chip">{a}</span>
          ))}
        </div>
      </div>

      {/* ── Success banner ── */}
      {secured && (
        <div className="secured-banner">
          <span className="secured-icon">✓</span>
          <div>
            <div className="secured-title">Network Secured</div>
            <div className="secured-sub">All persistence cycles broken · backdoor eradicated</div>
          </div>
          <button className="secured-reset" onClick={resetPlan}>↺ Replay</button>
        </div>
      )}

      <div className="two-col" style={{ marginBottom: 16 }}>
        {/* ── LEFT: Removal steps ── */}
        <div className="card">
          <div className="card-hd">
            <span className="card-title">Persistence Cycle — Removal Order</span>
            <span className="chip chip-red">TOPO SORT</span>
          </div>

          {data?.cycleDescription && (
            <div className="cycle-desc">{data.cycleDescription}</div>
          )}

          {data?.removalSteps?.map((s, i) => {
            const isDone    = i < executed
            const isCurrent = i === executed
            const isLocked  = i > executed

            return (
              <div
                key={s.step}
                className={`removal-step-v2 ${isDone ? 'step-done' : ''} ${isCurrent ? 'step-current' : ''} ${isLocked ? 'step-locked' : ''}`}
              >
                <div className={`rs-num-v2 ${isDone ? 'num-done' : ''}`}>
                  {isDone ? '✓' : s.step}
                </div>
                <div className="rs-body">
                  <div className="rs-title">{s.title}</div>
                  <div className="rs-sub">{s.subtitle}</div>
                </div>
                {isDone && <span className="rs-badge-done">EXECUTED</span>}
              </div>
            )
          })}

          {totalSteps > 0 && !secured && (
            <button
              className={`execute-btn ${animatingStep ? 'executing' : ''}`}
              onClick={executeNext}
              disabled={animatingStep || executed >= totalSteps}
            >
              {animatingStep
                ? 'Executing…'
                : executed >= totalSteps
                ? 'All steps complete'
                : `▶ Execute Step ${executed + 1}/${totalSteps}`}
            </button>
          )}
        </div>

        {/* ── RIGHT: Articulation points ── */}
        <div className="card">
          <div className="card-hd">
            <span className="card-title">Articulation Points</span>
            <span className="chip chip-amber">TARJAN'S SCC</span>
          </div>
          <div className="art-intro">
            Removing these nodes breaks the persistence chain entirely.
          </div>

          {data?.articulationPoints?.map((ap, i) => {
            const revealed = apProgress > i / (data.articulationPoints.length || 1)
            return (
              <div key={i} className={`art-item-v2 ${revealed ? 'art-revealed' : 'art-dim'}`}>
                <div className={`art-dot-v2 ${revealed ? 'dot-active' : ''}`} />
                <div>
                  <div className="art-name">{ap.name}</div>
                  <div className="art-note">{ap.note}</div>
                </div>
                {revealed && <span className="art-check">✓ isolated</span>}
              </div>
            )
          })}

          {data?.entryPointNote && (
            <div className="entry-point-box">
              <div className="ep-label">Entry Point (Betweenness Centrality)</div>
              <div className="ep-value">{data.entryPointNote}</div>
            </div>
          )}
        </div>
      </div>

      {/* ── MITM section ── */}
      <div className="card">
        <div className="card-hd">
          <span className="card-title">MITM Detection — Dijkstra Path Analysis</span>
          <span className="chip chip-blue">ARP SPOOF</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
          {data?.mitmPaths?.map((path, idx) => (
            <div key={idx}>
              <div className="mitm-label" style={{ color: idx === 0 ? 'var(--green)' : 'var(--red)' }}>
                {path.label}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                {path.nodes.map((n, i) => {
                  const isIntercept = n.includes('Intercept')
                  return (
                    <span key={n} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <span
                        className="mitm-node"
                        style={{
                          background: idx === 0 ? 'var(--green-dim)' : isIntercept ? 'var(--red-dim)' : 'var(--surface2)',
                          color: idx === 0 ? 'var(--green)' : isIntercept ? 'var(--red)' : 'var(--text)',
                          border: `1px solid ${idx === 0 ? 'rgba(22,163,74,0.2)' : isIntercept ? 'rgba(229,55,58,0.2)' : 'var(--border)'}`,
                        }}
                      >
                        {n}
                      </span>
                      {i < path.nodes.length - 1 && (
                        <span
                          className={idx !== 0 ? 'mitm-arrow-blink' : ''}
                          style={{ color: idx === 0 ? 'var(--green)' : 'var(--red)', fontSize: 16 }}
                        >
                          →
                        </span>
                      )}
                    </span>
                  )
                })}
              </div>
              {path.arpNote && <div className="arp-note">{path.arpNote}</div>}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
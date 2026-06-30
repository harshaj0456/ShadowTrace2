import { useEffect, useState, useRef, useCallback } from 'react'
import axios from 'axios'
import './Page.css'
import './SpreadSim.css'

const API = 'http://localhost:8080/api'

// ── tiny Web Audio ping ──
function playPing(type = 'infect') {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.connect(gain); gain.connect(ctx.destination)
    if (type === 'infect') {
      osc.frequency.setValueAtTime(520, ctx.currentTime)
      osc.frequency.exponentialRampToValueAtTime(280, ctx.currentTime + 0.18)
      gain.gain.setValueAtTime(0.12, ctx.currentTime)
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.22)
    } else if (type === 'done') {
      osc.frequency.setValueAtTime(440, ctx.currentTime)
      osc.frequency.setValueAtTime(554, ctx.currentTime + 0.1)
      osc.frequency.setValueAtTime(659, ctx.currentTime + 0.2)
      gain.gain.setValueAtTime(0.1, ctx.currentTime)
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4)
    }
    osc.start(); osc.stop(ctx.currentTime + 0.45)
  } catch {}
}

export default function SpreadSim() {
  const [data, setData]         = useState(null)
  const [step, setStep]         = useState(-1)       // -1 = not started
  const [playing, setPlaying]   = useState(false)
  const [muted, setMuted]       = useState(false)
  const [done, setDone]         = useState(false)
  const [pathVisible, setPathVisible] = useState(false)
  const intervalRef = useRef(null)

  useEffect(() => {
    axios.get(`${API}/spread`).then(r => setData(r.data)).catch(() => {})
  }, [])

  const timeline = data?.timeline || []
  const totalSteps = timeline.length

  // ── play one step forward ──
  const advance = useCallback(() => {
    setStep(prev => {
      const next = prev + 1
      if (next >= totalSteps) {
        setPlaying(false)
        setDone(true)
        setTimeout(() => setPathVisible(true), 400)
        if (!muted) playPing('done')
        return totalSteps - 1
      }
      if (!muted) playPing('infect')
      return next
    })
  }, [totalSteps, muted])

  // ── auto-advance when playing ──
  useEffect(() => {
    if (playing) {
      intervalRef.current = setInterval(advance, 700)
    } else {
      clearInterval(intervalRef.current)
    }
    return () => clearInterval(intervalRef.current)
  }, [playing, advance])

  const handlePlay = () => {
    if (done) return
    if (step === -1) setStep(0)
    setPlaying(true)
  }

  const handlePause = () => setPlaying(false)

  const handleReset = () => {
    setPlaying(false)
    setStep(-1)
    setDone(false)
    setPathVisible(false)
  }

  const handleStepForward = () => {
    if (!playing && !done) advance()
  }

  // current step description
  const currentEvent = step >= 0 ? timeline[step] : null
  const stepDesc = currentEvent
    ? `Step ${step + 1}/${totalSteps} — Node ${currentEvent.nodeId} (${currentEvent.nodeName}) infected at T=${currentEvent.time}`
    : step === -1
    ? 'Press Play to start BFS simulation'
    : 'Simulation complete'

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Spread Simulation</h1>
          <p className="page-sub">Backdoor propagation from entry node</p>
        </div>
        <div className="algo-chips">
          {['Trust-Weighted BFS', 'Dijkstra (fastest path)', 'Union-Find DSU'].map(a => (
            <span key={a} className="chip">{a}</span>
          ))}
        </div>
      </div>

      {/* ── BFS ANIMATION CARD ── */}
      <div className="card" style={{ marginBottom: 16 }}>
        <div className="card-hd">
          <span className="card-title">
            BFS Infection Timeline {data?.entryNodeId ? `— entry: Node ${data.entryNodeId}` : ''}
          </span>
          <span className="chip chip-red">BFS</span>
        </div>

        {/* controls */}
        <div className="bfs-controls">
          <div className="bfs-btns">
            {!playing ? (
              <button className="bfs-btn bfs-play" onClick={handlePlay} disabled={done}>
                ▶ {step === -1 ? 'Play' : 'Resume'}
              </button>
            ) : (
              <button className="bfs-btn bfs-pause" onClick={handlePause}>
                ⏸ Pause
              </button>
            )}
            <button className="bfs-btn bfs-step" onClick={handleStepForward} disabled={playing || done}>
              ⏭ Step
            </button>
            <button className="bfs-btn bfs-reset" onClick={handleReset}>
              ↺ Reset
            </button>
            <button className="bfs-btn bfs-mute" onClick={() => setMuted(m => !m)}>
              {muted ? '🔇' : '🔊'}
            </button>
          </div>

          {/* step description */}
          <div className={`bfs-step-desc ${done ? 'desc-done' : currentEvent ? 'desc-active' : ''}`}>
            {done ? '✓ Simulation complete — all reachable nodes infected' : stepDesc}
          </div>
        </div>

        {/* progress bar */}
        <div className="bfs-progress-wrap">
          <div
            className="bfs-progress-bar"
            style={{ width: totalSteps > 0 ? `${((step + 1) / totalSteps) * 100}%` : '0%' }}
          />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: 'var(--muted)', fontFamily: 'var(--mono)', marginBottom: 16 }}>
          <span>0</span>
          <span>{step + 1}/{totalSteps} nodes</span>
          <span>{totalSteps}</span>
        </div>

        {/* timeline rows */}
        <div className="bfs-timeline">
          {timeline.map((e, i) => {
            const revealed  = step >= i
            const isCurrent = step === i
            return (
              <div key={e.nodeId} className={`bfs-row ${revealed ? 'bfs-revealed' : 'bfs-hidden'} ${isCurrent ? 'bfs-current' : ''}`}>
                <div className="bfs-step-num">{i + 1}</div>
                <div className="bfs-node-label">
                  <span className="bfs-node-name">{e.nodeName}</span>
                  <span className="bfs-node-id">Node {e.nodeId}</span>
                </div>
                <div className="bfs-bar-wrap">
                  <div
                    className="bfs-bar"
                    style={{ width: revealed ? `${e.barPercent}%` : '0%' }}
                  />
                </div>
                <div className="bfs-time">T={e.time}</div>
                {isCurrent && <span className="bfs-pulse-badge">INFECTING</span>}
              </div>
            )
          })}
        </div>

        {/* fastest path — appears after done */}
        {data?.fastestPath?.length > 0 && (
          <div className={`bfs-path-section ${pathVisible ? 'path-visible' : 'path-hidden'}`}>
            <div className="card-hd" style={{ marginBottom: 10 }}>
              <span className="card-title">Fastest spread path (Dijkstra)</span>
              <span className="chip chip-blue">HIGH-TRUST ROUTE</span>
            </div>
            <div className="path-row">
              {data.fastestPath.map((id, i) => (
                <span key={id} style={{ display: 'flex', alignItems: 'center', gap: 8,
                  animationDelay: `${i * 0.15}s` }} className="path-item-anim">
                  <div className={`path-node ${i === data.fastestPath.length - 1 ? 'pn-target' : 'pn-entry'}`}>
                    {id}
                  </div>
                  {i < data.fastestPath.length - 1 && (
                    <span className="path-arr path-arr-animated">→</span>
                  )}
                </span>
              ))}
              <span style={{ fontSize: 11, color: 'var(--muted)', marginLeft: 8 }}>
                · {data.fastestPathNote}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* ── SEGMENTS ── */}
      <div className="card">
        <div className="card-hd">
          <span className="card-title">Network Segments</span>
          <span className="chip chip-green">DSU · Union-Find</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
          {data?.segments?.map((seg, i) => (
            <div key={i}>
              <div style={{ fontFamily: 'var(--mono)', fontSize: 9, letterSpacing: 1, textTransform: 'uppercase', color: 'var(--muted)', marginBottom: 8 }}>
                {seg.label}
              </div>
              {seg.nodes.map(n => (
                <div key={n.id} className="node-row">
                  <span className={`dot ${n.compromised ? 'dot-red' : 'dot-green'}`} />
                  <span className="node-name">{n.name}</span>
                  <span className="node-meta">Node {n.id}</span>
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
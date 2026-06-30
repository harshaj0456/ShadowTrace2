import { useEffect, useRef, useState } from 'react'
import './NetworkGraph.css'

const STATIC_NODES = [
  { id:1, name:'Server_1',  x:0.24, y:0.32, comp:true  },
  { id:2, name:'Server_2',  x:0.10, y:0.55, comp:true  },
  { id:3, name:'WsA',       x:0.27, y:0.72, comp:true  },
  { id:4, name:'WsB',       x:0.40, y:0.78, comp:true  },
  { id:5, name:'Entry',     x:0.38, y:0.48, comp:true, entry:true },
  { id:6, name:'Database',  x:0.60, y:0.28, comp:true, db:true },
  { id:7, name:'Backup',    x:0.13, y:0.80, comp:false },
  { id:8, name:'Isolated',  x:0.73, y:0.72, comp:false },
]

// animation order: DB first, then outward
const EDGES = [
  [6,1,0.9], // DB → Srv1
  [1,2,0.8], // Srv1 → Srv2
  [2,7,0.4], // Srv2 → Backup
  [5,1,0.9], // Entry → Srv1
  [5,3,0.7], // Entry → WsA
  [3,4,0.6], // WsA → WsB
  [7,8,0.2], // Backup → Isolated
]

export default function NetworkGraph({ nodes: liveNodes }) {
  const canvasRef   = useRef(null)
  const stateRef    = useRef({ phase:'idle', edgeProgress:0, nodeReveal:0, hoveredNode:null, selectedNode:null })
  const rafRef      = useRef(null)
  const [tooltip, setTooltip]       = useState(null) // {x,y,node}
  const [selected, setSelected]     = useState(null)
  const [animDone, setAnimDone]     = useState(false)

  const getNodes = () => STATIC_NODES.map(sn => {
    if (liveNodes) {
      const ln = liveNodes.find(l => l.id === sn.id)
      if (ln) return { ...sn, comp: ln.compromised, trust: ln.trust }
    }
    return sn
  })

  const getPos = (n, W, H) => ({ x: n.x * W, y: n.y * H })

  const draw = () => {
    const canvas = canvasRef.current
    if (!canvas) return
    const W = canvas.offsetWidth
    canvas.width = W
    const H = 340
    canvas.height = H
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, W, H)

    const s = stateRef.current
    const allNodes = getNodes()
    const totalEdges = EDGES.length
    // how many edges fully drawn + current partial
    const fullyDrawn = Math.floor(s.edgeProgress)
    const partial    = s.edgeProgress - fullyDrawn

    // ── draw completed edges ──
    for (let i = 0; i < Math.min(fullyDrawn, totalEdges); i++) {
      drawEdge(ctx, EDGES[i], allNodes, W, H, 1, s, selected)
    }
    // ── draw partial edge ──
    if (fullyDrawn < totalEdges && s.phase === 'edges') {
      drawEdge(ctx, EDGES[fullyDrawn], allNodes, W, H, partial, s, selected)
    }
    // ── draw nodes ──
    allNodes.forEach((n, idx) => {
      const revealed = s.phase === 'edges'
        ? idx < 1  // only show first node during edge draw
        : s.nodeReveal > idx
      if (!revealed && s.phase !== 'done') return
      const p = getPos(n, W, H)
      const r  = n.entry ? 22 : n.db ? 22 : 17
      const isSelected = selected?.id === n.id
      const isHovered  = s.hoveredNode?.id === n.id

      // colored phase — use real color
      const showColor = s.phase === 'done' || s.phase === 'color'
      const compColor  = 'rgba(229,55,58'
      const cleanColor = 'rgba(22,163,74'
      const grayColor  = 'rgba(150,160,180'

      const baseColor = showColor
        ? (n.comp ? compColor : cleanColor)
        : grayColor

      // glow for selected/hovered
      if (isSelected || isHovered) {
        ctx.beginPath()
        ctx.arc(p.x, p.y, r + 10, 0, Math.PI*2)
        ctx.fillStyle = `${baseColor},0.08)`
        ctx.fill()
      }

      // pulse ring for compromised (done phase)
      if (s.phase === 'done' && n.comp) {
        const pulse = (Date.now() % 2000) / 2000
        ctx.beginPath()
        ctx.arc(p.x, p.y, r + 6 + pulse * 8, 0, Math.PI*2)
        ctx.strokeStyle = `rgba(229,55,58,${0.35 - pulse * 0.35})`
        ctx.lineWidth = 1.5
        ctx.stroke()
      }

      // main circle
      ctx.beginPath()
      ctx.arc(p.x, p.y, r, 0, Math.PI*2)
      ctx.fillStyle = `${baseColor},0.08)`
      ctx.fill()
      ctx.strokeStyle = isSelected
        ? (n.comp ? '#e5373a' : '#16a34a')
        : `${baseColor},${showColor ? 0.85 : 0.4})`
      ctx.lineWidth = isSelected ? 2.5 : 1.5
      ctx.stroke()

      // label
      ctx.fillStyle = showColor
        ? `${baseColor},0.9)`
        : 'rgba(150,160,180,0.7)'
      ctx.font = `bold 11px Inter, sans-serif`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText(n.name, p.x, p.y - 2)
      ctx.font = '9px JetBrains Mono, monospace'
      ctx.fillStyle = `${baseColor},0.45)`
      ctx.fillText(`N${n.id}`, p.x, p.y + 9)
    })

    // ── connection highlight for selected node ──
    if (selected && s.phase === 'done') {
      EDGES.forEach(([a, b, w]) => {
        if (a !== selected.id && b !== selected.id) return
        const na = getPos(allNodes[a-1], W, H)
        const nb = getPos(allNodes[b-1], W, H)
        ctx.beginPath()
        ctx.moveTo(na.x, na.y)
        ctx.lineTo(nb.x, nb.y)
        ctx.strokeStyle = 'rgba(37,99,235,0.7)'
        ctx.lineWidth = 2.5
        ctx.setLineDash([])
        ctx.stroke()
      })
    }
  }

  function drawEdge(ctx, [a, b, w], allNodes, W, H, progress, s, selected) {
    const na = getPos(allNodes[a-1], W, H)
    const nb = getPos(allNodes[b-1], W, H)
    const ex = na.x + (nb.x - na.x) * progress
    const ey = na.y + (nb.y - na.y) * progress
    const showColor = s.phase === 'done' || s.phase === 'color'
    const comp = allNodes[a-1].comp && allNodes[b-1].comp

    ctx.beginPath()
    ctx.moveTo(na.x, na.y)
    ctx.lineTo(ex, ey)
    ctx.strokeStyle = showColor
      ? (comp ? `rgba(229,55,58,${0.25 + w*0.4})` : 'rgba(150,160,180,0.25)')
      : 'rgba(37,99,235,0.5)'
    ctx.lineWidth = w <= 0.4 ? 1 : w * 2.2
    ctx.setLineDash(w <= 0.4 ? [4,4] : [])
    ctx.stroke()
    ctx.setLineDash([])

    // weight label (only when fully drawn)
    if (progress >= 1) {
      ctx.fillStyle = 'rgba(100,110,130,0.5)'
      ctx.font = '9px JetBrains Mono, monospace'
      ctx.textAlign = 'center'
      ctx.fillText(w, (na.x+nb.x)/2, (na.y+nb.y)/2 - 6)
    }
  }

  // ── animation loop ──
  useEffect(() => {
    const s = stateRef.current
    s.phase = 'edges'
    s.edgeProgress = 0
    s.nodeReveal = 0

    let lastTime = null
    const EDGE_SPEED   = 0.0028  // edges per ms
    const NODE_SPEED   = 0.006   // nodes per ms

    const loop = (ts) => {
      if (!lastTime) lastTime = ts
      const dt = ts - lastTime
      lastTime = ts

      if (s.phase === 'edges') {
        s.edgeProgress = Math.min(s.edgeProgress + dt * EDGE_SPEED, EDGES.length)
        if (s.edgeProgress >= EDGES.length) {
          s.edgeProgress = EDGES.length
          s.phase = 'nodes'
          s.nodeReveal = 0
        }
      } else if (s.phase === 'nodes') {
        s.nodeReveal = Math.min(s.nodeReveal + dt * NODE_SPEED, STATIC_NODES.length)
        if (s.nodeReveal >= STATIC_NODES.length) {
          s.nodeReveal = STATIC_NODES.length
          s.phase = 'color'
          setTimeout(() => {
            s.phase = 'done'
            setAnimDone(true)
          }, 400)
        }
      } else if (s.phase === 'done') {
        // keep looping for pulse animation
      }

      draw()
      rafRef.current = requestAnimationFrame(loop)
    }

    rafRef.current = requestAnimationFrame(loop)
    return () => cancelAnimationFrame(rafRef.current)
  }, [liveNodes])

  // ── mouse interaction ──
  const handleMouseMove = (e) => {
    if (!animDone) return
    const canvas = canvasRef.current
    const rect   = canvas.getBoundingClientRect()
    const mx     = e.clientX - rect.left
    const my     = e.clientY - rect.top
    const W      = canvas.offsetWidth
    const H      = 340
    const allNodes = getNodes()

    let hovered = null
    for (const n of allNodes) {
      const p = getPos(n, W, H)
      const r = n.entry || n.db ? 22 : 17
      const dist = Math.sqrt((mx-p.x)**2 + (my-p.y)**2)
      if (dist < r + 6) { hovered = n; break }
    }

    stateRef.current.hoveredNode = hovered
    if (hovered) {
      // find connections
      const conns = EDGES
        .filter(([a,b]) => a === hovered.id || b === hovered.id)
        .map(([a,b,w]) => {
          const other = a === hovered.id ? allNodes[b-1] : allNodes[a-1]
          return `${other.name} (${w})`
        })
      setTooltip({ x: e.clientX - rect.left, y: e.clientY - rect.top, node: hovered, conns })
      canvas.style.cursor = 'pointer'
    } else {
      setTooltip(null)
      canvas.style.cursor = 'default'
    }
  }

  const handleClick = (e) => {
    if (!animDone) return
    const canvas  = canvasRef.current
    const rect    = canvas.getBoundingClientRect()
    const mx      = e.clientX - rect.left
    const my      = e.clientY - rect.top
    const W       = canvas.offsetWidth
    const H       = 340
    const allNodes = getNodes()

    for (const n of allNodes) {
      const p    = getPos(n, W, H)
      const r    = n.entry || n.db ? 22 : 17
      const dist = Math.sqrt((mx-p.x)**2 + (my-p.y)**2)
      if (dist < r + 6) {
        setSelected(prev => prev?.id === n.id ? null : n)
        return
      }
    }
    setSelected(null)
  }

  const handleMouseLeave = () => {
    stateRef.current.hoveredNode = null
    setTooltip(null)
  }

  return (
    <div className="graph-wrap">
      <canvas
        ref={canvasRef}
        className="network-canvas"
        onMouseMove={handleMouseMove}
        onClick={handleClick}
        onMouseLeave={handleMouseLeave}
      />

      {/* Tooltip on hover */}
      {tooltip && (
        <div className="graph-tooltip" style={{ left: tooltip.x + 14, top: tooltip.y - 10 }}>
          <div className="gt-name">{tooltip.node.name}</div>
          <div className="gt-row">
            <span className="gt-label">Status</span>
            <span style={{ color: tooltip.node.comp ? 'var(--red)' : 'var(--green)' }}>
              {tooltip.node.comp ? 'compromised' : 'clean'}
            </span>
          </div>
          {tooltip.node.trust && (
            <div className="gt-row">
              <span className="gt-label">Trust</span>
              <span>{tooltip.node.trust}</span>
            </div>
          )}
          <div className="gt-row" style={{ marginTop: 6, flexDirection:'column', gap:2 }}>
            <span className="gt-label">Connected to</span>
            {tooltip.conns.map(c => <span key={c} className="gt-conn">{c}</span>)}
          </div>
        </div>
      )}

      {/* Selected node panel */}
      {selected && (
        <div className="graph-selected">
          <div className="gs-header">
            <span className={`dot ${selected.comp ? 'dot-red' : 'dot-green'}`} style={{width:8,height:8,borderRadius:'50%',display:'inline-block'}}/>
            <span className="gs-name">{selected.name}</span>
            <button className="gs-close" onClick={() => setSelected(null)}>✕</button>
          </div>
          <div className="gs-row"><span className="gs-label">Node ID</span><span>N{selected.id}</span></div>
          <div className="gs-row"><span className="gs-label">Status</span>
            <span style={{color: selected.comp ? 'var(--red)':'var(--green)', fontWeight:600}}>
              {selected.comp ? 'Compromised':'Clean'}
            </span>
          </div>
          <div className="gs-row"><span className="gs-label">Role</span>
            <span>{selected.db ? 'Database' : selected.entry ? 'Entry Point' : 'Network Node'}</span>
          </div>
          <div className="gs-connections">
            <div className="gs-label" style={{marginBottom:6}}>Connections</div>
            {EDGES.filter(([a,b]) => a===selected.id||b===selected.id).map(([a,b,w]) => {
              const allNodes = getNodes()
              const other = a===selected.id ? allNodes[b-1] : allNodes[a-1]
              return (
                <div key={`${a}-${b}`} className="gs-conn-row">
                  <span className={`dot ${other.comp?'dot-red':'dot-green'}`} style={{width:6,height:6,borderRadius:'50%',display:'inline-block',flexShrink:0}}/>
                  <span>{other.name}</span>
                  <span className="gs-weight">weight {w}</span>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {!animDone && (
        <div className="graph-booting">
          <span className="boot-dot" /><span className="boot-dot" /><span className="boot-dot" />
          <span style={{marginLeft:8, fontSize:11, color:'var(--muted)', fontFamily:'var(--mono)'}}>
            mapping network topology…
          </span>
        </div>
      )}
    </div>
  )
}
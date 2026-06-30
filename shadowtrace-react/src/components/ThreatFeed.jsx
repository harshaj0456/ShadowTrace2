import { useEffect, useState } from 'react'
import './ThreatFeed.css'

const TYPE_META = {
  PERSISTENCE_CYCLE: { label: 'PERSIST CYCLE', color: 'red' },
  SPREAD:            { label: 'SPREAD',         color: 'amber' },
  MITM:              { label: 'MITM',           color: 'blue' },
}

const LIVE_EVENTS = [
  { type: 'MITM',              description: 'ARP spoof attempt on subnet 192.168.1.x',         score: 61 },
  { type: 'SPREAD',            description: 'Backdoor beacon from Workstation_A to Server_2',   score: 68 },
  { type: 'PERSISTENCE_CYCLE', description: 'Registry key reinstall loop detected on Entry_Node', score: 79 },
  { type: 'MITM',              description: 'Rogue DHCP response intercepted on Node 7',        score: 55 },
  { type: 'SPREAD',            description: 'Lateral movement: Workstation_B → Database',       score: 74 },
  { type: 'PERSISTENCE_CYCLE', description: 'Scheduled task re-creation on Proc_C',             score: 83 },
  { type: 'MITM',              description: 'SSL stripping attempt detected on Node 3',          score: 59 },
  { type: 'SPREAD',            description: 'New host infected: Backup server compromised',      score: 70 },
]

export default function ThreatFeed({ threats }) {
  const [feed, setFeed] = useState([])
  const [newIds, setNewIds] = useState(new Set())

  // seed with real backend threats first
  useEffect(() => {
    if (threats?.length) setFeed(threats.map((t, i) => ({ ...t, _id: i })))
  }, [threats])

  // inject a live event every 4 seconds
  useEffect(() => {
    let counter = 100
    const interval = setInterval(() => {
      const event = LIVE_EVENTS[Math.floor(Math.random() * LIVE_EVENTS.length)]
      const newItem = { ...event, _id: counter++ }
      setFeed(prev => [newItem, ...prev].slice(0, 12))
      setNewIds(prev => new Set([...prev, newItem._id]))
      setTimeout(() => {
        setNewIds(prev => { const s = new Set(prev); s.delete(newItem._id); return s })
      }, 800)
    }, 4000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">Live Threat Feed</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span className="live-indicator"><span className="live-dot" />LIVE</span>
          <span className="chip chip-red">MAX-HEAP SORTED</span>
        </div>
      </div>
      <div className="threat-list">
        {feed.map((t) => {
          const meta = TYPE_META[t.type] || TYPE_META.MITM
          return (
            <div key={t._id} className={`threat-row ${newIds.has(t._id) ? 'threat-new' : ''}`}>
              <span className={`threat-badge tb-${meta.color}`}>{meta.label}</span>
              <span className="threat-desc">{t.description}</span>
              <span className={`threat-score text-${meta.color}`}>{t.score}</span>
            </div>
          )
        })}
      </div>
    </div>
  )
}
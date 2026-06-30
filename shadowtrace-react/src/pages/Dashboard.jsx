import { useEffect, useState } from "react";
import StatCard from "../components/StatCard";
import NetworkGraph from "../components/NetworkGraph";
import ThreatFeed from "../components/ThreatFeed";
import axios from "axios";
import "./Page.css";
import "./Dashboard.css";

const API = import.meta.env.VITE_API_URL;

function useCountUp(target, duration = 1000) {
  const [val, setVal] = useState(0);
  useEffect(() => {
    if (target == null) return;
    const steps = 40;
    const step = target / steps;
    let current = 0;
    const timer = setInterval(() => {
      current += step;
      if (current >= target) {
        setVal(target);
        clearInterval(timer);
      } else setVal(Math.round(current));
    }, duration / steps);
    return () => clearInterval(timer);
  }, [target]);
  return val;
}

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(false);
  const [scanAge, setScanAge] = useState(0);

  const compromised = useCountUp(data?.compromisedNodes);
  const clean = useCountUp(data?.cleanNodes);
  const threats = useCountUp(data?.threatEvents);
  const riskScore = useCountUp(data?.highestRiskScore);

  useEffect(() => {
    axios
      .get(`${API}/overview`)
      .then((r) => setData(r.data))
      .catch(() => setError(true));
  }, []);

  useEffect(() => {
    const t = setInterval(() => setScanAge((a) => a + 1), 1000);
    return () => clearInterval(t);
  }, []);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Threat Overview</h1>
          <p className="page-sub">
            Real-time network compromise analysis
            <span className="scan-age"> · scanned {scanAge}s ago</span>
          </p>
        </div>
        <div className="algo-chips">
          {["Dijkstra", "BFS", "Max-Heap", "Bloom Filter"].map((a) => (
            <span key={a} className="chip">
              {a}
            </span>
          ))}
        </div>
      </div>

      {error && (
        <div className="error-bar">
          ⚠ Backend offline — start Spring Boot on :8080 to load live data
        </div>
      )}

      <div className="stats-grid">
        <StatCard
          label="Compromised Nodes"
          value={compromised}
          color="red"
          sub="Active threats"
          icon="🔴"
        />
        <StatCard
          label="Clean Nodes"
          value={clean}
          color="green"
          sub="Isolated / safe"
          icon="🟢"
        />
        <StatCard
          label="Threat Events"
          value={threats}
          color="amber"
          sub="Active alerts"
          icon="⚠"
        />
        <StatCard
          label="Highest Risk Score"
          value={riskScore}
          color="red"
          sub="Persistence cycle"
          icon="🔺"
        />
      </div>

      <ThreatFeed threats={data?.threats} />

      <div className="card" style={{ marginTop: 16 }}>
        <div className="card-hd">
          <span className="card-title">Network Topology</span>
          <span className="chip chip-blue">8 nodes · live</span>
        </div>
        <NetworkGraph nodes={data?.nodes} />
      </div>
    </div>
  );
}

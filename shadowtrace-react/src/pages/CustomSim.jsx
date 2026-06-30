import { useState } from "react";
import axios from "axios";
import "./Page.css";

const API = "http://localhost:8080/api";

const PRESETS = {
  office: {
    nodes: [
      { id: 1, name: "Reception_PC", type: "workstation", trust: 0.4 },
      { id: 2, name: "HR_Server", type: "server", trust: 0.8 },
      { id: 3, name: "Finance_DB", type: "database", trust: 0.95 },
      { id: 4, name: "IT_Admin_PC", type: "workstation", trust: 0.85 },
      { id: 5, name: "Guest_WiFi", type: "guest", trust: 0.1 },
      { id: 6, name: "Backup_Server", type: "server", trust: 0.7 },
    ],
    edges: [
      { from: 5, to: 1, weight: 0.5 },
      { from: 1, to: 2, weight: 0.7 },
      { from: 2, to: 3, weight: 0.9 },
      { from: 2, to: 4, weight: 0.8 },
      { from: 4, to: 3, weight: 0.9 },
      { from: 3, to: 6, weight: 0.6 },
    ],
    entry: 5,
  },
  hospital: {
    nodes: [
      { id: 1, name: "Patient_Terminal", type: "workstation", trust: 0.3 },
      { id: 2, name: "Nurse_Station", type: "workstation", trust: 0.6 },
      { id: 3, name: "MRI_Device", type: "iot", trust: 0.5 },
      { id: 4, name: "Patient_DB", type: "database", trust: 0.95 },
      { id: 5, name: "Admin_Server", type: "server", trust: 0.9 },
      { id: 6, name: "Billing_System", type: "server", trust: 0.8 },
      { id: 7, name: "External_Portal", type: "guest", trust: 0.15 },
    ],
    edges: [
      { from: 7, to: 1, weight: 0.4 },
      { from: 1, to: 2, weight: 0.6 },
      { from: 2, to: 4, weight: 0.8 },
      { from: 3, to: 4, weight: 0.7 },
      { from: 4, to: 5, weight: 0.9 },
      { from: 5, to: 6, weight: 0.8 },
    ],
    entry: 7,
  },
  bank: {
    nodes: [
      { id: 1, name: "ATM_Network", type: "iot", trust: 0.4 },
      { id: 2, name: "Core_Banking", type: "server", trust: 0.99 },
      { id: 3, name: "Fraud_Detection", type: "server", trust: 0.9 },
      { id: 4, name: "Customer_DB", type: "database", trust: 0.95 },
      { id: 5, name: "Online_Portal", type: "guest", trust: 0.2 },
      { id: 6, name: "Internal_LAN", type: "router", trust: 0.7 },
    ],
    edges: [
      { from: 5, to: 6, weight: 0.35 },
      { from: 6, to: 1, weight: 0.5 },
      { from: 1, to: 2, weight: 0.8 },
      { from: 6, to: 3, weight: 0.6 },
      { from: 3, to: 4, weight: 0.9 },
      { from: 2, to: 4, weight: 0.95 },
    ],
    entry: 5,
  },
  factory: {
    nodes: [
      { id: 1, name: "PLC_Controller", type: "iot", trust: 0.6 },
      { id: 2, name: "SCADA_Server", type: "server", trust: 0.85 },
      { id: 3, name: "Sensor_Net", type: "iot", trust: 0.3 },
      { id: 4, name: "HMI_Terminal", type: "workstation", trust: 0.5 },
      { id: 5, name: "Corporate_LAN", type: "router", trust: 0.7 },
      { id: 6, name: "Remote_Access", type: "guest", trust: 0.1 },
    ],
    edges: [
      { from: 6, to: 3, weight: 0.4 },
      { from: 3, to: 1, weight: 0.6 },
      { from: 1, to: 2, weight: 0.8 },
      { from: 4, to: 2, weight: 0.7 },
      { from: 5, to: 4, weight: 0.6 },
      { from: 2, to: 5, weight: 0.5 },
    ],
    entry: 6,
  },
};

export default function CustomSim() {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [entry, setEntry] = useState("");
  const [form, setForm] = useState({
    id: "",
    name: "",
    type: "server",
    trust: "",
  });
  const [eform, setEform] = useState({ from: "", to: "", weight: "" });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const addNode = () => {
    const { id, name, type, trust } = form;
    if (!id || !name || trust === "") return;
    if (nodes.find((n) => n.id === parseInt(id))) {
      alert("Node ID already exists");
      return;
    }
    setNodes((prev) => [
      ...prev,
      { id: parseInt(id), name, type, trust: parseFloat(trust) },
    ]);
    setForm((f) => ({ ...f, id: String(parseInt(id) + 1), name: "" }));
  };

  const addEdge = () => {
    const { from, to, weight } = eform;
    if (!from || !to || weight === "") return;
    if (from === to) {
      alert("From and To must differ");
      return;
    }
    setEdges((prev) => [
      ...prev,
      { from: parseInt(from), to: parseInt(to), weight: parseFloat(weight) },
    ]);
  };

  const loadPreset = (name) => {
    const p = PRESETS[name];
    if (!p) return;
    setNodes(p.nodes.map((n) => ({ ...n })));
    setEdges(p.edges.map((e) => ({ ...e })));
    setEntry(String(p.entry));
    setResult(null);
  };

  const runSim = async () => {
    setError("");
    setLoading(true);
    try {
      const r = await axios.post(`${API}/simulate`, {
        nodes,
        edges,
        entryNodeId: parseInt(entry),
      });
      setResult(r.data);
    } catch {
      setError("Backend unavailable. Start Spring Boot on :8080");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Custom Simulation</h1>
          <p className="page-sub">
            Build your own network topology and run live analysis
          </p>
        </div>
        <div className="algo-chips">
          {[
            "BFS Spread",
            "Dijkstra Path",
            "Degree Centrality",
            "Dynamic Risk Score",
          ].map((a) => (
            <span key={a} className="chip">
              {a}
            </span>
          ))}
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
        {/* LEFT — builder */}
        <div>
          <div style={{ marginBottom: 16 }}>
            <div className="card-title" style={{ marginBottom: 10 }}>
              Quick Presets
            </div>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              {[
                ["office", "🏢 Office"],
                ["hospital", "🏥 Hospital"],
                ["bank", "🏦 Bank"],
                ["factory", "🏭 Factory"],
              ].map(([k, l]) => (
                <button
                  key={k}
                  onClick={() => loadPreset(k)}
                  style={{
                    background: "var(--surface2)",
                    border: "1px solid var(--border2)",
                    color: "var(--muted)",
                    borderRadius: 6,
                    padding: "7px 14px",
                    cursor: "pointer",
                    fontSize: 12,
                    transition: "all 0.15s",
                  }}
                >
                  {l}
                </button>
              ))}
            </div>
          </div>

          <div className="card" style={{ marginBottom: 12 }}>
            <div className="card-hd">
              <span className="card-title">1 · Add Nodes</span>
            </div>
            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: 8,
                marginBottom: 10,
                alignItems: "flex-end",
              }}
            >
              {[
                ["id", "ID", "60px", "number"],
                ["name", "Name", "130px", "text"],
              ].map(([k, l, w, t]) => (
                <div
                  key={k}
                  style={{ display: "flex", flexDirection: "column", gap: 3 }}
                >
                  <label style={{ fontSize: 10, color: "var(--muted)" }}>
                    {l}
                  </label>
                  <input
                    type={t}
                    value={form[k]}
                    onChange={(e) =>
                      setForm((f) => ({ ...f, [k]: e.target.value }))
                    }
                    style={{
                      width: w,
                      background: "var(--surface2)",
                      border: "1px solid var(--border2)",
                      color: "var(--text)",
                      borderRadius: 6,
                      padding: "7px 10px",
                      fontFamily: "var(--mono)",
                      fontSize: 12,
                      outline: "none",
                    }}
                  />
                </div>
              ))}
              <div style={{ display: "flex", flexDirection: "column", gap: 3 }}>
                <label style={{ fontSize: 10, color: "var(--muted)" }}>
                  Type
                </label>
                <select
                  value={form.type}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, type: e.target.value }))
                  }
                  style={{
                    background: "var(--surface2)",
                    border: "1px solid var(--border2)",
                    color: "var(--text)",
                    borderRadius: 6,
                    padding: "7px 10px",
                    fontFamily: "var(--mono)",
                    fontSize: 12,
                    outline: "none",
                  }}
                >
                  {[
                    "server",
                    "workstation",
                    "database",
                    "router",
                    "iot",
                    "guest",
                  ].map((t) => (
                    <option key={t}>{t}</option>
                  ))}
                </select>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 3 }}>
                <label style={{ fontSize: 10, color: "var(--muted)" }}>
                  Trust
                </label>
                <input
                  type="number"
                  value={form.trust}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, trust: e.target.value }))
                  }
                  min="0"
                  max="1"
                  step="0.1"
                  style={{
                    width: 70,
                    background: "var(--surface2)",
                    border: "1px solid var(--border2)",
                    color: "var(--text)",
                    borderRadius: 6,
                    padding: "7px 10px",
                    fontFamily: "var(--mono)",
                    fontSize: 12,
                    outline: "none",
                  }}
                />
              </div>
              <button
                onClick={addNode}
                style={{
                  background: "var(--blue-dim)",
                  border: "1px solid rgba(68,147,248,0.3)",
                  color: "var(--blue)",
                  borderRadius: 6,
                  padding: "8px 14px",
                  cursor: "pointer",
                  fontFamily: "var(--mono)",
                  fontWeight: 700,
                  fontSize: 12,
                }}
              >
                + Add
              </button>
            </div>
            <table
              style={{
                width: "100%",
                borderCollapse: "collapse",
                fontSize: 12,
              }}
            >
              <thead>
                <tr>
                  {["ID", "Name", "Type", "Trust", ""].map((h) => (
                    <th
                      key={h}
                      style={{
                        textAlign: "left",
                        padding: "6px 8px",
                        fontFamily: "var(--mono)",
                        fontSize: 9,
                        letterSpacing: 1,
                        color: "var(--muted)",
                        borderBottom: "1px solid var(--border)",
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {nodes.map((n, i) => (
                  <tr key={n.id}>
                    {[n.id, n.name, n.type, n.trust].map((v, j) => (
                      <td
                        key={j}
                        style={{
                          padding: "7px 8px",
                          color: "#c9d1d9",
                          fontFamily: "var(--mono)",
                          fontSize: 11,
                          borderBottom: "1px solid rgba(255,255,255,0.03)",
                        }}
                      >
                        {v}
                      </td>
                    ))}
                    <td
                      style={{
                        padding: "7px 8px",
                        borderBottom: "1px solid rgba(255,255,255,0.03)",
                      }}
                    >
                      <button
                        onClick={() =>
                          setNodes((n) => n.filter((_, j) => j !== i))
                        }
                        style={{
                          background: "none",
                          border: "none",
                          color: "var(--muted)",
                          cursor: "pointer",
                          fontSize: 13,
                        }}
                      >
                        ✕
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card" style={{ marginBottom: 12 }}>
            <div className="card-hd">
              <span className="card-title">2 · Add Edges</span>
            </div>
            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: 8,
                marginBottom: 10,
                alignItems: "flex-end",
              }}
            >
              {[
                ["from", "From", "70px"],
                ["to", "To", "70px"],
                ["weight", "Weight", "70px"],
              ].map(([k, l, w]) => (
                <div
                  key={k}
                  style={{ display: "flex", flexDirection: "column", gap: 3 }}
                >
                  <label style={{ fontSize: 10, color: "var(--muted)" }}>
                    {l}
                  </label>
                  <input
                    type="number"
                    value={eform[k]}
                    onChange={(e) =>
                      setEform((f) => ({ ...f, [k]: e.target.value }))
                    }
                    step={k === "weight" ? 0.1 : 1}
                    min={k === "weight" ? 0 : 1}
                    max={k === "weight" ? 1 : undefined}
                    style={{
                      width: w,
                      background: "var(--surface2)",
                      border: "1px solid var(--border2)",
                      color: "var(--text)",
                      borderRadius: 6,
                      padding: "7px 10px",
                      fontFamily: "var(--mono)",
                      fontSize: 12,
                      outline: "none",
                    }}
                  />
                </div>
              ))}
              <button
                onClick={addEdge}
                style={{
                  background: "var(--blue-dim)",
                  border: "1px solid rgba(68,147,248,0.3)",
                  color: "var(--blue)",
                  borderRadius: 6,
                  padding: "8px 14px",
                  cursor: "pointer",
                  fontFamily: "var(--mono)",
                  fontWeight: 700,
                  fontSize: 12,
                }}
              >
                + Add
              </button>
            </div>
            <div
              style={{ fontSize: 11, color: "var(--muted)", marginBottom: 8 }}
            >
              Weight &gt; 0.3 = threat can spread
            </div>
            <table
              style={{
                width: "100%",
                borderCollapse: "collapse",
                fontSize: 12,
              }}
            >
              <thead>
                <tr>
                  {["From", "To", "Weight", ""].map((h) => (
                    <th
                      key={h}
                      style={{
                        textAlign: "left",
                        padding: "6px 8px",
                        fontFamily: "var(--mono)",
                        fontSize: 9,
                        letterSpacing: 1,
                        color: "var(--muted)",
                        borderBottom: "1px solid var(--border)",
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {edges.map((e, i) => (
                  <tr key={i}>
                    {[e.from, e.to, e.weight].map((v, j) => (
                      <td
                        key={j}
                        style={{
                          padding: "7px 8px",
                          color: "#c9d1d9",
                          fontFamily: "var(--mono)",
                          fontSize: 11,
                          borderBottom: "1px solid rgba(255,255,255,0.03)",
                        }}
                      >
                        {v}
                      </td>
                    ))}
                    <td
                      style={{
                        padding: "7px 8px",
                        borderBottom: "1px solid rgba(255,255,255,0.03)",
                      }}
                    >
                      <button
                        onClick={() =>
                          setEdges((e) => e.filter((_, j) => j !== i))
                        }
                        style={{
                          background: "none",
                          border: "none",
                          color: "var(--muted)",
                          cursor: "pointer",
                          fontSize: 13,
                        }}
                      >
                        ✕
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card">
            <div className="card-hd">
              <span className="card-title">3 · Entry Point</span>
            </div>
            <select
              value={entry}
              onChange={(e) => setEntry(e.target.value)}
              style={{
                background: "var(--surface2)",
                border: "1px solid var(--border2)",
                color: "var(--text)",
                borderRadius: 6,
                padding: "8px 12px",
                fontFamily: "var(--mono)",
                fontSize: 12,
                outline: "none",
                minWidth: 180,
                marginBottom: 16,
              }}
            >
              <option value="">— select entry node —</option>
              {nodes.map((n) => (
                <option key={n.id} value={n.id}>
                  {n.id} · {n.name}
                </option>
              ))}
            </select>
            <button
              onClick={runSim}
              disabled={nodes.length < 2 || !entry || loading}
              style={{
                width: "100%",
                background:
                  nodes.length < 2 || !entry ? "var(--surface3)" : "var(--red)",
                border: "none",
                color: nodes.length < 2 || !entry ? "var(--muted)" : "#fff",
                borderRadius: 8,
                padding: "12px",
                cursor: nodes.length < 2 || !entry ? "not-allowed" : "pointer",
                fontFamily: "var(--mono)",
                fontSize: 13,
                fontWeight: 700,
                transition: "all 0.15s",
              }}
            >
              {loading ? "⏳ Running…" : "▶ Run Simulation"}
            </button>
            {error && (
              <div
                style={{
                  fontFamily: "var(--mono)",
                  fontSize: 11,
                  padding: "10px",
                  background: "var(--red-dim)",
                  color: "var(--red)",
                  borderRadius: 6,
                  marginTop: 10,
                }}
              >
                {error}
              </div>
            )}
          </div>
        </div>

        {/* RIGHT — results */}
        <div>
          {!result ? (
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                height: "100%",
                color: "var(--muted)",
                fontFamily: "var(--mono)",
                fontSize: 12,
                textAlign: "center",
                gap: 8,
              }}
            >
              <div style={{ fontSize: 32, opacity: 0.2 }}>⟳</div>
              <div>
                Add nodes and edges,
                <br />
                then click Run Simulation
              </div>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
              <div
                style={{
                  display: "flex",
                  gap: 12,
                  padding: 16,
                  background: "var(--surface2)",
                  borderRadius: 10,
                  border: "1px solid var(--border)",
                  flexWrap: "wrap",
                }}
              >
                {[
                  ["Compromised", result.compromisedNodes, "red"],
                  ["Clean", result.cleanNodes, "green"],
                  ["Risk Score", result.riskScore, "amber"],
                ].map(([l, v, c]) => (
                  <div key={l} style={{ textAlign: "center", flex: 1 }}>
                    <div
                      style={{
                        fontFamily: "var(--mono)",
                        fontSize: 28,
                        fontWeight: 700,
                        color: `var(--${c})`,
                      }}
                    >
                      {v}
                    </div>
                    <div
                      style={{
                        fontSize: 10,
                        color: "var(--muted)",
                        letterSpacing: 0.5,
                        marginTop: 2,
                      }}
                    >
                      {l.toUpperCase()}
                    </div>
                  </div>
                ))}
                <div
                  style={{
                    padding: "8px 14px",
                    borderRadius: 8,
                    background: result.highTrustHit
                      ? "var(--red-dim)"
                      : "var(--green-dim)",
                    color: result.highTrustHit ? "var(--red)" : "var(--green)",
                    border: `1px solid ${result.highTrustHit ? "rgba(240,71,71,0.2)" : "rgba(63,185,80,0.2)"}`,
                    fontSize: 12,
                    flex: 2,
                    display: "flex",
                    alignItems: "center",
                  }}
                >
                  {result.threatDescription}
                </div>
              </div>

              <div className="card">
                <div className="card-hd">
                  <span className="card-title">Infection Timeline</span>
                </div>
                {result.timeline?.map((e) => (
                  <div key={e.nodeId} className="tl-row">
                    <div className="tl-label">
                      Node {e.nodeId} — {e.nodeName}
                    </div>
                    <div className="tl-bar-wrap">
                      <div
                        className="tl-bar"
                        style={{ width: `${e.barPercent}%` }}
                      />
                    </div>
                    <div className="tl-t">T={e.time}</div>
                  </div>
                ))}
              </div>

              <div className="card">
                <div className="card-hd">
                  <span className="card-title">Node Status</span>
                </div>
                {result.nodes?.map((n) => (
                  <div key={n.id} className="node-row">
                    <span
                      className={`dot ${n.compromised ? "dot-red" : "dot-green"}`}
                    />
                    <span className="node-name">{n.name}</span>
                    <span className="node-meta">trust {n.trust}</span>
                    <span
                      className={`node-status ${n.compromised ? "status-red" : "status-green"}`}
                    >
                      {n.compromised ? "compromised" : "clean"}
                    </span>
                  </div>
                ))}
              </div>

              {result.articulationPoints?.length > 0 && (
                <div className="card">
                  <div className="card-hd">
                    <span className="card-title">Critical Nodes to Remove</span>
                  </div>
                  {result.articulationPoints.map((ap, i) => (
                    <div key={i} className="art-item">
                      <div className="art-dot" />
                      <div>
                        <div className="art-name">{ap.name}</div>
                        <div className="art-note">{ap.note}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

import { useEffect, useState, useRef } from "react";
import axios from "axios";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";
import "./Page.css";
import "./RiskScoring.css";

const API = import.meta.env.VITE_API_URL;
const BSIZE = 32;
const h1 = (x) => Math.abs((x * 2654435761) % BSIZE);
const h2 = (x) => Math.abs((x * 1234567891) % BSIZE);

// animated bar width hook — grows from 0 to target % on mount
function useAnimatedWidth(target, delay = 0) {
  const [width, setWidth] = useState(0);
  useEffect(() => {
    if (!target) return;
    const t = setTimeout(() => {
      setWidth(target);
    }, delay);
    return () => clearTimeout(t);
  }, [target]);
  return width;
}

function RiskRow({ t, index }) {
  const width = useAnimatedWidth(t.score, index * 120);
  const tagColor = t.rank === 1 ? "red" : t.rank === 2 ? "amber" : "green";
  const colors = {
    red: {
      bg: "var(--red-dim)",
      text: "var(--red)",
      border: "rgba(229,55,58,0.2)",
    },
    amber: {
      bg: "var(--amber-dim)",
      text: "var(--amber)",
      border: "rgba(217,119,6,0.2)",
    },
    green: {
      bg: "var(--green-dim)",
      text: "var(--green)",
      border: "rgba(22,163,74,0.2)",
    },
  };
  const c = colors[tagColor];

  const tooltips = {
    PERSISTENCE_CYCLE:
      "Backdoor cycle detected — reinstalls itself after removal. Scored by cycle length × trust weight.",
    SPREAD:
      "Lateral movement across nodes. Score = infected nodes × avg trust of traversed edges.",
    MITM: "Man-in-the-middle intercept. Scored by path deviation distance + ARP spoof confidence.",
  };

  return (
    <div
      className="risk-row"
      title={tooltips[t.type] || ""}
      style={{ animationDelay: `${index * 0.1}s` }}
    >
      <span className="risk-rank">{t.rank}</span>
      <div style={{ flex: 1 }}>
        <span
          className="risk-tag"
          style={{
            background: c.bg,
            color: c.text,
            border: `1px solid ${c.border}`,
          }}
        >
          {t.type === "PERSISTENCE_CYCLE" ? "PERSIST CYCLE" : t.type}
        </span>
        <div className="risk-bar-wrap">
          <div
            className="risk-bar"
            style={{ width: `${width}%`, background: t.barColor }}
          />
        </div>
        <div className="risk-tooltip-hint">hover for score explanation</div>
      </div>
      <span className="risk-score" style={{ color: t.barColor }}>
        {t.score}
      </span>
    </div>
  );
}

const CustomTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const d = payload[0];
  return (
    <div className="chart-tooltip">
      <div className="ct-name">{d.payload.name}</div>
      <div className="ct-score" style={{ color: d.payload.color }}>
        Score: {d.value}
      </div>
      <div className="ct-sub">Max-Heap priority rank {d.payload.rank}</div>
    </div>
  );
};

export default function RiskScoring() {
  const [data, setData] = useState(null);
  const [bits, setBits] = useState(new Array(BSIZE).fill(false));
  const [flashBits, setFlashBits] = useState(new Set());
  const [checkBits, setCheckBits] = useState(new Set());
  const [input, setInput] = useState("");
  const [result, setResult] = useState(null);
  const [fpRate, setFpRate] = useState(0);
  const [animFp, setAnimFp] = useState(0);
  const setBitsCount = useRef(0);

  useEffect(() => {
    axios
      .get(`${API}/scoring`)
      .then((r) => {
        setData(r.data);
        if (r.data.preFlaggedNodes) {
          const b = new Array(BSIZE).fill(false);
          r.data.preFlaggedNodes.forEach((id) => {
            b[h1(id)] = true;
            b[h2(id)] = true;
          });
          setBits(b);
          setBitsCount.current = b.filter(Boolean).length;
          updateFpRate(b);
        }
      })
      .catch(() => {});
  }, []);

  // false positive rate = (setBits/total)^k where k=2 hash functions
  const updateFpRate = (b) => {
    const setBitsN = b.filter(Boolean).length;
    const rate = Math.round(Math.pow(setBitsN / BSIZE, 2) * 100);
    setFpRate(rate);
  };

  // animate fp rate bar
  useEffect(() => {
    const t = setTimeout(() => setAnimFp(fpRate), 300);
    return () => clearTimeout(t);
  }, [fpRate]);

  const addNode = () => {
    const v = parseInt(input);
    if (isNaN(v) || v < 1) return;
    const b1 = h1(v),
      b2 = h2(v);
    setBits((prev) => {
      const b = [...prev];
      b[b1] = true;
      b[b2] = true;
      updateFpRate(b);
      return b;
    });
    // flash the newly set bits
    setFlashBits(new Set([b1, b2]));
    setCheckBits(new Set());
    setTimeout(() => setFlashBits(new Set()), 800);
    setResult(null);
  };

  const checkNode = () => {
    const v = parseInt(input);
    if (isNaN(v) || v < 1) return;
    const b1 = h1(v),
      b2 = h2(v);
    const maybe = bits[b1] && bits[b2];
    setCheckBits(new Set([b1, b2]));
    setFlashBits(new Set());
    setTimeout(() => setCheckBits(new Set()), 2000);
    setResult({ v, maybe });
  };

  const getBitStyle = (i) => {
    const isSet = bits[i];
    const isFlash = flashBits.has(i);
    const isCheck = checkBits.has(i);

    if (isFlash)
      return {
        background: "rgba(229,55,58,0.5)",
        border: "1px solid rgba(229,55,58,0.9)",
        boxShadow: "0 0 8px rgba(229,55,58,0.6)",
        transform: "scale(1.3)",
      };
    if (isCheck)
      return {
        background: "rgba(217,119,6,0.5)",
        border: "1px solid rgba(217,119,6,0.9)",
        boxShadow: "0 0 8px rgba(217,119,6,0.5)",
        transform: "scale(1.2)",
      };
    if (isSet)
      return {
        background: "rgba(37,99,235,0.25)",
        border: "1px solid rgba(37,99,235,0.5)",
        boxShadow: "0 0 4px rgba(37,99,235,0.3)",
      };
    return {
      background: "var(--surface3)",
      border: "1px solid var(--border)",
    };
  };

  const chartData = data?.rankedThreats?.map((t, i) => ({
    name: t.type === "PERSISTENCE_CYCLE" ? "Persist" : t.type,
    score: t.score,
    color: t.barColor,
    rank: t.rank,
  }));

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Risk Scoring</h1>
          <p className="page-sub">Threat prioritization and node flagging</p>
        </div>
        <div className="algo-chips">
          {["Max-Heap", "ThreatScorer", "Bloom Filter O(1)"].map((a) => (
            <span key={a} className="chip">
              {a}
            </span>
          ))}
        </div>
      </div>

      <div className="two-col">
        {/* ── LEFT: Risk Heap ── */}
        <div className="card">
          <div className="card-hd">
            <span className="card-title">Risk Heap — highest first</span>
            <span className="chip chip-amber">PRIORITY QUEUE</span>
          </div>

          {data?.rankedThreats?.map((t, i) => (
            <RiskRow key={t.rank} t={t} index={i} />
          ))}

          {chartData && (
            <div style={{ marginTop: 20, height: 160 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={chartData}
                  margin={{ top: 5, right: 5, left: -20, bottom: 5 }}
                >
                  <XAxis
                    dataKey="name"
                    tick={{ fill: "var(--muted)", fontSize: 11 }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <YAxis
                    tick={{ fill: "var(--muted)", fontSize: 11 }}
                    axisLine={false}
                    tickLine={false}
                    domain={[0, 100]}
                  />
                  <Tooltip
                    content={<CustomTooltip />}
                    cursor={{ fill: "rgba(30,40,80,0.04)" }}
                  />
                  <Bar
                    dataKey="score"
                    radius={[4, 4, 0, 0]}
                    isAnimationActive={true}
                    animationBegin={200}
                    animationDuration={800}
                  >
                    {chartData.map((entry, i) => (
                      <Cell key={i} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* ── RIGHT: Bloom Filter ── */}
        <div className="card">
          <div className="card-hd">
            <span className="card-title">Bloom Filter</span>
            <span className="chip chip-blue">O(1) LOOKUP</span>
          </div>

          <div className="bloom-meta">Bit array size 32 · 2 hash functions</div>

          {/* bit grid */}
          <div className="bit-grid">
            {bits.map((b, i) => (
              <div
                key={i}
                title={`bit ${i}${b ? " — set" : ""}`}
                className="bit-cell"
                style={getBitStyle(i)}
              >
                <span className="bit-index">{i}</span>
              </div>
            ))}
          </div>

          {/* legend */}
          <div className="bit-legend">
            <span className="bl-item">
              <span
                className="bl-dot"
                style={{ background: "rgba(37,99,235,0.4)" }}
              />
              set
            </span>
            <span className="bl-item">
              <span
                className="bl-dot"
                style={{ background: "rgba(229,55,58,0.5)" }}
              />
              just added
            </span>
            <span className="bl-item">
              <span
                className="bl-dot"
                style={{ background: "rgba(217,119,6,0.5)" }}
              />
              checking
            </span>
          </div>

          {/* controls */}
          <div className="bloom-controls">
            <input
              type="number"
              value={input}
              onChange={(e) => {
                setInput(e.target.value);
                setResult(null);
              }}
              onKeyDown={(e) => e.key === "Enter" && addNode()}
              placeholder="Node ID"
              className="bloom-input"
            />
            <button onClick={addNode} className="bloom-btn bloom-add">
              Add
            </button>
            <button onClick={checkNode} className="bloom-btn bloom-check">
              Check
            </button>
          </div>

          {/* result */}
          {result && (
            <div
              className={`bloom-result ${result.maybe ? "br-maybe" : "br-clean"}`}
            >
              <span className="br-node">Node {result.v}</span>
              {result.maybe
                ? " → possibly flagged — mightBeFlagged() = true"
                : " → definitely NOT flagged — mightBeFlagged() = false"}
            </div>
          )}

          {/* false positive rate */}
          <div className="fp-section">
            <div className="fp-header">
              <span className="fp-label">False positive rate</span>
              <span className="fp-val">{fpRate}%</span>
            </div>
            <div className="fp-bar-wrap">
              <div
                className="fp-bar"
                style={{
                  width: `${animFp}%`,
                  background:
                    fpRate < 20
                      ? "var(--green)"
                      : fpRate < 50
                        ? "var(--amber)"
                        : "var(--red)",
                }}
              />
            </div>
            <div className="fp-hint">
              {fpRate < 20
                ? "Low — filter is reliable"
                : fpRate < 50
                  ? "Moderate — consider resizing"
                  : "High — filter saturated"}
            </div>
          </div>

          {data?.preFlaggedNodes && (
            <div className="preflagged">
              Pre-flagged:{" "}
              <strong>Nodes {data.preFlaggedNodes.join(", ")}</strong>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

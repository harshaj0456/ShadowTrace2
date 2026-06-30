package com.shadowtrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

import java.util.*;
import java.util.stream.*;

// ──────────────────────────────────────────────────────────────
// Entry point
// ──────────────────────────────────────────────────────────────
@SpringBootApplication
public class ShadowTraceAPI {
    public static void main(String[] args) {
        SpringApplication.run(ShadowTraceAPI.class, args);
    }
}

// ──────────────────────────────────────────────────────────────
// CORS — allow frontend (file:// or localhost) to call the API
// ──────────────────────────────────────────────────────────────
@org.springframework.context.annotation.Configuration
class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}

// ──────────────────────────────────────────────────────────────
// Shared data models
// ──────────────────────────────────────────────────────────────
record ThreatEvent(String type, String description, int score) {}
record NodeStatus(int id, String name, double trust, boolean compromised) {}
record TimelineEntry(int nodeId, String nodeName, int time, int barPercent) {}
record RemovalStep(int step, String title, String subtitle) {}
record ArticulationPoint(String name, String note) {}
record MitmPath(String label, List<String> nodes, String arpNote) {}
record Segment(String label, List<NodeStatus> nodes) {}

// ──────────────────────────────────────────────────────────────
// Input models for /api/simulate
// ──────────────────────────────────────────────────────────────
class SimNode {
    public int id;
    public String name;
    public String type;    // e.g. "server", "workstation", "database"
    public double trust;   // 0.0 – 1.0
}

class SimEdge {
    public int from;
    public int to;
    public double weight;  // 0.0 – 1.0 (connection strength)
}

class SimRequest {
    public List<SimNode> nodes;
    public List<SimEdge> edges;
    public int entryNodeId;
}

// ──────────────────────────────────────────────────────────────
// /api/overview  (hardcoded demo — unchanged)
// ──────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/overview")
class OverviewController {
    @GetMapping
    public Map<String, Object> overview() {
        List<ThreatEvent> threats = List.of(
            new ThreatEvent("PERSISTENCE_CYCLE", "3-node backdoor cycle: Task_A → Key_B → Proc_C", 82),
            new ThreatEvent("SPREAD",            "Backdoor spread to 6 nodes from Entry_Node",      72),
            new ThreatEvent("MITM",              "ARP spoofing on Node 7 · Intercepts A↔B traffic", 57)
        );
        List<NodeStatus> nodes = List.of(
            new NodeStatus(1,"Server_1",     0.9,true),
            new NodeStatus(2,"Server_2",     0.8,true),
            new NodeStatus(3,"Workstation_A",0.7,true),
            new NodeStatus(4,"Workstation_B",0.6,true),
            new NodeStatus(5,"Entry_Node",   0.5,true),
            new NodeStatus(6,"Database",     0.9,true),
            new NodeStatus(7,"Backup",       0.4,false),
            new NodeStatus(8,"Isolated",     0.1,false)
        );
        long compromised = nodes.stream().filter(NodeStatus::compromised).count();
        long clean       = nodes.stream().filter(n -> !n.compromised()).count();
        int  highestRisk = threats.stream().mapToInt(ThreatEvent::score).max().orElse(0);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("compromisedNodes", compromised);
        r.put("cleanNodes",       clean);
        r.put("threatEvents",     threats.size());
        r.put("highestRiskScore", highestRisk);
        r.put("threats",          threats);
        r.put("nodes",            nodes);
        return r;
    }
}

// ──────────────────────────────────────────────────────────────
// /api/spread  (hardcoded demo — unchanged)
// ──────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/spread")
class SpreadController {
    @GetMapping
    public Map<String, Object> spread() {
        List<TimelineEntry> timeline = List.of(
            new TimelineEntry(5,"Entry_Node",    0,100),
            new TimelineEntry(1,"Server_1",      1, 82),
            new TimelineEntry(6,"Database",      2, 65),
            new TimelineEntry(3,"Workstation_A", 2, 55),
            new TimelineEntry(2,"Server_2",      3, 28),
            new TimelineEntry(4,"Workstation_B", 3, 18)
        );
        List<Segment> segments = List.of(
            new Segment("Compromised segment", List.of(
                new NodeStatus(1,"Server_1",     0.9,true),
                new NodeStatus(2,"Server_2",     0.8,true),
                new NodeStatus(3,"Workstation_A",0.7,true),
                new NodeStatus(4,"Workstation_B",0.6,true),
                new NodeStatus(5,"Entry_Node",   0.5,true),
                new NodeStatus(6,"Database",     0.9,true)
            )),
            new Segment("Clean segment", List.of(
                new NodeStatus(7,"Backup",  0.4,false),
                new NodeStatus(8,"Isolated",0.1,false)
            ))
        );
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("entryNodeId",     5);
        r.put("entryNodeName",   "Entry_Node");
        r.put("timeline",        timeline);
        r.put("fastestPath",     List.of(5,1,6));
        r.put("fastestPathNote", "High-trust route to database");
        r.put("segments",        segments);
        return r;
    }
}

// ──────────────────────────────────────────────────────────────
// /api/scoring  (hardcoded demo — unchanged)
// ──────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/scoring")
class ScoringController {
    @GetMapping
    public Map<String, Object> scoring() {
        List<Map<String,Object>> heap = new ArrayList<>();
        Map<String,Object> t1 = new LinkedHashMap<>(); t1.put("rank",1); t1.put("type","PERSISTENCE_CYCLE"); t1.put("score",82); t1.put("barColor","#e84040"); heap.add(t1);
        Map<String,Object> t2 = new LinkedHashMap<>(); t2.put("rank",2); t2.put("type","SPREAD");            t2.put("score",72); t2.put("barColor","#e8a020"); heap.add(t2);
        Map<String,Object> t3 = new LinkedHashMap<>(); t3.put("rank",3); t3.put("type","MITM");              t3.put("score",57); t3.put("barColor","#4caf50"); heap.add(t3);
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("rankedThreats",   heap);
        r.put("preFlaggedNodes", List.of(1,2,3,5,6,12));
        r.put("bloomSize",       32);
        return r;
    }
}

// ──────────────────────────────────────────────────────────────
// /api/removal  (hardcoded demo — unchanged)
// ──────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/removal")
class RemovalController {
    @GetMapping
    public Map<String, Object> removal() {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("cycleDescription", "Cycle: Task_A → Key_B → Proc_C → Task_A · Break at weakest edge (weight 0.3)");
        r.put("removalSteps", List.of(
            new RemovalStep(1,"Remove Proc_C (process)",       "Lowest in-degree after breaking cycle at weakest edge"),
            new RemovalStep(2,"Remove Key_B (registry key)",   "No longer reinstalled — safe to delete"),
            new RemovalStep(3,"Remove Task_A (scheduled task)","All dependencies cleared")
        ));
        r.put("articulationPoints", List.of(
            new ArticulationPoint("Entry_Node (Node 5)","Disconnect severs entry route"),
            new ArticulationPoint("Database (Node 6)",  "Removal isolates DB segment")
        ));
        r.put("entryPointNote", "Node 5 · Highest centrality — attacker entry via Entry_Node");
        r.put("mitmPaths", List.of(
            new MitmPath("Expected path (Dijkstra)",      List.of("Device A","Device B"), null),
            new MitmPath("Observed path (MITM detected)", List.of("Device A","Node 7 (Intercept)","Device B"),
                         "ARP spoof: Node 7 claims IP of Device B · confidence 90.0")
        ));
        return r;
    }
}

// ──────────────────────────────────────────────────────────────
// /api/simulate  — NEW custom topology POST endpoint
//
// Algorithms used:
//   • BFS          → spread simulation from entry node
//   • Dijkstra     → fastest (highest-weight) spread path
//   • Degree centrality heuristic → articulation point detection
//   • Risk scoring → base 40 + 10/node + 15 if high-trust hit
// ──────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/simulate")
class SimulateController {

    @PostMapping
    public Map<String, Object> simulate(@RequestBody SimRequest req) {
        List<SimNode> nodes   = req.nodes   != null ? req.nodes   : List.of();
        List<SimEdge> edges   = req.edges   != null ? req.edges   : List.of();
        int           entryId = req.entryNodeId;

        // ── 1. Build adjacency map (undirected) ──
        Map<Integer, List<SimEdge>> adj = new HashMap<>();
        for (SimNode n : nodes) adj.put(n.id, new ArrayList<>());
        for (SimEdge e : edges) {
            adj.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e);
            SimEdge rev = new SimEdge();
            rev.from = e.to; rev.to = e.from; rev.weight = e.weight;
            adj.computeIfAbsent(e.to, k -> new ArrayList<>()).add(rev);
        }

        // ── 2. BFS spread (weight > 0.3 means it spreads) ──
        Set<Integer>         compromised = new LinkedHashSet<>();
        Map<Integer,Integer> timeMap     = new LinkedHashMap<>();
        Queue<Integer>       queue       = new LinkedList<>();
        compromised.add(entryId);
        timeMap.put(entryId, 0);
        queue.add(entryId);
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            int t   = timeMap.get(cur);
            for (SimEdge e : adj.getOrDefault(cur, List.of())) {
                if (!compromised.contains(e.to) && e.weight > 0.3) {
                    compromised.add(e.to);
                    timeMap.put(e.to, t + 1);
                    queue.add(e.to);
                }
            }
        }
        Set<Integer> clean = nodes.stream()
            .map(n -> n.id)
            .filter(id -> !compromised.contains(id))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // ── 3. Build timeline ──
        int maxTime = timeMap.values().stream().mapToInt(i->i).max().orElse(1);
        List<Map<String,Object>> timeline = new ArrayList<>();
        timeMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
            SimNode nd = nodes.stream().filter(n -> n.id == entry.getKey()).findFirst().orElse(null);
            if (nd == null) return;
            int barPct = maxTime == 0 ? 100 : Math.min(100, (int)(100.0*(maxTime - entry.getValue())/maxTime + 10));
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("nodeId",     nd.id);
            row.put("nodeName",   nd.name);
            row.put("time",       entry.getValue());
            row.put("barPercent", barPct);
            timeline.add(row);
        });

        // ── 4. Dijkstra — fastest spread path to highest-trust compromised node ──
        Map<Integer,Double>  dist = new HashMap<>();
        Map<Integer,Integer> prev = new HashMap<>();
        PriorityQueue<int[]> pq   = new PriorityQueue<>(Comparator.comparingInt((int[] a) -> a[1]).reversed());
        for (SimNode n : nodes) dist.put(n.id, 0.0);
        dist.put(entryId, 1.0);
        pq.offer(new int[]{entryId, 1000});
        while (!pq.isEmpty()) {
            int[] cur   = pq.poll();
            int   curId = cur[0];
            for (SimEdge e : adj.getOrDefault(curId, List.of())) {
                double nd = dist.get(curId) * e.weight;
                if (nd > dist.getOrDefault(e.to, 0.0)) {
                    dist.put(e.to, nd);
                    prev.put(e.to, curId);
                    pq.offer(new int[]{e.to, (int)(nd * 1000)});
                }
            }
        }
        // Reconstruct path to highest-trust compromised node
        int target = compromised.stream()
            .filter(id -> id != entryId)
            .max(Comparator.comparingDouble(id ->
                nodes.stream().filter(n -> n.id == id).mapToDouble(n -> n.trust).findFirst().orElse(0)))
            .orElse(-1);
        List<Integer> fastestPath = new ArrayList<>();
        if (target != -1) {
            int cur = target;
            while (prev.containsKey(cur)) { fastestPath.add(0, cur); cur = prev.get(cur); }
            fastestPath.add(0, entryId);
        }

        // ── 5. Risk score ──
        boolean highTrustHit = compromised.stream().anyMatch(id ->
            nodes.stream().filter(n -> n.id == id).anyMatch(n -> n.trust > 0.8));
        int score = Math.min(99, 40 + compromised.size() * 10 + (highTrustHit ? 15 : 0));

        // ── 6. Articulation points (degree heuristic) ──
        Map<Integer,Long> degree = new HashMap<>();
        for (SimEdge e : edges) {
            degree.merge(e.from, 1L, Long::sum);
            degree.merge(e.to,   1L, Long::sum);
        }
        List<Map<String,Object>> artPoints = compromised.stream()
            .filter(id -> id != entryId)
            .sorted(Comparator.comparingLong((Integer id) -> degree.getOrDefault(id, 0L)).reversed())
            .limit(3)
            .map(id -> {
                SimNode nd = nodes.stream().filter(n -> n.id == id).findFirst().orElse(null);
                Map<String,Object> ap = new LinkedHashMap<>();
                ap.put("name", nd != null ? nd.name + " (Node " + id + ")" : "Node " + id);
                ap.put("note", "High connectivity — removal disrupts spread chain");
                return ap;
            }).collect(Collectors.toList());

        // ── 7. Node list with status ──
        List<Map<String,Object>> nodeList = nodes.stream().map(n -> {
            Map<String,Object> nm = new LinkedHashMap<>();
            nm.put("id",          n.id);
            nm.put("name",        n.name);
            nm.put("type",        n.type);
            nm.put("trust",       n.trust);
            nm.put("compromised", compromised.contains(n.id));
            return nm;
        }).collect(Collectors.toList());

        // ── 8. Assemble final response ──
        String entryName = nodes.stream().filter(n -> n.id == entryId)
            .map(n -> n.name).findFirst().orElse("Node " + entryId);
        String targetName = target == -1 ? "None" : nodes.stream()
            .filter(n -> n.id == target).map(n -> n.name).findFirst().orElse("Node " + target);

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("compromisedNodes",   compromised.size());
        result.put("cleanNodes",         clean.size());
        result.put("riskScore",          score);
        result.put("highTrustHit",       highTrustHit);
        result.put("threatDescription",  "Spread from " + entryName + " → reached " +
                                         compromised.size() + " node(s)" +
                                         (highTrustHit ? " · HIGH-VALUE target hit: " + targetName : ""));
        result.put("entryNodeId",        entryId);
        result.put("entryNodeName",      entryName);
        result.put("compromisedIds",     new ArrayList<>(compromised));
        result.put("cleanIds",           new ArrayList<>(clean));
        result.put("timeline",           timeline);
        result.put("fastestPath",        fastestPath);
        result.put("fastestPathNote",    highTrustHit
                                         ? "High-trust route reached: " + targetName
                                         : "No high-trust node reached");
        result.put("nodes",              nodeList);
        result.put("articulationPoints", artPoints);
        return result;
    }
}
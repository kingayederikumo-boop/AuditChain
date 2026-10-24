package com.auditchain.generator;

import com.auditchain.model.AuditEntry;
import com.auditchain.persistence.PersistentHashChainEngine;
import com.auditchain.persistence.SqliteAuditStore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a realistic simulated results flow: multiple PUs -> LGA -> State.
 * Fully synthetic. No real electoral data.
 */
public class SyntheticDataGenerator {

    private final Random random = new Random(42);
    private final String[] parties = {"APC", "PDP", "LP", "NNPP"};

    public static void main(String[] args) {
        int puCount = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        boolean useFile = args.length > 1 && "file".equalsIgnoreCase(args[1]);

        SyntheticDataGenerator gen = new SyntheticDataGenerator();
        Path dbPath = useFile ? Path.of("data", "synthetic-auditchain.db") : null;

        SqliteAuditStore store = dbPath == null
                ? SqliteAuditStore.inMemory()
                : new SqliteAuditStore(dbPath);

        PersistentHashChainEngine engine = new PersistentHashChainEngine(store);

        System.out.println("=== AuditChain Synthetic Data Generator ===\n");
        System.out.println("PU count: " + puCount);
        System.out.println("Store: " + (dbPath == null ? "in-memory" : dbPath));
        System.out.println();

        List<String> puCodes = gen.generatePuCodes(puCount);
        List<PuResult> puResults = new ArrayList<>();

        System.out.println("1. Generating PU-level entries...");
        for (String pu : puCodes) {
            PuResult r = gen.randomPuResult(pu);
            puResults.add(r);
            String payload = r.toJson("PU");
            engine.append(payload, AuditEntry.ActorRole.PU_OFFICER, "officer-" + pu.toLowerCase(), pu);
            System.out.println("   " + pu + " -> votes=" + r.votesCast + " " + r.partyBreakdown());
        }

        System.out.println("\n2. Generating LGA aggregation...");
        String lgaCode = "LGA-01";
        LgaResult lga = gen.aggregateLga(lgaCode, puResults);
        engine.append(lga.toJson(), AuditEntry.ActorRole.LGA_OFFICER, "officer-lga-01", lgaCode);
        System.out.println("   " + lgaCode + " -> totalVotes=" + lga.totalVotes + " from " + puResults.size() + " PUs");

        System.out.println("\n3. Generating State aggregation...");
        String stateCode = "STATE-ENUGU";
        StateResult state = gen.aggregateState(stateCode, List.of(lga));
        engine.append(state.toJson(), AuditEntry.ActorRole.STATE_OFFICER, "officer-state-enugu", stateCode);
        System.out.println("   " + stateCode + " -> totalVotes=" + state.totalVotes);

        System.out.println("\n4. Verifying chain...");
        var breakPoint = engine.verifyChain();
        if (breakPoint.isEmpty()) {
            System.out.println("   [OK] Chain VALID -- size=" + engine.size()
                    + " tip=" + engine.getLatestHash().substring(0, 16) + "...");
        } else {
            System.out.println("   [FAIL] Broken at sequence " + breakPoint.get());
        }

        System.out.println("\n5. Offline queue status: pending=" + engine.getPendingSync().size());

        engine.close();
        System.out.println("\n=== Generator complete ===");
    }

    List<String> generatePuCodes(int n) {
        List<String> codes = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            codes.add(String.format("PU-%03d", i));
        }
        return codes;
    }

    PuResult randomPuResult(String puCode) {
        int total = 80 + random.nextInt(250);
        int[] shares = randomShares(total, parties.length);
        return new PuResult(puCode, total, parties, shares);
    }

    LgaResult aggregateLga(String lgaCode, List<PuResult> pus) {
        int total = 0;
        int[] shares = new int[parties.length];
        for (PuResult p : pus) {
            total += p.votesCast;
            for (int i = 0; i < parties.length; i++) {
                shares[i] += p.shares[i];
            }
        }
        return new LgaResult(lgaCode, total, parties, shares, pus.size());
    }

    StateResult aggregateState(String stateCode, List<LgaResult> lgas) {
        int total = 0;
        int[] shares = new int[parties.length];
        for (LgaResult l : lgas) {
            total += l.totalVotes;
            for (int i = 0; i < parties.length; i++) {
                shares[i] += l.shares[i];
            }
        }
        return new StateResult(stateCode, total, parties, shares, lgas.size());
    }

    private int[] randomShares(int total, int n) {
        int[] s = new int[n];
        int remaining = total;
        for (int i = 0; i < n - 1; i++) {
            s[i] = remaining == 0 ? 0 : random.nextInt(remaining + 1);
            remaining -= s[i];
        }
        s[n - 1] = remaining;
        for (int i = 0; i < n; i++) {
            int j = random.nextInt(n);
            int tmp = s[i];
            s[i] = s[j];
            s[j] = tmp;
        }
        int sum = 0;
        for (int v : s) sum += v;
        if (sum != total && s.length > 0) {
            s[0] += (total - sum);
        }
        return s;
    }

    static class PuResult {
        final String puCode;
        final int votesCast;
        final String[] parties;
        final int[] shares;

        PuResult(String puCode, int votesCast, String[] parties, int[] shares) {
            this.puCode = puCode;
            this.votesCast = votesCast;
            this.parties = parties;
            this.shares = shares;
        }

        String partyBreakdown() {
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < parties.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(parties[i]).append("\":" ).append(shares[i]);
            }
            return sb.append("}").toString();
        }

        String toJson(String level) {
            return "{\"polling_unit_code\":\"" + puCode + "\",\"votes_cast\":" + votesCast
                    + ",\"party_results\":" + partyBreakdown() + ",\"level\":\"" + level + "\"}";
        }
    }

    static class LgaResult {
        final String lgaCode;
        final int totalVotes;
        final String[] parties;
        final int[] shares;
        final int puCount;

        LgaResult(String lgaCode, int totalVotes, String[] parties, int[] shares, int puCount) {
            this.lgaCode = lgaCode;
            this.totalVotes = totalVotes;
            this.parties = parties;
            this.shares = shares;
            this.puCount = puCount;
        }

        String toJson() {
            StringBuilder partiesJson = new StringBuilder("{");
            for (int i = 0; i < parties.length; i++) {
                if (i > 0) partiesJson.append(",");
                partiesJson.append("\"").append(parties[i]).append("\":" ).append(shares[i]);
            }
            partiesJson.append("}");
            return "{\"lga_code\":\"" + lgaCode + "\",\"total_votes\":" + totalVotes
                    + ",\"pu_count\":" + puCount + ",\"party_results\":" + partiesJson
                    + ",\"level\":\"LGA\"}";
        }
    }

    static class StateResult {
        final String stateCode;
        final int totalVotes;
        final String[] parties;
        final int[] shares;
        final int lgaCount;

        StateResult(String stateCode, int totalVotes, String[] parties, int[] shares, int lgaCount) {
            this.stateCode = stateCode;
            this.totalVotes = totalVotes;
            this.parties = parties;
            this.shares = shares;
            this.lgaCount = lgaCount;
        }

        String toJson() {
            StringBuilder partiesJson = new StringBuilder("{");
            for (int i = 0; i < parties.length; i++) {
                if (i > 0) partiesJson.append(",");
                partiesJson.append("\"").append(parties[i]).append("\":" ).append(shares[i]);
            }
            partiesJson.append("}");
            return "{\"state_code\":\"" + stateCode + "\",\"total_votes\":" + totalVotes
                    + ",\"lga_count\":" + lgaCount + ",\"party_results\":" + partiesJson
                    + ",\"level\":\"STATE\"}";
        }
    }
}

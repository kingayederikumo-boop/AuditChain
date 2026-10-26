package com.auditchain.ai;

import java.util.LinkedHashMap;
import java.util.Map;

public class VisionResult {
    private final Map<String, Integer> partyVotes = new LinkedHashMap<>();
    private int totalVotesExtracted;
    private boolean arithmeticConsistent;
    private String notes;
    private boolean requiresHumanReview;

    public Map<String, Integer> getPartyVotes() { return partyVotes; }
    public int getTotalVotesExtracted() { return totalVotesExtracted; }
    public void setTotalVotesExtracted(int totalVotesExtracted) { this.totalVotesExtracted = totalVotesExtracted; }
    public boolean isArithmeticConsistent() { return arithmeticConsistent; }
    public void setArithmeticConsistent(boolean arithmeticConsistent) { this.arithmeticConsistent = arithmeticConsistent; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isRequiresHumanReview() { return requiresHumanReview; }
    public void setRequiresHumanReview(boolean requiresHumanReview) { this.requiresHumanReview = requiresHumanReview; }
}

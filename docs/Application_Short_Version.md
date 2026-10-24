# AuditChain – Application Short Version
**For #AI4Elections Hackathon Application Form**

---

**Project Name:** AuditChain

**Primary Track:** Track 4 – Electoral Cybersecurity and Resilience  
**Optional Cross-Cutting Area:** Climate Resilience and Environmental Innovation

---

### Short Problem Statement (for application form)

In many parts of Nigeria, electoral results transmission faces two persistent risks: (1) possible undetected alteration of digital records, and (2) loss of connectivity caused by infrastructure gaps or climate-related events such as flooding and power outages. Traditional logs often lack strong cryptographic guarantees and fail when the network is unavailable.

AuditChain addresses this by providing a simulated, offline-first, hash-chained audit trail for results moving from Polling Unit → LGA → State. Every entry is cryptographically linked to the previous one. Any modification breaks the chain and is immediately detectable. The system continues to function without network access and synchronises later, making it resilient to the connectivity disruptions that climate events frequently cause.

This is a purely defensive simulation. It never connects to, probes, or depends on any live electoral system.

---

### Preferred Challenge Track
Track 4 – Electoral Cybersecurity and Resilience  
(with Climate Resilience cross-cutting focus on offline operation during infrastructure disruption)

---

### Relevant Skills & Experience
- Senior Backend Engineering (Java, Spring Boot, microservices, distributed systems)
- Proven work on high-integrity systems including a Distributed Voting System on Hedera Hashgraph (10,000+ concurrent votes, zero fraud incidents, fault-tolerant design)
- Strong security background: JWT/OAuth2, Spring Security, cryptographic practices, AWS
- Experience building resilient, high-throughput backend systems (100k+ daily requests, significant latency reduction, high uptime)

---

### What We Will Build (Prototype Scope)
- Hash-chained audit log for a simulated results transmission flow
- Offline-first operation with local queuing and later synchronisation
- Independent verification tool that detects the exact point of any tampering
- Basic role-based access and clear residual risk documentation
- Demonstrable resilience under simulated connectivity loss

---

### Key Principles We Will Follow
- Fully synthetic data only
- No interaction with live electoral infrastructure
- Human oversight retained at all times
- Honest documentation of limitations and residual risks
- Focus on defensive integrity, not attack tools

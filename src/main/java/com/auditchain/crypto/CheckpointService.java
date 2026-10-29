package com.auditchain.crypto;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ed25519-signed checkpoints of the hash-chain tip.
 * Verification is pinned to this service's trusted public key, not the
 * public key supplied by an untrusted checkpoint payload.
 */
@Service
public class CheckpointService {

    public static final class Checkpoint {
        public final long sequence;
        public final String chainTipHash;
        public final Instant createdAt;
        public final String signatureHex;
        public final String publicKeyHex;

        public Checkpoint(long sequence, String chainTipHash, Instant createdAt,
                          String signatureHex, String publicKeyHex) {
            this.sequence = sequence;
            this.chainTipHash = chainTipHash;
            this.createdAt = createdAt;
            this.signatureHex = signatureHex;
            this.publicKeyHex = publicKeyHex;
        }
    }

    private final Ed25519PrivateKeyParameters privateKey;
    private final Ed25519PublicKeyParameters publicKey;
    private final List<Checkpoint> published = new ArrayList<>();

    public CheckpointService(
            @Value("${auditchain.crypto.ed25519.seed-hex:}") String seedHex) {
        byte[] seed;
        if (seedHex != null && seedHex.length() == 64) {
            seed = Hex.decode(seedHex);
        } else {
            seed = new byte[32];
            new SecureRandom().nextBytes(seed);
        }
        this.privateKey = new Ed25519PrivateKeyParameters(seed, 0);
        this.publicKey = privateKey.generatePublicKey();
    }

    public String publicKeyHex() {
        return Hex.toHexString(publicKey.getEncoded());
    }

    public Checkpoint createAndPublish(long sequence, String chainTipHash) {
        Instant now = Instant.now();
        String message = sequence + "|" + chainTipHash + "|" + now;
        byte[] sig = sign(message.getBytes(StandardCharsets.UTF_8));
        Checkpoint cp = new Checkpoint(sequence, chainTipHash, now,
                Hex.toHexString(sig), publicKeyHex());
        synchronized (published) {
            published.add(cp);
        }
        return cp;
    }

    /**
     * Verify both the signature and that the checkpoint names this service's
     * trusted key. Never trust a public key merely because it is embedded in
     * the checkpoint being checked.
     */
    public boolean verify(Checkpoint cp) {
        if (cp == null || cp.chainTipHash == null || cp.createdAt == null
                || cp.signatureHex == null || cp.publicKeyHex == null) {
            return false;
        }
        if (!publicKeyHex().equalsIgnoreCase(cp.publicKeyHex)) {
            return false;
        }
        String message = cp.sequence + "|" + cp.chainTipHash + "|" + cp.createdAt;
        try {
            return verify(message.getBytes(StandardCharsets.UTF_8),
                    Hex.decode(cp.signatureHex), publicKey.getEncoded());
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<String> detectTampering(long currentSequence, String currentTipHash,
                                            List<Checkpoint> externalCheckpoints) {
        for (Checkpoint cp : externalCheckpoints) {
            if (!verify(cp)) {
                return Optional.of("checkpoint signature invalid at sequence " + cp.sequence);
            }
            if (cp.sequence == currentSequence && !cp.chainTipHash.equals(currentTipHash)) {
                return Optional.of("tip hash diverged from published checkpoint at sequence " + cp.sequence);
            }
        }
        return Optional.empty();
    }

    public List<Checkpoint> listPublished() {
        synchronized (published) {
            return List.copyOf(published);
        }
    }

    private byte[] sign(byte[] message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, privateKey);
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    private boolean verify(byte[] message, byte[] signature, byte[] pubKeyBytes) {
        try {
            Ed25519PublicKeyParameters pub = new Ed25519PublicKeyParameters(pubKeyBytes, 0);
            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, pub);
            verifier.update(message, 0, message.length);
            return verifier.verifySignature(signature);
        } catch (Exception e) {
            return false;
        }
    }
}

package com.sandustnetwork.fineclaim.claim.config;

import com.sandustnetwork.fineclaim.claim.storage.ClaimRepository;

import java.util.Objects;
import java.util.UUID;

public final class ClaimLimitChecker {

    private final ClaimRepository repository;
    private ClaimSettings settings;

    public ClaimLimitChecker(ClaimRepository repository, ClaimSettings settings) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public void updateSettings(ClaimSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public boolean canAddChunk(UUID owner) {
        Objects.requireNonNull(owner, "owner");
        return repository.countChunksByOwner(owner) < settings.maxChunksPerMember()
                && repository.countTotalChunks() < settings.maxChunksPerServer();
    }

    public String limitFailureMessage(UUID owner) {
        Objects.requireNonNull(owner, "owner");
        if (repository.countChunksByOwner(owner) >= settings.maxChunksPerMember()) {
            return "You have reached your maximum claim size of " + settings.maxChunksPerMember() + " chunk(s).";
        }
        if (repository.countTotalChunks() >= settings.maxChunksPerServer()) {
            return "The server has reached the maximum claim capacity of "
                    + settings.maxChunksPerServer() + " chunk(s).";
        }
        return "You cannot claim this chunk.";
    }
}

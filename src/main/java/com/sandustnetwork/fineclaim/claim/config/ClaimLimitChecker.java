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

    public boolean canAddBlocks(UUID owner, int additionalBlocks) {
        Objects.requireNonNull(owner, "owner");
        if (additionalBlocks <= 0) {
            return true;
        }
        return repository.countBlocksByOwner(owner) + additionalBlocks <= settings.maxBlocksPerMember()
                && repository.countTotalBlocks() + additionalBlocks <= settings.maxBlocksPerServer();
    }

    public boolean canChangeBlocks(UUID owner, int blockDelta) {
        Objects.requireNonNull(owner, "owner");
        if (blockDelta <= 0) {
            return true;
        }
        return canAddBlocks(owner, blockDelta);
    }

    public String limitFailureMessage(UUID owner, int additionalBlocks) {
        Objects.requireNonNull(owner, "owner");
        if (repository.countBlocksByOwner(owner) + additionalBlocks > settings.maxBlocksPerMember()) {
            return "You have reached your maximum claim size of " + settings.maxBlocksPerMember() + " block(s).";
        }
        if (repository.countTotalBlocks() + additionalBlocks > settings.maxBlocksPerServer()) {
            return "The server has reached the maximum claim capacity of "
                    + settings.maxBlocksPerServer() + " block(s).";
        }
        return "You cannot claim this area.";
    }

    public String validateBox(int volume) {
        if (volume <= 0) {
            return "Claim area must contain at least one block.";
        }
        return null;
    }
}

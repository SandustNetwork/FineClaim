package com.sandustnetwork.fineclaim.claim.visual;

import com.sandustnetwork.fineclaim.claim.domain.ClaimBox;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.concurrent.atomic.AtomicBoolean;

final class PreviewSession {

    private final ClaimBox box;
    private final ScheduledTask animationTask;
    private final ScheduledTask expiryTask;
    private final AtomicBoolean active = new AtomicBoolean(true);

    PreviewSession(ClaimBox box, ScheduledTask animationTask, ScheduledTask expiryTask) {
        this.box = box;
        this.animationTask = animationTask;
        this.expiryTask = expiryTask;
    }

    ClaimBox box() {
        return box;
    }

    boolean isActive() {
        return active.get();
    }

    void deactivate() {
        active.set(false);
        if (animationTask != null) {
            animationTask.cancel();
        }
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }
}

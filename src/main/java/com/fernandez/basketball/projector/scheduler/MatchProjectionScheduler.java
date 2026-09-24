package com.fernandez.basketball.projector.scheduler;

import com.fernandez.basketball.projector.service.MatchProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchProjectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchProjectionScheduler.class);
    private final MatchProjectionService service;

    public MatchProjectionScheduler(MatchProjectionService service) {
        this.service = service;
    }

    @Scheduled(
            fixedDelayString = "${app.projector.fixed-delay-ms:60000}",
            initialDelayString = "${app.projector.initial-delay-ms:5000}"
    )
    public void refreshReadModel() {
        int projected = service.projectAll();
        log.info("Projection cycle completed. Projected {} matches", projected);
    }
}

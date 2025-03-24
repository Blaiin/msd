package it.dmi.quartz.manager;

import it.dmi.data.api.service.*;
import it.dmi.data.dto.*;
import it.dmi.quartz.manager.components.JobDispatcher;
import it.dmi.quartz.manager.components.JobEventHandler;
import it.dmi.quartz.scheduler.MSDScheduler;
import it.dmi.structure.exceptions.impl.internal.DependencyInjectionException;
import it.dmi.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Stateless
public class Manager {

    @Schedule(minute = "*",
              hour = "*",
              persistent = false)
    private void start() {
        //Check if class is fully initialized first
        if (!this.initialized) {
            log.debug("Waiting for Manager to fully initialize before scheduling");
            return;
        }
        if (this.scheduling) {
            log.debug("Manager is already scheduling");
            return;
        }
        if (Instant.now().isBefore(SERVER_START_TIME.plusSeconds(15))) {
            log.debug("Skipping execution, waiting for server to fully initialize...");
            return;
        }

        boolean reloaded = false;
        //Before scheduling check if configs have to be reloaded
        if (this.reloadCount.get() > 0 && this.appService.getByID(1L).getReload().equals("RELOAD")) {
            log.info("Reloading all controls");
            reloaded = reloadTasks();
        }

        if (reloaded || !this.configs.isEmpty()) {
            scheduleConfigs();
        } else log.warn("No configs found to be scheduled");
    }

    public void scheduleConfigs() {
        this.scheduling = true;

        if(this.configs.isEmpty()) {
            log.warn("No configs available to be scheduled, checking reload status..");
            reloadTasks();
            if (this.configs.isEmpty()) {
                log.warn("No configs found to be scheduled after reloading.");
                this.scheduling = false;
                return;
            }
        }
        this.dispatcher.schedule(this.configs);
        this.scheduling = false;
    }

    private synchronized boolean reloadTasks() {
        try {
            final var appConf = appService.getByID(1L);
            if (appConf.getReload().equals("RELOAD")) {
                final var controlList = controlService.getAllOrderedAsDTOs();
                controlList.forEach(cDTO ->
                        Utils.Dev.populateConfigs(false, false, false, true,
                                this.configs, cDTO));
                appConf.setReload("RELOADED");
                appService.update(appConf);
                this.lastReloadState = "RELOADED";
                this.reloadCount.incrementAndGet();
                log.info("Configuration reloaded");
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error while reloading Tasks: ", e);
            return false;
        }
    }

    @PostConstruct
    public void init() {
        try {
            if (controlService == null) throw new DependencyInjectionException("Control Service was null");
            if (msdScheduler == null) throw new DependencyInjectionException("MSD Scheduler was null");
            if (appService == null) throw new DependencyInjectionException("AppConfig Service was null");
            if (handler == null) throw new DependencyInjectionException("Job event handler was null");
            if (dispatcher == null) throw new DependencyInjectionException("Job dispatcher was null");

            this.scheduler = this.msdScheduler.getMsdScheduler();
            configs = new ArrayList<>();
            List<ControlloDTO> controls = this.controlService.getAllOrderedAsDTOs();
            if(!controls.isEmpty()) {
                log.debug("Controls fetched: {}", controls.size());
                for(ControlloDTO controllo : controls)
                    Utils.Dev.populateConfigs(false, false, false, true,
                            this.configs, controllo);
                this.lastReloadState = "LOADED";
                this.reloadCount.incrementAndGet();
            } else log.warn("No Controls found.");
            this.handler.bindManager(this);
            this.dispatcher.bindManager(this);
            log.debug("Manager initialized.");
            this.initialized = true;
        } catch (DependencyInjectionException e) {
            log.error("Internal DI error. {}", e.getMessage());
            log.debug("Internal DI error. ", e);
            this.initialized = false;
        } catch (Exception e) {
            log.error("Failed to initialize Manager. {}", e.getMessage());
            log.debug("Failed to initialize Manager", e);
            this.initialized = false;
        }
    }

    @Inject private MSDScheduler msdScheduler;

    @Inject private ControlloService controlService;

    @Inject private ApplicationConfigService appService;

    @Inject @Getter private JobEventHandler handler;

    @Inject @Getter private JobDispatcher dispatcher;

    private List<ConfigurazioneDTO> configs;

    @Getter private Scheduler scheduler;

    private final AtomicInteger reloadCount = new AtomicInteger(0);

    private volatile boolean initialized = false;

    private volatile boolean scheduling = false;

    private static final Instant SERVER_START_TIME = Instant.now();

    private volatile String lastReloadState = null;

}

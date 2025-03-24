package it.dmi.data.api.repositories.impl;

import it.dmi.data.api.repositories.ARepository;
import it.dmi.data.entities.application.ApplicationConfig;
import jakarta.ejb.Stateless;

@Stateless
public class ApplicationConfigRP extends ARepository<ApplicationConfig> {

    private static final Long RELOAD_CFG_ID = 1L;

    public ApplicationConfigRP() {}

    public void updateReloadStatus(String reloadStatus) {
        final ApplicationConfig applicationConfig = this.findByID(RELOAD_CFG_ID);
        if (applicationConfig != null) {
            applicationConfig.setReload(reloadStatus);
            this.update(applicationConfig);
        }
    }

    public ApplicationConfig getReloadCfg() {
        return this.findByID(RELOAD_CFG_ID);
    }
}

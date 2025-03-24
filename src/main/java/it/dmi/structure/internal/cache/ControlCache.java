package it.dmi.structure.internal.cache;

import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.ControlloDTO;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class ControlCache {

    private final Map<ControlloDTO, TreeSet<ConfigurazioneDTO>> CACHE;

    public boolean put(ControlloDTO c) {
        return put(c, c.configurazioni().stream().map(ConfigurazioneDTO::new).toList());
    }

    @Synchronized
    public boolean put(ControlloDTO controllo, List<ConfigurazioneDTO> configurazioni) {

        if (controllo == null || configurazioni == null) {
            log.debug("Cannot add null values");
            return false;
        }
        final var clean = configurazioni.stream().filter(Objects::nonNull).toList();
        if (clean.isEmpty()) {
            log.debug("Cannot add empty Config set");
            return false;
        }
        TreeSet<ConfigurazioneDTO> currentSet = CACHE.computeIfAbsent(controllo,
                key -> new TreeSet<>(CONFIG_COMP));
        boolean isChanged = false;
        for (final var dto : clean) if (currentSet.add(dto)) isChanged = true;
        return isChanged;
    }

    @Synchronized
    public Map<ControlloDTO, TreeSet<ConfigurazioneDTO>> getAll() {
        Map<ControlloDTO, TreeSet<ConfigurazioneDTO>> copy = new TreeMap<>(CTRL_COMP);

        CACHE.forEach((control, configs) -> {
            TreeSet<ConfigurazioneDTO> treeSetCopy = new TreeSet<>(CONFIG_COMP);
            treeSetCopy.addAll(configs);
            copy.put(control, treeSetCopy);
        });
        return copy;
    }

    @Contract(" -> new")
    public static @NotNull ControlCache create() {
        return new ControlCache(CTRL_COMP);
    }

    private ControlCache(Comparator<ControlloDTO> comparator) {
        this.CACHE = new TreeMap<>(comparator);
    }

    private static final Comparator<ControlloDTO> CTRL_COMP = (ctrl1, ctrl2) -> {
        if (ctrl1 == null && ctrl2 == null) return 0;
        if (ctrl1 == null) return -1;
        if (ctrl2 == null) return 1;
        return Long.compare(ctrl1.ordineControllo(), ctrl2.ordineControllo());
    };

    private static final Comparator<ConfigurazioneDTO> CONFIG_COMP = (conf1, conf2) -> {
        if (conf1 == null && conf2 == null) return 0;
        if (conf1 == null) return -1;
        if (conf2 == null) return 1;
        return Comparator
                .comparingLong(ConfigurazioneDTO::ordine)
                .thenComparingLong(c -> c.tipoControllo().id())
                .thenComparingLong(c -> c.controllo().id())
                .thenComparingLong(ConfigurazioneDTO::id)
                .compare(conf1, conf2);
    };

    @Synchronized
    public boolean remove(@NotNull ConfigurazioneDTO configTask) {
        boolean isRemoved = false;

        for (var entry : this.CACHE.entrySet()) {
            TreeSet<ConfigurazioneDTO> configSet = entry.getValue();

            if (configSet.removeIf(config ->
                    Objects.equals(config.id(), configTask.id()) &&
                            Objects.equals(config.controllo().id(), configTask.controllo().id()) &&
                            Objects.equals(config.tipoControllo().id(), configTask.tipoControllo().id()))) {
                isRemoved = true;
            }
        }
        if (isRemoved) log.info("Configuration {} removed from cache", configTask.strID());
        else log.warn("No matching configurations found for removal (searched for id: {})", configTask.strID());
        return isRemoved;
    }

    public int size() {
        return this.CACHE.size();
    }

    @Synchronized
    public void checkCollisionAndPutAll(final @NotNull List<ControlloDTO> ctrlDTOs) {
        final var filteredForNull = ctrlDTOs.stream().filter(Objects::nonNull).toList();
        if (filteredForNull.isEmpty()) return;
        filteredForNull.forEach(c -> {

        });
    }

    public void putAllAndRemoveRemaining(List<ControlloDTO> ctrlDTOs) {
    }
}



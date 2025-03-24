package it.dmi.quartz.builders;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.data.dto.ConfigurazioneDTO;
import it.dmi.data.dto.QuartzTask;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
abstract class MSDJobBuilder {

    protected record JobIdentity(String jobName,
                                 String jobGroup,
                                 String triggerName,
                                 String triggerGroup,
                                 String name) {

        static @NotNull JobIdentity of(@NotNull QuartzTask task) {
            return new JobIdentity(Name.jobName(task), Group.jobGroup(task),
                                    Name.triggerName(task), Group.triggerGroup(task), task.taskName());
        }
        private enum Name {
        CONFIG("Config"),
        AZIONE("Azione");

        private final String name;
        Name(String name) {
            this.name = name;
        }

        static @NotNull String jobName(@NotNull QuartzTask task) {
            return switch (task) {
                case ConfigurazioneDTO c -> CONFIG.name + c.strID();
                case AzioneDTO a -> AZIONE.name + a.strID();
            };
        }

        static @NotNull String triggerName(@NotNull QuartzTask task) {
            return switch (task) {
                case ConfigurazioneDTO c -> CONFIG.name + c.strID();
                case AzioneDTO a -> AZIONE.name + a.strID();
            };
        }
    }
        private enum Group {
            CONTROLLO("Controllo"),
            CONFIG("Config");
            private final String group;
            Group(String group) {
                this.group = group;
            }

            static @NotNull String jobGroup(@NotNull QuartzTask task) {
                return switch (task) {
                    case ConfigurazioneDTO c -> CONTROLLO.group + c.controllo().id();
                    case AzioneDTO a -> CONFIG.group + a.soglia().configurazione().getId();
                };
            }

            static @NotNull String triggerGroup(@NotNull QuartzTask task) {
                return switch (task) {
                    case ConfigurazioneDTO c -> CONTROLLO.group + c.controllo().id();
                    case AzioneDTO a -> CONFIG.group + a.soglia().configurazione().getId();
                };
            }
        }
    }

}

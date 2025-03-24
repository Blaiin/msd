package it.dmi.structure.internal.info;

import org.jetbrains.annotations.NotNull;
import org.quartz.JobDetail;
import org.quartz.Trigger;

public record JobInfo(JobDetail jobDetail, Trigger trigger, boolean alreadyDefined) implements Info {

    public static @NotNull JobInfo buildNew(@NotNull JobDetail detail, @NotNull Trigger trigger) {
        return new JobInfo(detail, trigger);
    }

    public JobInfo(JobDetail detail, Trigger trigger) {
        this(detail, trigger, false);
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        return jobDetail != null && trigger != null;
    }
}

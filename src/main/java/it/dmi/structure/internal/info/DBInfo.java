package it.dmi.structure.internal.info;

import it.dmi.data.dto.FonteDatiDTO;
import it.dmi.data.dto.QuartzTask;
import it.dmi.data.dto.SicurezzaFonteDatiDTO;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
public record DBInfo(@NotNull String id,
                     @NotNull String taskName,
                     @Nullable String jndi,
                     @Nullable String driverName,
                     @Nullable String url,
                     @Nullable String user,
                     @Nullable String password,
                     @NotNull String sqlScript) implements Info {

    public static @NotNull DBInfo from(@NotNull QuartzTask task) {
        final FonteDatiDTO fonteDati = task.fonteDati();
        final SicurezzaFonteDatiDTO utenteFonteDati = task.utenteFonteDati();
        final boolean fd = fonteDati != null;
        final boolean ufd = utenteFonteDati != null;

        if (!fd && !ufd)
            throw new IllegalStateException("Cannot make a database connection with no configured no values");

        if (fd && isBlank(fonteDati.jndiName()) && ufd)
            throw new IllegalStateException("Not possible to guarantee a database connection with url and no credentials");

        return new DBInfo(
                task.strID(),
                task.taskName(),
                fd ? fonteDati.jndiName() : null,
                fd ? fonteDati.nomeDriver() : null,
                fd ? fonteDati.url() : null,
                ufd ? utenteFonteDati.userID() : null,
                ufd ? utenteFonteDati.password() : null,
                task.sqlScript());
    }

    public @Nullable String invalid() {
        if (throughJNDI() || throughParameters()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (isBlank(this.jndi)) builder.append("jndi");
        if (isBlank(this.url)) builder.append("url");
        if (isBlank(this.user)) builder.append("user");
        if (isBlank(this.password)) builder.append("password");
        return builder.toString();
    }

    public boolean throughJNDI() {
        return isNotBlank(this.jndi);
    }
    public boolean throughParameters() {
        return isNotBlank(this.url) && isNotBlank(this.user) && isNotBlank(this.password);
    }
}


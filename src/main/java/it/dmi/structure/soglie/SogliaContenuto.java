package it.dmi.structure.soglie;

import it.dmi.utils.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

public record SogliaContenuto(String id, String operatore, String valore) implements Comparable {

    @Override
    public <Content> boolean compare(@NotNull Content toCompare) {
        AtomicBoolean compared = new AtomicBoolean(false);
        final var map = toMap(toCompare);
        if (map == null) {
            return compared.get();
        }
        map.forEach((key, value) -> compare(value, compared));
        return compared.get();
    }

    private void compare(Object value, AtomicBoolean compared) {
        if (value instanceof List<?> list)
            list.forEach(o -> {
                if (o instanceof String s)
                    compared.set(VALUE_COMPARATORS.get(this.operatore).test(s, this.valore));
            });
    }

    @Contract(pure = true)
    private static @Nullable Map<?, ?> toMap(Object o) {
        try {
            return (Map<?, ?>) o;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private static final Map<String, BiPredicate<String, String>> VALUE_COMPARATORS = Map.of(
            "<>", Utils::differ,
            "!=", Utils::differ,
            "=", Objects::equals,
            "==", Objects::equals,
            "===", Objects::equals,
            "->", String::contains,
            "<", Utils.Math::lesserThan,
            ">", Utils.Math::greaterThan
    );




}

package io.github.afa_simoes.changelistsorter;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Extends {@link DynamicBundle} (rather than hand-rolling {@code AbstractBundle} +
 * {@code SoftReference<ResourceBundle>} caching) so message lookups also pick up any installed
 * IDE language pack.
 */
public final class ChangelistOrganizerBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.ChangelistOrganizerBundle";

    private static final ChangelistOrganizerBundle INSTANCE = new ChangelistOrganizerBundle();

    private ChangelistOrganizerBundle() {
        super(BUNDLE);
    }

    public static @NotNull String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}

package io.github.afa_simoes.changelistsorter;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * Typed icon constants, so a typo in an icon path fails at compile time rather than showing up
 * as a missing icon at runtime.
 */
public final class ChangelistOrganizerIcons {
    public static final Icon ORGANIZE = IconLoader.getIcon("/icons/icon_16x16.png", ChangelistOrganizerIcons.class);
    public static final Icon ORGANIZE_LARGE = IconLoader.getIcon("/icons/icon_32x32.png", ChangelistOrganizerIcons.class);

    private ChangelistOrganizerIcons() {
    }
}

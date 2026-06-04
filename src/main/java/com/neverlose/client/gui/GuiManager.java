package com.neverlose.client.gui;

import com.neverlose.client.screen.NeverloseScreen;
import net.minecraft.client.gui.screen.Screen;

public class GuiManager {

    private Tab currentTab = Tab.VISUALS;

    public NeverloseScreen createScreen(Screen parent) {
        return new NeverloseScreen(parent, this);
    }

    public Tab getCurrentTab() { return currentTab; }
    public void setCurrentTab(Tab tab) { this.currentTab = tab; }

    public enum Tab {
        RAGE("Rage", "⚡", 0xFFFF8A3D),
        LEGIT("Legit", "◎", 0xFFE05A7A),
        VISUALS("Visuals", "👁", 0xFF8A8A9A),
        SCRIPTS("Scripts", "⚙", 0xFF8A8A9A),
        MISC("Misc", "✦", 0xFFE05A7A),
        SETTINGS("Settings", "⚙", 0xFF8A8A9A);

        public static final Tab[] SIDEBAR = { RAGE, LEGIT, VISUALS, SCRIPTS, MISC };

        public final String label;
        public final String icon;
        public final int iconColor;

        Tab(String label, String icon, int iconColor) {
            this.label = label;
            this.icon = icon;
            this.iconColor = iconColor;
        }

        public boolean isSidebarTab() {
            return this != SETTINGS;
        }
    }
}

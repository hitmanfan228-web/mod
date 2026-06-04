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
        VISUALS("Visuals", "👁"),
        AUTOMATION("Scripts", "⚙"),
        ACTIVE("Active", "✦"),
        SETTINGS("Settings", "📄");

        public final String label;
        public final String icon;

        Tab(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }
    }
}

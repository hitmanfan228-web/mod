package com.neverlose.client.gui;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name;
    public final String description;
    public final String key;
    public final Category category;
    private boolean enabled;
    public final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, String key, Category category) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.category = category;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { this.enabled = !this.enabled; }

    // ── Setting types ──────────────────────────────────────────────

    public static abstract class Setting<T> {
        public final String label;
        public final String description;
        protected T value;

        public Setting(String label, String description, T defaultValue) {
            this.label = label;
            this.description = description;
            this.value = defaultValue;
        }

        public T getValue() { return value; }
        public void setValue(T value) { this.value = value; }
    }

    public static class ToggleSetting extends Setting<Boolean> {
        public ToggleSetting(String label, String desc, boolean def) { super(label, desc, def); }
    }

    public static class SliderSetting extends Setting<Double> {
        public final double min, max, step;
        public SliderSetting(String label, String desc, double def, double min, double max, double step) {
            super(label, desc, def);
            this.min = min; this.max = max; this.step = step;
        }
    }

    public static class SelectSetting extends Setting<String> {
        public final List<String> options;
        public SelectSetting(String label, String desc, String def, List<String> options) {
            super(label, desc, def);
            this.options = options;
        }
        public void cycle() {
            int idx = options.indexOf(value);
            value = options.get((idx + 1) % options.size());
        }
    }

    // ── Category ──────────────────────────────────────────────────

    public enum Category { VISUALS }
}

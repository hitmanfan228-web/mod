package com.neverlose.client.screen;

import com.neverlose.client.gui.GuiManager;
import com.neverlose.client.gui.Module;
import com.neverlose.client.gui.ModuleRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class NeverloseScreen extends Screen {

    // ── Layout constants ──────────────────────────────────────────
    private static final int WIN_W = 860;
    private static final int WIN_H = 560;
    private static final int SIDEBAR_W = 148;
    private static final int TOPBAR_H = 40;
    private static final int CARD_W = 160;
    private static final int CARD_H = 44;
    private static final int CARD_GAP = 4;
    private static final int SETTINGS_W = 200;

    // ── Pink theme ────────────────────────────────────────────────
    private static final int COL_ACCENT       = 0xFFE05A7A;
    private static final int COL_ACCENT_DIM   = 0x33E05A7A;
    private static final int COL_BG           = 0xEBF8F8FF; // frosted glass bg
    private static final int COL_SIDEBAR      = 0xFAFFFFFF;
    private static final int COL_TOPBAR       = 0xA0FFFFFF;
    private static final int COL_CARD         = 0x8CFFFFFF;
    private static final int COL_CARD_EN      = 0x22E05A7A;
    private static final int COL_BORDER       = 0x18000000;
    private static final int COL_BORDER_STR   = 0x22000000;
    private static final int COL_TEXT         = 0xFF1A1A2A;
    private static final int COL_TEXT_MID     = 0xFF5A5A7A;
    private static final int COL_TEXT_DIM     = 0xFF9A9AB0;
    private static final int COL_TOGGLE_OFF   = 0x1F000000;
    private static final int COL_WHITE        = 0xFFFFFFFF;
    private static final int COL_SETTINGS_PAN = 0xB2FFFFFF;

    // ── State ─────────────────────────────────────────────────────
    private final Screen parent;
    private final GuiManager manager;

    // Window position (centred on open)
    private int winX, winY;

    // Dragging
    private boolean dragging = false;
    private int dragOffX, dragOffY;

    // Resizing
    private boolean resizing = false;
    private int resizeStartX, resizeStartY, resizeStartW, resizeStartH;
    private int winW = WIN_W, winH = WIN_H;

    // Tab & module selection
    private GuiManager.Tab activeTab = GuiManager.Tab.VISUALS;
    private Module selectedModule = null;

    // Scroll
    private int gridScrollY = 0;

    // Search
    private String searchQuery = "";

    // Save flash
    private int saveFlashTimer = 0;

    public NeverloseScreen(Screen parent, GuiManager manager) {
        super(Text.literal("Neverlose"));
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    protected void init() {
        winX = (this.width  - winW) / 2;
        winY = (this.height - winH) / 2;
    }

    // ── Render ────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim background
        ctx.fill(0, 0, this.width, this.height, 0x88000000);

        renderWindow(ctx, mouseX, mouseY);

        if (saveFlashTimer > 0) saveFlashTimer--;
    }

    private void renderWindow(DrawContext ctx, int mx, int my) {
        int x = winX, y = winY, w = winW, h = winH;

        // Window bg
        fillRounded(ctx, x, y, x + w, y + h, COL_BG);
        drawRoundedBorder(ctx, x, y, x + w, y + h, 0x26FFFFFF);

        // Sidebar
        ctx.fill(x, y, x + SIDEBAR_W, y + h, COL_SIDEBAR);
        ctx.fill(x + SIDEBAR_W - 1, y, x + SIDEBAR_W, y + h, COL_BORDER_STR);
        renderSidebar(ctx, x, y, h);

        // Topbar
        ctx.fill(x + SIDEBAR_W, y, x + w, y + TOPBAR_H, COL_TOPBAR);
        ctx.fill(x + SIDEBAR_W, y + TOPBAR_H - 1, x + w, y + TOPBAR_H, COL_BORDER);
        renderTopbar(ctx, x, y, w);

        // Content
        renderContent(ctx, x, y, w, h, mx, my);

        // Resize handle
        ctx.fill(x + w - 12, y + h - 12, x + w, y + h, 0x22000000);
        drawText(ctx, "⋱", x + w - 11, y + h - 11, COL_TEXT_DIM);
    }

    private void renderSidebar(DrawContext ctx, int x, int y, int h) {
        // Logo
        drawText(ctx, "NEVERLOSE", x + 12, y + 14, COL_TEXT);
        drawText(ctx, ".MC", x + 12 + textWidth("NEVERLOSE"), y + 14, COL_ACCENT);
        ctx.fill(x, y + 34, x + SIDEBAR_W - 1, y + 35, COL_BORDER);

        int cy = y + 44;

        // Section: Common
        drawText(ctx, "COMMON", x + 12, cy, COL_TEXT_DIM);
        cy += 16;

        for (GuiManager.Tab tab : GuiManager.Tab.values()) {
            boolean active = tab == activeTab;
            if (active) {
                ctx.fill(x, cy, x + SIDEBAR_W - 1, cy + 26, 0x0A000000);
                ctx.fill(x, cy + 4, x + 3, cy + 22, COL_ACCENT); // accent bar
            }
            int labelColor = active ? COL_TEXT : COL_TEXT_MID;
            drawText(ctx, tab.icon + "  " + tab.label, x + 14, cy + 7, labelColor);
            cy += 26;
        }

        // User info at bottom
        ctx.fill(x, y + h - 38, x + SIDEBAR_W - 1, y + h - 37, COL_BORDER);
        drawText(ctx, "disney13337", x + 12, y + h - 30, COL_TEXT);
        drawText(ctx, "Till: 10.09 06:40", x + 12, y + h - 18, COL_ACCENT);
    }

    private void renderTopbar(DrawContext ctx, int x, int y, int w) {
        drawText(ctx, activeTab.label, x + SIDEBAR_W + 14, y + 13, COL_TEXT);

        // Save button
        int bx = x + w - 12 - 70;
        int by = y + 7;
        boolean flash = saveFlashTimer > 0;
        int btnCol = flash ? 0xFF22CC66 : COL_ACCENT;
        ctx.fill(bx, by, bx + 68, by + 26, btnCol);
        fillRounded(ctx, bx, by, bx + 68, by + 26, btnCol);
        drawText(ctx, flash ? "✓ Saved" : "💾 Save", bx + 10, by + 8, COL_WHITE);

        // Close button
        ctx.fill(x + w - 36, by, x + w - 10, by + 26, COL_BORDER);
        drawText(ctx, "✕", x + w - 28, by + 8, COL_TEXT_MID);
    }

    private void renderContent(DrawContext ctx, int x, int y, int w, int h, int mx, int my) {
        int cx = x + SIDEBAR_W;
        int cy = y + TOPBAR_H;
        int cw = w - SIDEBAR_W;
        int ch = h - TOPBAR_H;

        switch (activeTab) {
            case VISUALS   -> renderModuleGrid(ctx, cx, cy, cw, ch, mx, my, Module.Category.VISUALS);
            case AUTOMATION -> renderAutomation(ctx, cx, cy, cw, ch);
            case ACTIVE    -> renderActiveScripts(ctx, cx, cy, cw, ch);
            case SETTINGS  -> renderSettings(ctx, cx, cy, cw, ch);
        }
    }

    // ── Module Grid ───────────────────────────────────────────────

    private void renderModuleGrid(DrawContext ctx, int x, int y, int w, int h,
                                   int mx, int my, Module.Category cat) {
        List<Module> mods = ModuleRegistry.getByCategory(cat).stream()
            .filter(m -> searchQuery.isEmpty() ||
                         m.name.toLowerCase().contains(searchQuery.toLowerCase()) ||
                         m.description.toLowerCase().contains(searchQuery.toLowerCase()))
            .toList();

        int gridW = w - SETTINGS_W;
        int cols  = Math.max(1, (gridW - 8) / (CARD_W + CARD_GAP));
        int padX  = 8;
        int padY  = 8;

        // Search bar
        ctx.fill(x + padX, y + padY, x + gridW - padX, y + padY + 22, 0x88FFFFFF);
        ctx.fill(x + padX, y + padY, x + gridW - padX, y + padY + 22, COL_BORDER);
        String searchDisplay = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        int searchCol = searchQuery.isEmpty() ? COL_TEXT_DIM : COL_TEXT;
        drawText(ctx, searchDisplay, x + padX + 7, y + padY + 6, searchCol);
        int cardStartY = y + padY + 28;

        // Cards
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx2 = x + padX + col * (CARD_W + CARD_GAP);
            int cy2 = cardStartY + row * (CARD_H + CARD_GAP) - gridScrollY;

            if (cy2 + CARD_H < y || cy2 > y + h) continue; // cull

            int cardBg   = m.isEnabled() ? COL_CARD_EN : COL_CARD;
            int borderCol = m.isEnabled() ? 0x33E05A7A : COL_BORDER;
            ctx.fill(cx2, cy2, cx2 + CARD_W, cy2 + CARD_H, cardBg);
            ctx.fill(cx2, cy2, cx2 + CARD_W, cy2 + 1, borderCol);
            ctx.fill(cx2, cy2 + CARD_H - 1, cx2 + CARD_W, cy2 + CARD_H, borderCol);
            ctx.fill(cx2, cy2, cx2 + 1, cy2 + CARD_H, borderCol);
            ctx.fill(cx2 + CARD_W - 1, cy2, cx2 + CARD_W, cy2 + CARD_H, borderCol);
            // Left accent bar
            ctx.fill(cx2, cy2, cx2 + 3, cy2 + CARD_H, m.isEnabled() ? COL_ACCENT : 0x00000000);

            // Selected outline
            if (m == selectedModule) {
                ctx.fill(cx2, cy2, cx2 + CARD_W, cy2 + 1, COL_ACCENT);
                ctx.fill(cx2, cy2 + CARD_H - 1, cx2 + CARD_W, cy2 + CARD_H, COL_ACCENT);
                ctx.fill(cx2, cy2, cx2 + 1, cy2 + CARD_H, COL_ACCENT);
                ctx.fill(cx2 + CARD_W - 1, cy2, cx2 + CARD_W, cy2 + CARD_H, COL_ACCENT);
            }

            int nameCol = m.isEnabled() ? COL_ACCENT : COL_TEXT;
            drawText(ctx, m.name, cx2 + 10, cy2 + 8, nameCol);
            drawSmallText(ctx, m.description, cx2 + 10, cy2 + 20, COL_TEXT_DIM);

            if (!m.key.isEmpty()) {
                drawSmallText(ctx, m.key, cx2 + CARD_W - 14, cy2 + 6, COL_TEXT_DIM);
            }
        }

        // Settings panel (right side)
        ctx.fill(x + gridW, y, x + w, y + h, COL_SETTINGS_PAN);
        ctx.fill(x + gridW, y, x + gridW + 1, y + h, COL_BORDER_STR);

        if (selectedModule != null) {
            renderModuleSettings(ctx, x + gridW, y, SETTINGS_W, h);
        } else {
            drawText(ctx, "SELECT MODULE", x + gridW + 12, y + 14, COL_ACCENT);
            drawSmallText(ctx, "Click a module to configure it.", x + gridW + 12, y + 28, COL_TEXT_DIM);
        }
    }

    private void renderModuleSettings(DrawContext ctx, int x, int y, int w, int h) {
        Module m = selectedModule;
        // Header
        ctx.fill(x, y, x + w, y + 30, 0x88FFFFFF);
        ctx.fill(x, y + 30, x + w, y + 31, COL_BORDER);
        drawText(ctx, m.name, x + 10, y + 10, COL_ACCENT);

        int sy = y + 38;
        for (Module.Setting<?> s : m.settings) {
            ctx.fill(x, sy - 2, x + w, sy + 26, 0x00000000);
            ctx.fill(x + 6, sy + 26, x + w - 6, sy + 27, COL_BORDER);

            drawSmallText(ctx, s.label, x + 10, sy + 2, COL_TEXT);

            if (s instanceof Module.ToggleSetting ts) {
                renderToggle(ctx, x + w - 46, sy + 2, ts.getValue());
            } else if (s instanceof Module.SliderSetting ss) {
                double pct = (ss.getValue() - ss.min) / (ss.max - ss.min);
                int trackX = x + 10, trackY = sy + 14, trackW = w - 20;
                ctx.fill(trackX, trackY + 1, trackX + trackW, trackY + 3, COL_TOGGLE_OFF);
                ctx.fill(trackX, trackY + 1, trackX + (int)(trackW * pct), trackY + 3, COL_ACCENT);
                int thumbX = trackX + (int)(trackW * pct) - 5;
                ctx.fill(thumbX, trackY - 2, thumbX + 10, trackY + 6, COL_ACCENT);
                String valStr = ss.getValue() % 1 == 0
                    ? String.valueOf((int)(double)ss.getValue())
                    : String.format("%.1f", ss.getValue());
                drawSmallText(ctx, valStr, x + w - 10 - textWidth(valStr), sy + 2, COL_TEXT_DIM);
            } else if (s instanceof Module.SelectSetting sel) {
                String val = sel.getValue();
                int bx = x + w - 10 - textWidth(val) - 8;
                ctx.fill(bx - 2, sy, bx + textWidth(val) + 10, sy + 14, COL_BORDER);
                drawSmallText(ctx, val, bx + 4, sy + 2, COL_TEXT_MID);
            }

            sy += 30;
            if (sy > y + h - 10) break;
        }
    }

    private void renderToggle(DrawContext ctx, int x, int y, boolean on) {
        int bg = on ? COL_ACCENT : COL_TOGGLE_OFF;
        ctx.fill(x, y, x + 32, y + 16, bg);
        int tx = on ? x + 32 - 14 - 2 : x + 2;
        ctx.fill(tx, y + 2, tx + 12, y + 14, COL_WHITE);
    }

    // ── Automation tab (placeholder) ──────────────────────────────

    private void renderAutomation(DrawContext ctx, int x, int y, int w, int h) {
        drawText(ctx, "Scripts", x + 14, y + 14, COL_TEXT);
        drawSmallText(ctx, "Script editor — coming soon.", x + 14, y + 32, COL_TEXT_DIM);
    }

    private void renderActiveScripts(DrawContext ctx, int x, int y, int w, int h) {
        drawText(ctx, "Active Scripts", x + 14, y + 14, COL_TEXT);
        drawSmallText(ctx, "No scripts running.", x + 14, y + 32, COL_TEXT_DIM);
    }

    // ── Settings tab ──────────────────────────────────────────────

    private void renderSettings(DrawContext ctx, int x, int y, int w, int h) {
        int sx = x + 10, sy = y + 10;

        // About badge
        ctx.fill(sx, sy, sx + 240, sy + 80, 0x99FFFFFF);
        drawText(ctx, "NEVERLOSE", sx + 10, sy + 10, COL_TEXT);
        drawText(ctx, ".CC", sx + 10 + textWidth("NEVERLOSE"), sy + 10, COL_ACCENT);
        drawSmallText(ctx, "Username: disney13337", sx + 10, sy + 30, COL_TEXT_MID);
        drawSmallText(ctx, "Branch: Release", sx + 10, sy + 42, COL_TEXT_MID);
        drawSmallText(ctx, "Updated: Jun 18 2024", sx + 10, sy + 54, COL_TEXT_MID);
        drawSmallText(ctx, "Valid Until: 10.09.2024 06:40", sx + 10, sy + 66, COL_TEXT_MID);

        sy += 90;
        drawSmallText(ctx, "CLIENT", sx, sy, COL_TEXT_DIM);
        sy += 14;
        ctx.fill(sx, sy, sx + 300, sy + 28, 0x66FFFFFF);
        drawSmallText(ctx, "GUI Key", sx + 10, sy + 9, COL_TEXT);
        drawSmallText(ctx, "INSERT", sx + 220, sy + 9, COL_TEXT_MID);
        sy += 28;
        ctx.fill(sx, sy, sx + 300, sy + 28, 0x44FFFFFF);
        drawSmallText(ctx, "Auto Save", sx + 10, sy + 9, COL_TEXT);
        renderToggle(ctx, sx + 258, sy + 6, true);
        sy += 28;
        ctx.fill(sx, sy, sx + 300, sy + 28, 0x66FFFFFF);
        drawSmallText(ctx, "Language", sx + 10, sy + 9, COL_TEXT);
        drawSmallText(ctx, "English", sx + 220, sy + 9, COL_TEXT_MID);

        sy += 38;
        drawSmallText(ctx, "ACCENT COLOR", sx, sy, COL_TEXT_DIM);
        sy += 14;
        ctx.fill(sx, sy, sx + 300, sy + 30, 0x66FFFFFF);
        int[] colors = {0xFF5B8AE0, 0xFF00AAFF, 0xFF44BB66, 0xFFAA44DD, 0xFFFFAA00, 0xFFE05A7A};
        for (int i = 0; i < colors.length; i++) {
            int cx2 = sx + 10 + i * 22;
            ctx.fill(cx2, sy + 7, cx2 + 16, sy + 23, colors[i]);
        }
    }

    // ── Mouse events ──────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int imx = (int) mx, imy = (int) my;

        // Close button
        int bx = winX + winW - 36, by = winY + 7;
        if (imx >= bx && imx <= winX + winW - 10 && imy >= by && imy <= by + 26) {
            this.close();
            return true;
        }

        // Save button
        int sbx = winX + winW - 12 - 70;
        if (imx >= sbx && imx <= sbx + 68 && imy >= by && imy <= by + 26) {
            saveFlashTimer = 40;
            return true;
        }

        // Resize handle
        if (imx >= winX + winW - 12 && imy >= winY + winH - 12) {
            resizing = true;
            resizeStartX = imx; resizeStartY = imy;
            resizeStartW = winW; resizeStartH = winH;
            return true;
        }

        // Topbar drag
        if (imx >= winX + SIDEBAR_W && imx <= winX + winW &&
            imy >= winY && imy <= winY + TOPBAR_H) {
            dragging = true;
            dragOffX = imx - winX;
            dragOffY = imy - winY;
            return true;
        }

        // Sidebar tabs
        if (imx >= winX && imx <= winX + SIDEBAR_W) {
            int cy = winY + 60;
            for (GuiManager.Tab tab : GuiManager.Tab.values()) {
                if (imy >= cy && imy <= cy + 26) {
                    activeTab = tab;
                    selectedModule = null;
                    searchQuery = "";
                    gridScrollY = 0;
                    return true;
                }
                cy += 26;
            }
        }

        // Module cards (visuals tab)
        if (activeTab == GuiManager.Tab.VISUALS) {
            int gx = winX + SIDEBAR_W;
            int gy = winY + TOPBAR_H;
            int gridW = winW - SIDEBAR_W - SETTINGS_W;
            int cols = Math.max(1, (gridW - 8) / (CARD_W + CARD_GAP));
            int cardStartY = gy + 8 + 28;

            List<Module> mods = getFilteredMods(Module.Category.VISUALS);
            for (int i = 0; i < mods.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int cx2 = gx + 8 + col * (CARD_W + CARD_GAP);
                int cy2 = cardStartY + row * (CARD_H + CARD_GAP) - gridScrollY;
                if (imx >= cx2 && imx <= cx2 + CARD_W && imy >= cy2 && imy <= cy2 + CARD_H) {
                    if (button == 1) { // right click = toggle
                        mods.get(i).toggle();
                    } else {
                        selectedModule = mods.get(i);
                    }
                    return true;
                }
            }

            // Click in settings panel — toggle / cycle
            if (selectedModule != null) {
                int spx = winX + SIDEBAR_W + (winW - SIDEBAR_W - SETTINGS_W);
                int sy2 = winY + TOPBAR_H + 38;
                for (Module.Setting<?> s : selectedModule.settings) {
                    if (imy >= sy2 && imy <= sy2 + 28) {
                        if (s instanceof Module.ToggleSetting ts) {
                            ts.setValue(!ts.getValue());
                        } else if (s instanceof Module.SelectSetting sel) {
                            sel.cycle();
                        }
                        return true;
                    }
                    sy2 += 30;
                }
            }

            // Search bar click
            if (imy >= winY + TOPBAR_H + 8 && imy <= winY + TOPBAR_H + 30 &&
                imx >= winX + SIDEBAR_W + 8 && imx <= winX + SIDEBAR_W + gridW - 8) {
                // Focus search — handled via keyboard
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            winX = (int) mx - dragOffX;
            winY = (int) my - dragOffY;
            // Clamp
            winX = Math.max(0, Math.min(this.width  - winW, winX));
            winY = Math.max(0, Math.min(this.height - winH, winY));
            return true;
        }
        if (resizing) {
            winW = Math.max(700, resizeStartW + (int)(mx - resizeStartX));
            winH = Math.max(420, resizeStartH + (int)(my - resizeStartY));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        resizing = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        if (mx >= winX + SIDEBAR_W && mx <= winX + winW - SETTINGS_W) {
            gridScrollY = Math.max(0, gridScrollY - (int)(vScroll * 16));
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { this.close(); return true; }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (Character.isLetterOrDigit(chr) || chr == ' ') {
            searchQuery += chr;
            return true;
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private List<Module> getFilteredMods(Module.Category cat) {
        return ModuleRegistry.getByCategory(cat).stream()
            .filter(m -> searchQuery.isEmpty() ||
                         m.name.toLowerCase().contains(searchQuery.toLowerCase()))
            .toList();
    }

    private void drawText(DrawContext ctx, String text, int x, int y, int color) {
        ctx.drawText(this.textRenderer, text, x, y, color, false);
    }

    private void drawSmallText(DrawContext ctx, String text, int x, int y, int color) {
        ctx.drawText(this.textRenderer, text, x, y, color, false);
    }

    private int textWidth(String text) {
        return this.textRenderer != null ? this.textRenderer.getWidth(text) : text.length() * 5;
    }

    /** Simple filled rect helper (rounded look via layered fills) */
    private void fillRounded(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private void drawRoundedBorder(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y1 + 1, color);
        ctx.fill(x1 + 2, y2 - 1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x1 + 1, y2 - 2, color);
        ctx.fill(x2 - 1, y1 + 2, x2, y2 - 2, color);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}

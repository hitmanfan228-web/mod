package com.neverlose.client.screen;

import com.neverlose.client.gui.GuiManager;
import com.neverlose.client.gui.Module;
import com.neverlose.client.gui.ModuleRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;

import java.util.List;

public class NeverloseScreen extends Screen {

    // ── Layout constants (compact default window) ─────────────────
    private static final int WIN_W = 340;
    private static final int WIN_H = 220;
    private static final int SIDEBAR_W = 92;
    private static final int TOPBAR_H = 15;
    private static final int MODULE_COLS = 1;
    private static final int CARD_H = 20;
    private static final int CARD_GAP = 2;
    private static final int SETTINGS_W = 78;
    private static final int SEARCH_BAR_H = 12;
    private static final int PAD = 3;
    private static final int TAB_START_Y = 4;
    private static final int TAB_SLOT_H = 22;
    private static final int TAB_ICON_X = 10;
    private static final int TAB_LABEL_X = 26;
    private static final int USER_AREA_H = 14;

    // ── Pink theme (low-alpha glass — stacks stay see-through) ─────
    private static final int COL_ACCENT       = 0xFFE05A7A;
    private static final int COL_ACCENT_DIM   = 0x55E05A7A;
    private static final int COL_BG           = 0x45F4F4FA;
    private static final int COL_SIDEBAR      = 0x50FFFFFF;
    private static final int COL_CONTENT      = 0x48F8F8FC;
    private static final int COL_TOPBAR       = 0x4CF0F0F6;
    private static final int COL_CARD         = 0x62FFFFFF;
    private static final int COL_CARD_EN      = 0x72FFE8EE;
    private static final int COL_BORDER       = 0x18000000;
    private static final int COL_BORDER_STR   = 0x33000000;
    private static final int COL_TEXT         = 0xFF1A1A2A;
    private static final int COL_TEXT_MID     = 0xFF5A5A7A;
    private static final int COL_TEXT_DIM     = 0xFF9A9AB0;
    private static final int COL_TOGGLE_OFF   = 0x1F000000;
    private static final int COL_WHITE        = 0xFFFFFFFF;
    private static final int COL_SETTINGS_PAN = 0x50F2F2F8;
    private static final int COL_DIM_OVERLAY  = 0x38000000;
    private static final int COL_TAB_ACTIVE   = 0x88E8E4F8;
    private static final int COL_SIDEBAR_BG   = 0x55F0F0F4;

    // ── State ─────────────────────────────────────────────────────
    private final Screen parent;
    private final GuiManager manager;

    private int winX, winY;

    private boolean dragging = false;
    private int dragOffX, dragOffY;

    private int winW = WIN_W, winH = WIN_H;

    private GuiManager.Tab activeTab = GuiManager.Tab.VISUALS;
    private Module selectedModule = null;

    private Module.SliderSetting activeSlider = null;

    private int gridScrollY = 0;
    private String searchQuery = "";
    private int saveFlashTimer = 0;

    public NeverloseScreen(Screen parent, GuiManager manager) {
        super(Text.literal("neverclick"));
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    protected void init() {
        // Fixed compact size — also cap to ~35% of screen on very large displays
        this.winW = Math.min(WIN_W, Math.max(280, (int) (this.width * 0.35)));
        this.winH = Math.min(WIN_H, Math.max(160, (int) (this.height * 0.30)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
    }

    // ── Render ────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, COL_DIM_OVERLAY);
        renderWindow(ctx, mouseX, mouseY);
        if (saveFlashTimer > 0) saveFlashTimer--;
    }

    private void renderWindow(DrawContext ctx, int mx, int my) {
        int x = winX, y = winY, w = winW, h = winH;
        int contentX = x + SIDEBAR_W;
        int contentY = y + TOPBAR_H;

        fillRounded(ctx, x, y, x + w, y + h, COL_BG);
        drawRoundedBorder(ctx, x, y, x + w, y + h, COL_BORDER);

        // Layer panels back-to-front so nothing bleeds through
        ctx.fill(x, y, x + SIDEBAR_W, y + h, COL_SIDEBAR_BG);
        ctx.fill(contentX, contentY, x + w, y + h, COL_CONTENT);
        ctx.fill(contentX, y, x + w, y + TOPBAR_H, COL_TOPBAR);
        ctx.fill(contentX, y + TOPBAR_H - 1, x + w, y + TOPBAR_H, COL_BORDER);

        ctx.enableScissor(contentX, contentY, x + w, y + h);
        renderContent(ctx, contentX, contentY, w - SIDEBAR_W, h - TOPBAR_H, mx, my);
        ctx.disableScissor();

        ctx.fill(x + SIDEBAR_W, y, x + SIDEBAR_W + 1, y + h, COL_BORDER_STR);
        renderSidebar(ctx, x, y, h);
        renderTopbar(ctx, x, y, w);
    }

    private void renderSidebar(DrawContext ctx, int x, int y, int h) {
        renderTabColumn(ctx, x, y);

        int userTop = y + h - USER_AREA_H - 4;
        boolean settingsOpen = activeTab == GuiManager.Tab.SETTINGS;
        if (settingsOpen) {
            ctx.fill(x + 3, userTop - 1, x + SIDEBAR_W - 3, y + h - 3, COL_ACCENT_DIM);
        }
        ctx.fill(x + 4, userTop - 2, x + SIDEBAR_W - 4, userTop - 1, COL_BORDER);
        String user = truncate(getPlayerName(), SIDEBAR_W - 8);
        int userX = x + (SIDEBAR_W - textWidth(user)) / 2;
        drawSmallText(ctx, user, userX, userTop + 2, settingsOpen ? COL_ACCENT : COL_TEXT_MID);
    }

    private void renderTabColumn(DrawContext ctx, int sidebarX, int windowY) {
        GuiManager.Tab[] tabs = GuiManager.Tab.SIDEBAR;

        for (int i = 0; i < tabs.length; i++) {
            GuiManager.Tab tab = tabs[i];
            int slotY = windowY + TAB_START_Y + i * TAB_SLOT_H;
            boolean active = tab == activeTab;

            if (active) {
                ctx.fill(sidebarX, slotY, sidebarX + SIDEBAR_W, slotY + TAB_SLOT_H, COL_TAB_ACTIVE);
                ctx.fill(sidebarX, slotY, sidebarX + 2, slotY + TAB_SLOT_H, COL_ACCENT);
            }

            int rowCenterY = slotY + (TAB_SLOT_H - 8) / 2;
            drawSmallText(ctx, tab.icon, sidebarX + TAB_ICON_X, rowCenterY, tab.iconColor);
            int labelColor = active ? COL_TEXT : COL_TEXT_MID;
            drawSmallText(ctx, tab.label, sidebarX + TAB_LABEL_X, rowCenterY, labelColor);
        }
    }

    private void renderTopbar(DrawContext ctx, int x, int y, int w) {
        drawSmallText(ctx, "never", x + SIDEBAR_W + 6, y + 4, COL_TEXT);
        drawSmallText(ctx, "click", x + SIDEBAR_W + 6 + textWidth("never"), y + 4, COL_ACCENT);
        int brandW = textWidth("never") + textWidth("click");
        drawSmallText(ctx, " · " + activeTab.label, x + SIDEBAR_W + 6 + brandW, y + 4, COL_TEXT_MID);

        int bx = x + w - 6 - 44;
        int by = y + 2;
        boolean flash = saveFlashTimer > 0;
        int btnCol = flash ? 0xFF22CC66 : COL_ACCENT;
        fillRounded(ctx, bx, by, bx + 42, by + 12, btnCol);
        drawSmallText(ctx, flash ? "Saved" : "Save", bx + 6, by + 2, COL_WHITE);

        ctx.fill(x + w - 20, by, x + w - 4, by + 12, COL_BORDER);
        drawSmallText(ctx, "x", x + w - 14, by + 2, COL_TEXT_MID);
    }

    private void renderContent(DrawContext ctx, int x, int y, int w, int h, int mx, int my) {
        Module.Category cat = categoryForTab(activeTab);
        if (cat != null) {
            renderModuleGrid(ctx, x, y, w, h, mx, my, cat);
            return;
        }
        switch (activeTab) {
            case SCRIPTS  -> renderAutomation(ctx, x, y, w, h);
            case SETTINGS -> renderSettings(ctx, x, y, w, h);
            default -> { }
        }
    }

    private Module.Category categoryForTab(GuiManager.Tab tab) {
        return switch (tab) {
            case RAGE -> Module.Category.RAGE;
            case LEGIT -> Module.Category.LEGIT;
            case VISUALS -> Module.Category.VISUALS;
            case MISC -> Module.Category.MISC;
            default -> null;
        };
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
        int cols = MODULE_COLS;
        int cardW = moduleCardWidth(gridW);
        int listTop = y + PAD + SEARCH_BAR_H;
        int listBottom = y + h - PAD;
        int listH = Math.max(0, listBottom - listTop);

        int rows = mods.isEmpty() ? 0 : (mods.size() + cols - 1) / cols;
        int contentH = rows > 0 ? rows * (CARD_H + CARD_GAP) - CARD_GAP : 0;
        int maxScroll = Math.max(0, contentH - listH);
        gridScrollY = Math.min(gridScrollY, maxScroll);

        int searchX1 = x + PAD;
        int searchY1 = y + PAD;
        int searchX2 = x + gridW - PAD;
        int searchY2 = searchY1 + SEARCH_BAR_H;

        ctx.enableScissor(x + PAD, listTop, x + gridW - PAD, listBottom);

        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx2 = x + PAD + col * (cardW + CARD_GAP);
            int cy2 = listTop + row * (CARD_H + CARD_GAP) - gridScrollY;

            if (cy2 + CARD_H < listTop || cy2 > listBottom) continue;

            int cardBg    = m.isEnabled() ? COL_CARD_EN : COL_CARD;
            int borderCol = m.isEnabled() ? 0x33E05A7A : COL_BORDER;
            ctx.fill(cx2, cy2, cx2 + cardW, cy2 + CARD_H, cardBg);
            ctx.fill(cx2, cy2, cx2 + cardW, cy2 + 1, borderCol);
            ctx.fill(cx2, cy2 + CARD_H - 1, cx2 + cardW, cy2 + CARD_H, borderCol);
            ctx.fill(cx2, cy2, cx2 + 1, cy2 + CARD_H, borderCol);
            ctx.fill(cx2 + cardW - 1, cy2, cx2 + cardW, cy2 + CARD_H, borderCol);
            ctx.fill(cx2, cy2, cx2 + 3, cy2 + CARD_H, m.isEnabled() ? COL_ACCENT : 0x00000000);

            if (m == selectedModule) {
                ctx.fill(cx2, cy2, cx2 + cardW, cy2 + 1, COL_ACCENT);
                ctx.fill(cx2, cy2 + CARD_H - 1, cx2 + cardW, cy2 + CARD_H, COL_ACCENT);
                ctx.fill(cx2, cy2, cx2 + 1, cy2 + CARD_H, COL_ACCENT);
                ctx.fill(cx2 + cardW - 1, cy2, cx2 + cardW, cy2 + CARD_H, COL_ACCENT);
            }

            int nameCol = m.isEnabled() ? COL_ACCENT : COL_TEXT;
            int keyPad = m.key.isEmpty() ? 4 : 14;
            String label = truncate(m.name, cardW - keyPad - 8);
            drawSmallText(ctx, label, cx2 + 6, cy2 + 6, nameCol);

            if (!m.key.isEmpty()) {
                drawSmallText(ctx, m.key, cx2 + cardW - 12, cy2 + 6, COL_TEXT_DIM);
            }
        }

        ctx.disableScissor();

        // Search bar stays pinned above the scrollable list
        ctx.fill(searchX1, searchY1, searchX2, searchY2, COL_CARD);
        ctx.fill(searchX1, searchY2 - 1, searchX2, searchY2, COL_BORDER);
        String searchDisplay = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        int searchCol = searchQuery.isEmpty() ? COL_TEXT_DIM : COL_TEXT;
        drawSmallText(ctx, searchDisplay, searchX1 + 4, searchY1 + 3, searchCol);

        ctx.fill(x + gridW, y, x + w, y + h, COL_SETTINGS_PAN);
        ctx.fill(x + gridW, y, x + gridW + 1, y + h, COL_BORDER_STR);

        if (selectedModule != null) {
            renderModuleSettings(ctx, x + gridW, y, SETTINGS_W, h);
        } else {
            drawSmallText(ctx, "SELECT", x + gridW + 4, y + 6, COL_ACCENT);
            drawSmallText(ctx, "Click module", x + gridW + 4, y + 16, COL_TEXT_DIM);
        }
    }

    private void renderModuleSettings(DrawContext ctx, int x, int y, int w, int h) {
        Module m = selectedModule;
        ctx.fill(x, y, x + w, y + 18, COL_TOPBAR);
        ctx.fill(x, y + 18, x + w, y + 19, COL_BORDER);
        drawSmallText(ctx, truncate(m.name, w - 8), x + 4, y + 5, COL_ACCENT);
        drawSmallText(ctx, truncate(m.description, w - 8), x + 4, y + 24, COL_TEXT_DIM);

        int sy = y + 36;
        for (Module.Setting<?> s : m.settings) {
            ctx.fill(x + 3, sy + 18, x + w - 3, sy + 19, COL_BORDER);
            drawSmallText(ctx, truncate(s.label, w - 34), x + 4, sy + 2, COL_TEXT);

            if (s instanceof Module.ToggleSetting ts) {
                renderToggle(ctx, x + w - 30, sy + 2, ts.getValue());
            } else if (s instanceof Module.SliderSetting ss) {
                double pct = (ss.getValue() - ss.min) / (ss.max - ss.min);
                int trackX = x + 4, trackY = sy + 10, trackW = w - 8;
                ctx.fill(trackX, trackY + 1, trackX + trackW, trackY + 2, COL_TOGGLE_OFF);
                ctx.fill(trackX, trackY + 1, trackX + (int)(trackW * pct), trackY + 2, COL_ACCENT);
            } else if (s instanceof Module.SelectSetting sel) {
                String val = truncate(sel.getValue(), 10);
                int bx = x + w - 4 - textWidth(val) - 4;
                ctx.fill(bx - 2, sy, bx + textWidth(val) + 6, sy + 10, COL_BORDER);
                drawSmallText(ctx, val, bx + 2, sy + 1, COL_TEXT_MID);
            }

            sy += 22;
            if (sy > y + h - 6) break;
        }
    }

    private void renderToggle(DrawContext ctx, int x, int y, boolean on) {
        int bg = on ? COL_ACCENT : COL_TOGGLE_OFF;
        ctx.fill(x, y, x + 22, y + 10, bg);
        int tx = on ? x + 22 - 9 - 1 : x + 1;
        ctx.fill(tx, y + 1, tx + 8, y + 9, COL_WHITE);
    }

    // ── Other tabs ────────────────────────────────────────────────

    private void renderAutomation(DrawContext ctx, int x, int y, int w, int h) {
        drawText(ctx, "Scripts", x + 14, y + 14, COL_TEXT);
        drawSmallText(ctx, "Script editor — coming soon.", x + 14, y + 32, COL_TEXT_DIM);
    }

    private void updateSliderValue(Module.SliderSetting sl, double mouseX, int sliderX, int sliderWidth) {
        double min = sl.min;
        double max = sl.max;
        // Если в SliderSetting есть поле step, используй его, иначе шаг по умолчанию 0.1
        double step = 0.1; 

        double pct = (mouseX - sliderX) / (double) sliderWidth;
        if (pct < 0) pct = 0;
        if (pct > 1) pct = 1;

        double val = min + pct * (max - min);
        val = Math.round(val / step) * step;

        if (val < min) val = min;
        if (val > max) val = max;

        sl.setValue(val);
    }

    private void renderSettings(DrawContext ctx, int x, int y, int w, int h) {
        int sx = x + 6, sy = y + 6;
        int boxW = Math.min(w - 12, 200);

        ctx.fill(sx, sy, sx + boxW, sy + 52, COL_CARD);
        drawSmallText(ctx, "never", sx + 6, sy + 6, COL_TEXT);
        drawSmallText(ctx, "click", sx + 6 + textWidth("never"), sy + 6, COL_ACCENT);
        drawSmallText(ctx, "User: " + getPlayerName(), sx + 6, sy + 22, COL_TEXT_MID);
        drawSmallText(ctx, "Branch: Release", sx + 6, sy + 34, COL_TEXT_DIM);

        sy += 60;
        drawSmallText(ctx, "CLIENT", sx, sy, COL_TEXT_DIM);
        sy += 14;
        int rowW = Math.min(w - 12, 220);
        ctx.fill(sx, sy, sx + rowW, sy + 22, COL_CARD);
        drawSmallText(ctx, "GUI Key", sx + 6, sy + 7, COL_TEXT);
        drawSmallText(ctx, "INSERT", sx + rowW - 42, sy + 7, COL_TEXT_MID);
        sy += 26;
        ctx.fill(sx, sy, sx + rowW, sy + 22, COL_CARD);
        drawSmallText(ctx, "Auto Save", sx + 6, sy + 7, COL_TEXT);
        renderToggle(ctx, sx + rowW - 28, sy + 6, true);
        sy += 26;
        ctx.fill(sx, sy, sx + rowW, sy + 22, COL_CARD);
        drawSmallText(ctx, "Language", sx + 6, sy + 7, COL_TEXT);
        drawSmallText(ctx, "English", sx + rowW - 42, sy + 7, COL_TEXT_MID);

        sy += 32;
        drawSmallText(ctx, "ACCENT COLOR", sx, sy, COL_TEXT_DIM);
        sy += 12;
        ctx.fill(sx, sy, sx + rowW, sy + 24, COL_CARD);
        int[] colors = {0xFF5B8AE0, 0xFF00AAFF, 0xFF44BB66, 0xFFAA44DD, 0xFFFFAA00, 0xFFE05A7A};
        for (int i = 0; i < colors.length; i++) {
            int cx2 = sx + 10 + i * 22;
            ctx.fill(cx2, sy + 7, cx2 + 16, sy + 23, colors[i]);
        }
    }

// ── Mouse events ──────────────────────────────────────────────

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        // Извлекаем координаты и кнопку из объекта Click
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button != 0) return super.mouseClicked(click, bl);

        int x = this.winX;
        int y = this.winY;
        int w = this.winW;
        int h = this.winH;

        // =====================================================================
        // 1. КЛИКИ ПО БОКОВОЙ ПАНЕЛИ (ВКЛАДКИ / ТАБЫ)
        // =====================================================================
        if (mouseX >= x && mouseX <= x + SIDEBAR_W) {
            GuiManager.Tab[] tabs = GuiManager.Tab.SIDEBAR;
            for (int i = 0; i < tabs.length; i++) {
                int slotY = y + TAB_START_Y + i * TAB_SLOT_H;
                if (mouseY >= slotY && mouseY <= slotY + TAB_SLOT_H) {
                    this.activeTab = tabs[i];
                    this.selectedModule = null;
                    this.gridScrollY = 0;
                    return true;
                }
            }

            int userTop = y + h - USER_AREA_H - 4;
            if (mouseY >= userTop - 2 && mouseY <= y + h) {
                this.activeTab = GuiManager.Tab.SETTINGS;
                this.selectedModule = null;
                return true;
            }
        }

        // =====================================================================
        // 2. КЛИКИ ПО МОДУЛЯМ (СЕТКА СЛЕВА)
        // =====================================================================
        Module.Category cat = categoryForTab(activeTab);
        int gridW = w - SETTINGS_W;
        int listTop = y + PAD + SEARCH_BAR_H;
        int listBottom = y + h - PAD;

        if (cat != null && mouseX >= x + SIDEBAR_W && mouseX <= x + gridW) {
            List<Module> mods = getFilteredMods(cat);
            int cardW = moduleCardWidth(gridW);

            for (int i = 0; i < mods.size(); i++) {
                int row = i / MODULE_COLS;
                int cx2 = x + SIDEBAR_W + PAD;
                int cy2 = listTop + row * (CARD_H + CARD_GAP) - gridScrollY;

                if (cy2 + CARD_H < listTop || cy2 > listBottom) continue;

                if (mouseX >= cx2 && mouseX <= cx2 + cardW &&
                    mouseY >= cy2 && mouseY <= cy2 + CARD_H) {
                    
                    Module m = mods.get(i);
                    if (this.selectedModule == m) {
                        m.toggle();
                        com.neverlose.client.module.ModuleManager.onModuleStateChanged(m, m.isEnabled());
                    } else {
                        this.selectedModule = m;
                    }
                    return true;
                }
            }
        }

        // =====================================================================
        // 3. КЛИКИ ПО НАСТРОЙКАМ (ПАНЕЛЬ СПРАВА)
        // =====================================================================
        int settingsPanelX = x + gridW;
        if (this.selectedModule != null && mouseX >= settingsPanelX && mouseX <= x + w) {
            int sy = y + 36;
            int trackWidth = SETTINGS_W - 8;

            for (Module.Setting<?> s : this.selectedModule.settings) {
                if (s instanceof Module.ToggleSetting ts) {
                    int toggleX = settingsPanelX + SETTINGS_W - 30;
                    if (mouseX >= toggleX && mouseX <= toggleX + 22 &&
                        mouseY >= sy + 2 && mouseY <= sy + 12) {
                        ts.setValue(!ts.getValue());
                        return true;
                    }
                } else if (s instanceof Module.SliderSetting ss) {
                    int trackX = settingsPanelX + 4;
                    int trackY = sy + 10;

                    if (mouseX >= trackX && mouseX <= trackX + trackWidth &&
                        mouseY >= trackY - 3 && mouseY <= trackY + 7) {
                        
                        this.activeSlider = ss;
                        updateSliderValue(ss, mouseX, trackX, trackWidth);
                        return true;
                    }
                }
                
                sy += 22;
                if (sy > y + h - 6) break;
            }
        }

        // Клик по кнопке "Save"
        int bx = x + w - 6 - 44;
        int by = y + 2;
        if (mouseX >= bx && mouseX <= bx + 42 && mouseY >= by && mouseY <= by + 12) {
            this.saveFlashTimer = 40;
            return true;
        }

        // Клик по кнопке закрытия "x"
        if (mouseX >= x + w - 20 && mouseX <= x + w - 4 && mouseY >= by && mouseY <= by + 12) {
            this.close();
            return true;
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (this.activeSlider != null) {
            double mouseX = click.x(); // Получаем X координату перетаскивания
            int gridW = this.winW - SETTINGS_W;
            int settingsPanelX = this.winX + gridW;
            
            int trackX = settingsPanelX + 4;
            int trackWidth = SETTINGS_W - 8;
            
            updateSliderValue(this.activeSlider, mouseX, trackX, trackWidth);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        this.activeSlider = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        Module.Category scrollCat = categoryForTab(activeTab);
        if (scrollCat != null &&
            mx >= winX + SIDEBAR_W && mx <= winX + winW - SETTINGS_W) {
            int gridW = winW - SIDEBAR_W - SETTINGS_W;
            int contentH = getModuleGridContentHeight(scrollCat, gridW);
            int listH = winH - TOPBAR_H - PAD * 2 - SEARCH_BAR_H;
            int maxScroll = Math.max(0, contentH - listH);
            gridScrollY = Math.max(0, Math.min(maxScroll, gridScrollY - (int)(vScroll * 12)));
        }
        return true;
    }

    private int moduleCardWidth(int gridW) {
        return gridW - PAD * 2;
    }

    private int getModuleGridContentHeight(Module.Category cat, int gridW) {
        List<Module> mods = getFilteredMods(cat);
        int rows = mods.isEmpty() ? 0 : (mods.size() + MODULE_COLS - 1) / MODULE_COLS;
        return rows > 0 ? rows * (CARD_H + CARD_GAP) - CARD_GAP : 0;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        int keyCode = key.key();
        if (keyCode == GLFW.GLFW_KEY_INSERT) {
            this.close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            return true;
        }
        return super.keyPressed(key);
    }

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

    private String getPlayerName() {
        if (this.client == null) {
            return "Player";
        }
        if (this.client.player != null) {
            return this.client.player.getName().getString();
        }
        return this.client.getSession().getUsername();
    }

    private String truncate(String text, int maxPx) {
        if (text == null || text.isEmpty()) return "";
        if (textWidth(text) <= maxPx) return text;
        String ellipsis = "..";
        int limit = Math.max(1, text.length());
        while (limit > 0 && textWidth(text.substring(0, limit) + ellipsis) > maxPx) {
            limit--;
        }
        return text.substring(0, limit) + ellipsis;
    }

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
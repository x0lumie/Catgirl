package lol.catgirl.ui.click.dropdown;

import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.ClickGuiModule;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.client.Animation;
import lol.catgirl.utils.client.Easing;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class CatgirlDropdown extends Screen implements IMinecraft {

    private static String searchQuery = "";
    private final Animation searchBarAnimation = new Animation(Easing.DECELERATE, 250L);

    public CatgirlDropdown() {
        super(Component.empty());
        searchQuery = "";
    }

    public static String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        DrawUtil.begin();

        boolean searchActive = !searchQuery.isEmpty();
        searchBarAnimation.run(searchActive ? 1f : 0f);
        float searchProgress = searchBarAnimation.getValue();

        if (searchProgress > 0.01f) {
            long vg = DrawUtil.context;
            float screenWidth = this.width;

            float boxWidth = 150f;
            float boxHeight = 18f;
            float x = (screenWidth / 2f) - (boxWidth / 2f);
            float y = -boxHeight + (boxHeight + 10f) * searchProgress;

            Color searchBgColor = new Color(0, 0, 0, 250);
            DrawUtil.roundedRect(x, y, x + boxWidth, y + boxHeight, 4f, searchBgColor);

            String displayText = searchQuery;
            DrawUtil.drawString(displayText, x + 6f, y + 12f, 9f, Color.WHITE, ResourceManager.getSelectedFont());

            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                float textWidth = (float) DrawUtil.getStringWidth(displayText, 9f, ResourceManager.getSelectedFont());
                DrawUtil.roundedRect(x + 6f + textWidth + 2f, y + 4f, x + 6f + textWidth + 3.5f, y + boxHeight - 4f, 0.5f, Color.WHITE);
            }
        }

        float defaultOffsetX = 15;
        float defaultOffsetY = 50;
        int index = 0;

        for (ModuleCategory category : ModuleCategory.values()) {
//            if (category == ModuleCategory.Ghost) continue;

            float startX = defaultOffsetX + (index * 110);
            CatgirlPanel.draw(category, startX, defaultOffsetY, mouseX, mouseY, partialTick);

            index++;
        }

        DrawUtil.end();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            }
        }
        else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = "";
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int codePoint = event.codepoint();

        if ((codePoint >= 'a' && codePoint <= 'z')
                || (codePoint >= 'A' && codePoint <= 'Z')
                || (codePoint >= '0' && codePoint <= '9')
                || codePoint == ' ')
        {
            char character = (char) codePoint;
            searchQuery += Character.toLowerCase(character);
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        ClickGuiModule.INSTANCE.toggle();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean b) {
        float mouseX = (float) event.x();
        float mouseY = (float) event.y();

        float defaultOffsetX = 15;
        float defaultOffsetY = 50;
        int index = 0;

        for (ModuleCategory category : ModuleCategory.values()) {
            if (category == ModuleCategory.Ghost) continue;

            float startX = defaultOffsetX + (index * 110);
            if (CatgirlPanel.mouseClicked(category, startX, defaultOffsetY, mouseX, mouseY, event.button())) {
                break;
            }
            index++;
        }

        return super.mouseClicked(event, b);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        CatgirlPanel.mouseReleased();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        float defaultOffsetX = 15;
        float defaultOffsetY = 50;
        int index = 0;

        for (ModuleCategory category : ModuleCategory.values()) {
            if (category == ModuleCategory.Ghost) continue;

            float startX = defaultOffsetX + (index * 110);
            java.awt.Point.Float pos = CatgirlPanel.getPosition(category, startX, defaultOffsetY);
            float activeHeight = CatgirlPanel.isCollapsed(category) ? 18f : 218f;

            if (mouseX >= pos.x && mouseX <= pos.x + 100 && mouseY >= pos.y && mouseY <= pos.y + activeHeight) {
                CatgirlPanel.handleScroll(category, deltaY);
                return true;
            }
            index++;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {}

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}
}
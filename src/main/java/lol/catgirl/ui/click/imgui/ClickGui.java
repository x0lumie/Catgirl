package lol.catgirl.ui.click.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.flag.ImGuiCond;
import imgui.type.ImFloat;
import imgui.type.ImString;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.Property;
import lol.catgirl.property.impl.*;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import static lol.catgirl.utils.IMinecraft.mc;

public class ClickGui extends Screen {
    private final ImString searchText = new ImString(500);
    private ModuleCategory moduleCategory = ModuleCategory.values()[0];
    private lol.catgirl.module.Module keyBindingModule = null;
    private lol.catgirl.module.Module module;

    private final float windowWidth = 650.0f;
    private final float windowHeight = 450.0f;

    public ClickGui() {
        super(Component.empty());
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        DrawUtil.begin();

        ImGuiImpl.render(io -> {
            int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar;

            ImGui.setNextWindowSize(windowWidth, windowHeight, ImGuiCond.FirstUseEver);

            if (ImGui.begin("Catgirl", windowFlags)) {

                if (ImGui.beginTabBar("Main")) {
                    for (ModuleCategory category : ModuleCategory.values()) {
                        if (ImGui.beginTabItem(category.name() + "##tab")) {
                            moduleCategory = category;
                            ImGui.endTabItem();
                        }
                    }
                    ImGui.endTabBar();
                }

                if (moduleCategory != null) {
                    for (lol.catgirl.module.Module module : ModuleManager.getInstance().getModulesByCategory(moduleCategory)) {
                        ImGui.setCursorPosX(ImGui.getCursorPosX() + 25);
                        if (ImGui.collapsingHeader(module.getDisplayName())) {
                            drawToggle(module);

                            this.module = module;

                            ImGui.text(module.getDescription());

                            ImGui.sameLine();

                            String keyName = GLFW.glfwGetKeyName(module.getKey(), 0);

                            if (ImGui.button(
                                    keyBindingModule == module
                                            ? "Listening..."
                                            : "Key " + (keyName != null ? keyName.toLowerCase() : "none"))) {
                                keyBindingModule = (keyBindingModule == module) ? null : module;
                            }

                            ImGui.separator();

                            for (Property<?> property : module.getProperties()) {
                                if (property.isHidden()) continue;

                                if (property instanceof BoolProperty booleanSetting) {
                                    if (ImGui.checkbox(property.getName(), booleanSetting.getValue())) {
                                        booleanSetting.setValue(!booleanSetting.getValue());
                                    }
                                }

                                if (property instanceof SliderProperty numberProperty) {
                                    ImFloat imFloat = new ImFloat((float) numberProperty.getValue());

                                    if (ImGui.sliderFloat("##" + numberProperty.getName(), imFloat.getData(), (float) numberProperty.getMin(), (float) numberProperty.getMax())) {
                                        numberProperty.setValue(imFloat.get());
                                    }

                                    ImGui.sameLine();
                                    ImGui.text(numberProperty.getName());

                                    imFloat.getData()[0] = (float) numberProperty.getValue();
                                }

                                if (property instanceof EnumProperty<?> modeProperty) {
                                    String propertyName = modeProperty.getName();
                                    String comboId = "##" + propertyName + "_" + module.getDisplayName();

                                    String previewValue = String.valueOf(modeProperty.getValue());

                                    if (ImGui.beginCombo(comboId, previewValue)) {
                                        ImGui.inputTextWithHint(comboId + "_search", "Search For Modes...", searchText, ImGuiInputTextFlags.None);
                                        String search = searchText.get().toLowerCase();

                                        for (Enum<?> mode : modeProperty.getModes()) {
                                            String modeName = mode.toString();

                                            if (search.isEmpty() || modeName.toLowerCase().contains(search)) {
                                                boolean isSelected = modeProperty.getValue() == mode;

                                                if (ImGui.selectable(modeName, isSelected)) {
                                                    modeProperty.setValueByEnum(mode);

                                                    searchText.clear();
                                                }

                                                if (isSelected) {
                                                    ImGui.setItemDefaultFocus();
                                                }
                                            }
                                        }
                                        ImGui.endCombo();
                                    }

                                    ImGui.sameLine();
                                    ImGui.text(propertyName);
                                }
                            }
                        } else {
                            drawToggle(module);
                        }
                    }
                }
            }
            ImGui.end();
        });
        DrawUtil.end();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keycode = event.key();

        if (keycode == GLFW.GLFW_KEY_ESCAPE && keyBindingModule == null) {
            mc.setScreen(null);
        }

        if (keyBindingModule != null) {
            keyBindingModule.setKey(keycode == GLFW.GLFW_KEY_ESCAPE ? 0 : keycode);
            keyBindingModule = null;
        }

        return false;
    }

    public void drawToggle(lol.catgirl.module.Module module) {
        ImGui.sameLine(-16);

        ImGui.setCursorPosX(ImGui.getCursorPosX() + 20);
        if (ImGui.checkbox("##T" + module.getDisplayName(), module.isEnabled())) {
            module.toggle();
        }
    }
}
package lol.catgirl.ui.click.imgui;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImString;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.Setting;
import lol.catgirl.setting.impl.BoolSetting;
import lol.catgirl.setting.impl.EnumSetting;
import lol.catgirl.setting.impl.SliderSetting;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import static lol.catgirl.utils.IMinecraft.mc;

public class ClickGui extends Screen {
    private final ImString searchText = new ImString(500);
    private ModuleCategory moduleCategory;
    private lol.catgirl.module.Module keyBindingModule = null;
    private lol.catgirl.module.Module module;

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

            if (ImGui.begin("Catgirl", windowFlags)) {
                for (ModuleCategory moduleCategory1 : ModuleCategory.values()) {
                    ImGui.beginTabBar("Main");

                    if (ImGui.beginTabItem(moduleCategory1.name() + "##tab")) {
                        moduleCategory = moduleCategory1;
                        ImGui.endTabItem();
                    }

                    ImGui.endTabBar();
                }

                for (lol.catgirl.module.Module module : ModuleManager.getInstance().getModulesByCategory(moduleCategory)) {
                    ImGui.setCursorPosX(ImGui.getCursorPosX() + 25);
                    if (ImGui.collapsingHeader(module.getName())) {
                        drawToggle(module);

                        this.module = module;

                        ImGui.text(module.getDescription());

                        ImGui.sameLine();

                        if (ImGui.button(keyBindingModule == module ? "Listening..." : "Key " + GLFW.glfwGetKeyName(module.getKey(), 0))) {
                            keyBindingModule = (keyBindingModule == module) ? null : module;
                        }

                        ImGui.separator();

                        for (Setting<?> property : module.getSettings()) {

                            if (property instanceof BoolSetting booleanSetting) {
                                if (ImGui.checkbox(property.getName(), booleanSetting.getValue())) {
                                    booleanSetting.setValue(!booleanSetting.getValue());
                                }
                            }

                            if (property instanceof SliderSetting numberProperty) {
                                ImFloat imFloat = new ImFloat((float) numberProperty.getValue());

                                if (ImGui.sliderFloat("##" + numberProperty.getName(), imFloat.getData(), (float) numberProperty.getMin(), (float) numberProperty.getMax())) {
                                    numberProperty.setValue(imFloat.get());
                                }

                                ImGui.sameLine();
                                ImGui.text(numberProperty.getName());

                                imFloat.getData()[0] = (float) numberProperty.getValue();
                            }

                            if (property instanceof EnumSetting<?> modeProperty) {
                                String propertyName = modeProperty.getName();
                                String comboId = "##" + propertyName + "_" + module.getName();

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
        if (ImGui.checkbox("##T" + module.getName(), module.isEnabled())) {
            module.toggle();
        }
    }
}
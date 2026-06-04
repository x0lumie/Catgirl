package lol.catgirl.command.impl;

import lol.catgirl.Catgirl;
import lol.catgirl.manager.ModuleManager;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import lol.catgirl.module.Module;
import lol.catgirl.command.Command;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a module to a key.", "b");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            Module module = ModuleManager.getInstance().getModule(args[0]);
            String keyName = args[1].toUpperCase();
            if (module != null) {
                int keyCode = stringToGlfwKey(keyName);
                module.setKey(keyCode);
                Catgirl.sendChatMessage("Bound " + module.getName() + " to " + keyName + ".");
            } else {
                Catgirl.sendChatMessage("Module \"" + args[0] + "\" was not found.");
            }
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                ModuleManager.modules.forEach(m -> m.setKey(GLFW.GLFW_KEY_UNKNOWN));
                Catgirl.sendChatMessage("Cleared all binds.");
            } else if (args[0].equalsIgnoreCase("list")) {
                final List<Module> boundModules = ModuleManager.modules.stream().filter(m -> m.getKey() != GLFW.GLFW_KEY_UNKNOWN).toList();
                if (!boundModules.isEmpty()) {
                    Catgirl.sendChatMessage("Current binds: ");
                    for (Module module : boundModules) {
                        Catgirl.sendChatMessage(module.getName() + ": " + module.getKey());
                    }
                } else {
                    Catgirl.sendChatMessage("No modules are currently bound.");
                }
            }
            return;
        }
    }


    public static int stringToGlfwKey(String keyName) {
        return switch (keyName.toUpperCase()) {
            case "A" -> GLFW.GLFW_KEY_A;
            case "B" -> GLFW.GLFW_KEY_B;
            case "C" -> GLFW.GLFW_KEY_C;
            case "D" -> GLFW.GLFW_KEY_D;
            case "E" -> GLFW.GLFW_KEY_E;
            case "F" -> GLFW.GLFW_KEY_F;
            case "G" -> GLFW.GLFW_KEY_G;
            case "H" -> GLFW.GLFW_KEY_H;
            case "I" -> GLFW.GLFW_KEY_I;
            case "J" -> GLFW.GLFW_KEY_J;
            case "K" -> GLFW.GLFW_KEY_K;
            case "L" -> GLFW.GLFW_KEY_L;
            case "M" -> GLFW.GLFW_KEY_M;
            case "N" -> GLFW.GLFW_KEY_N;
            case "O" -> GLFW.GLFW_KEY_O;
            case "P" -> GLFW.GLFW_KEY_P;
            case "Q" -> GLFW.GLFW_KEY_Q;
            case "R" -> GLFW.GLFW_KEY_R;
            case "S" -> GLFW.GLFW_KEY_S;
            case "T" -> GLFW.GLFW_KEY_T;
            case "U" -> GLFW.GLFW_KEY_U;
            case "V" -> GLFW.GLFW_KEY_V;
            case "W" -> GLFW.GLFW_KEY_W;
            case "X" -> GLFW.GLFW_KEY_X;
            case "Y" -> GLFW.GLFW_KEY_Y;
            case "Z" -> GLFW.GLFW_KEY_Z;
            case "0" -> GLFW.GLFW_KEY_0;
            case "1" -> GLFW.GLFW_KEY_1;
            case "2" -> GLFW.GLFW_KEY_2;
            case "3" -> GLFW.GLFW_KEY_3;
            case "4" -> GLFW.GLFW_KEY_4;
            case "5" -> GLFW.GLFW_KEY_5;
            case "6" -> GLFW.GLFW_KEY_6;
            case "7" -> GLFW.GLFW_KEY_7;
            case "8" -> GLFW.GLFW_KEY_8;
            case "9" -> GLFW.GLFW_KEY_9;
            case "F1" -> GLFW.GLFW_KEY_F1;
            case "F2" -> GLFW.GLFW_KEY_F2;
            case "F3" -> GLFW.GLFW_KEY_F3;
            case "F4" -> GLFW.GLFW_KEY_F4;
            case "F5" -> GLFW.GLFW_KEY_F5;
            case "F6" -> GLFW.GLFW_KEY_F6;
            case "F7" -> GLFW.GLFW_KEY_F7;
            case "F8" -> GLFW.GLFW_KEY_F8;
            case "F9" -> GLFW.GLFW_KEY_F9;
            case "F10" -> GLFW.GLFW_KEY_F10;
            case "F11" -> GLFW.GLFW_KEY_F11;
            case "F12" -> GLFW.GLFW_KEY_F12;
            case "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LCONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCONTROL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "LSUPER" -> GLFW.GLFW_KEY_LEFT_SUPER;
            case "RSUPER" -> GLFW.GLFW_KEY_RIGHT_SUPER;
            case "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "DELETE" -> GLFW.GLFW_KEY_DELETE;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "PAGE_UP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PAGE_DOWN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "CAPS_LOCK" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "SCROLL_LOCK" -> GLFW.GLFW_KEY_SCROLL_LOCK;
            case "NUM_LOCK" -> GLFW.GLFW_KEY_NUM_LOCK;
            case "PRINT_SCREEN" -> GLFW.GLFW_KEY_PRINT_SCREEN;
            case "PAUSE" -> GLFW.GLFW_KEY_PAUSE;
            case "KP_0" -> GLFW.GLFW_KEY_KP_0;
            case "KP_1" -> GLFW.GLFW_KEY_KP_1;
            case "KP_2" -> GLFW.GLFW_KEY_KP_2;
            case "KP_3" -> GLFW.GLFW_KEY_KP_3;
            case "KP_4" -> GLFW.GLFW_KEY_KP_4;
            case "KP_5" -> GLFW.GLFW_KEY_KP_5;
            case "KP_6" -> GLFW.GLFW_KEY_KP_6;
            case "KP_7" -> GLFW.GLFW_KEY_KP_7;
            case "KP_8" -> GLFW.GLFW_KEY_KP_8;
            case "KP_9" -> GLFW.GLFW_KEY_KP_9;
            case "KP_DECIMAL" -> GLFW.GLFW_KEY_KP_DECIMAL;
            case "KP_DIVIDE" -> GLFW.GLFW_KEY_KP_DIVIDE;
            case "KP_MULTIPLY" -> GLFW.GLFW_KEY_KP_MULTIPLY;
            case "KP_SUBTRACT" -> GLFW.GLFW_KEY_KP_SUBTRACT;
            case "KP_ADD" -> GLFW.GLFW_KEY_KP_ADD;
            case "KP_ENTER" -> GLFW.GLFW_KEY_KP_ENTER;
            case "KP_EQUAL" -> GLFW.GLFW_KEY_KP_EQUAL;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "APOSTROPHE" -> GLFW.GLFW_KEY_APOSTROPHE;
            case "COMMA" -> GLFW.GLFW_KEY_COMMA;
            case "MINUS" -> GLFW.GLFW_KEY_MINUS;
            case "PERIOD" -> GLFW.GLFW_KEY_PERIOD;
            case "SLASH" -> GLFW.GLFW_KEY_SLASH;
            case "SEMICOLON" -> GLFW.GLFW_KEY_SEMICOLON;
            case "EQUAL" -> GLFW.GLFW_KEY_EQUAL;
            case "LEFT_BRACKET" -> GLFW.GLFW_KEY_LEFT_BRACKET;
            case "RIGHT_BRACKET" -> GLFW.GLFW_KEY_RIGHT_BRACKET;
            case "BACKSLASH" -> GLFW.GLFW_KEY_BACKSLASH;
            case "GRAVE_ACCENT" -> GLFW.GLFW_KEY_GRAVE_ACCENT;
            case "WORLD_1" -> GLFW.GLFW_KEY_WORLD_1;
            case "WORLD_2" -> GLFW.GLFW_KEY_WORLD_2;
            default -> -1;
        };
    }

}

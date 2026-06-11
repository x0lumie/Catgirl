package lol.catgirl.module.client;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientBrandRetrieverEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;

public final class ClientSpooferModule extends Module {
    public static final ClientSpooferModule INSTANCE = new ClientSpooferModule();

    // add more pwease
    public enum Mode {
        LunarClient
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.LunarClient);

    public ClientSpooferModule() {
        super("ClientSpoofer", "Spoofs your client.", ModuleCategory.Client);
        addSetting(mode);
    }

    @EventHook
    public void onClientBrand(ClientBrandRetrieverEvent event) {
        if (mode.getValue() == Mode.LunarClient) {
            event.callback.setReturnValue("lunarclient:v2.21.5-2540");
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}

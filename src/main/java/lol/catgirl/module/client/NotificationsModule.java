package lol.catgirl.module.client;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.ui.notification.NotificationManager;

public class NotificationsModule extends Module {
    public static final NotificationsModule INSTANCE = new NotificationsModule();

    public enum Mode {
        None,
        Exhibition
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Style", Mode.Exhibition);

    public NotificationsModule() {
        super("Notifications", "Shows client-side visual notifications.",
                ModuleCategory.Client);
        addSetting(mode);
    }

    @EventHook
    public void onRender(Render2DEvent event) {
        if(!this.isEnabled()) return;
        if(mode.getValue() == Mode.None) return;

        NotificationManager.render(event.context);
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}

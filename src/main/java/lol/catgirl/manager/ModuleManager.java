package lol.catgirl.manager;

import java.util.ArrayList;
import java.util.List;

import lol.catgirl.Catgirl;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.*;
import lol.catgirl.module.combat.*;
import lol.catgirl.module.ghost.*;
import lol.catgirl.module.hud.*;
import lol.catgirl.module.movement.*;
import lol.catgirl.module.player.*;
import lol.catgirl.module.render.*;
import lombok.Getter;

@Getter
public final class ModuleManager {
    @Getter
    private static final ModuleManager instance = new ModuleManager();

    public static ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        modules.add(SprintModule.INSTANCE);
        modules.add(MovementFixModule.INSTANCE);
        modules.add(AutoTotemModule.INSTANCE);
        modules.add(RegenModule.INSTANCE);
        modules.add(VelocityModule.INSTANCE);
        modules.add(AuraModule.INSTANCE);
        modules.add(ClickGuiModule.INSTANCE);
        modules.add(InterfaceModule.INSTANCE);
        modules.add(WatermarkModule.INSTANCE);
        modules.add(EagleModule.INSTANCE);
        modules.add(FastThrowModule.INSTANCE);
        modules.add(ModuleListModule.INSTANCE);
        modules.add(NoWebModule.INSTANCE);
        modules.add(SpeedModule.INSTANCE);
        modules.add(TickShiftModule.INSTANCE);
        modules.add(TargetsModule.INSTANCE);
        modules.add(TargetHUDModule.INSTANCE);
        modules.add(NoFallModule.INSTANCE);
        modules.add(AntiFireballModule.INSTANCE);
        modules.add(CriticalsModule.INSTANCE);
        modules.add(NoSlowModule.INSTANCE);
        modules.add(ScaffoldModule.INSTANCE);
        modules.add(TimerModule.INSTANCE);
        modules.add(ChestStealerModule.INSTANCE);
        modules.add(AutoComboModule.INSTANCE);
        modules.add(NoJumpDelayModule.INSTANCE);
        modules.add(AutoClickerModule.INSTANCE);
        modules.add(AimAssistModule.INSTANCE);
        modules.add(NoPushModule.INSTANCE);
        modules.add(AnimationsModule.INSTANCE);
        modules.add(AutoArmorModule.INSTANCE);
        modules.add(InventoryManagerModule.INSTANCE);
        modules.add(InventoryMoveModule.INSTANCE);
        modules.add(ChinaHatModule.INSTANCE);
        modules.add(FullbrightModule.INSTANCE);
        modules.add(PlayerESPModule.INSTANCE);
        modules.add(StorageESPModule.INSTANCE);
        modules.add(AntiBotModule.INSTANCE);
        modules.add(OreESPModule.INSTANCE);
        modules.add(FriendsModule.INSTANCE);
        modules.add(AutoDisableModule.INSTANCE);
        modules.add(SaveMoveKeysModule.INSTANCE);
        modules.add(AirStuckModule.INSTANCE);
        modules.add(HitboxDesyncModule.INSTANCE);
        modules.add(PanicModule.INSTANCE);
        modules.add(InsultsModule.INSTANCE);
        modules.add(NoRotateModule.INSTANCE);
        modules.add(FunnyDisplayerModule.INSTANCE);
        modules.add(WallClimbModule.INSTANCE);
        modules.add(TriggerBotModule.INSTANCE);
        modules.add(AttributeSwapModule.INSTANCE);
        modules.add(ElytraFlyModule.INSTANCE);
        modules.add(NoRenderModule.INSTANCE);
        modules.add(HotbarModule.INSTANCE);
        modules.add(AutoTranslateModule.INSTANCE);
        modules.add(NametagsModule.INSTANCE);
        modules.add(MCFModule.INSTANCE);
        modules.add(ResourcePackSpoofModule.INSTANCE);
        modules.add(ClickTPModule.INSTANCE);
        modules.add(BlockOverlayModule.INSTANCE);
        modules.add(SpammerModule.INSTANCE);
        modules.add(MCPModule.INSTANCE);
        modules.add(AutoAuthModule.INSTANCE);
        modules.add(AutoWalkModule.INSTANCE);
        modules.add(FastStopModule.INSTANCE);
        modules.add(HealthAlertModule.INSTANCE);
        modules.add(ChestAuraModule.INSTANCE);
        modules.add(NotificationsModule.INSTANCE);
        modules.add(AutoPotModule.INSTANCE);
        modules.add(RefillModule.INSTANCE);
        modules.add(PingSpoofModule.INSTANCE);
        modules.add(AutoCrafterModule.INSTANCE);
        modules.add(AutoHeadHitterModule.INSTANCE);
        modules.add(AutoDrainModule.INSTANCE);
        modules.add(AutoDoubleHandModule.INSTANCE);
        modules.add(AutoToolModule.INSTANCE);
        modules.add(BacktrackModule.INSTANCE);

        Catgirl.LOGGER.info("Initializing " + modules.size() +  " Modules...");
    }

    public List<Module> getModulesByCategory(ModuleCategory moduleCategory) {
        List<Module> result = new ArrayList<>();
        for(Module module : modules) {
            if(module.getCategory() == moduleCategory) {
                result.add(module);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <M extends Module> M getModule(String module) {
        return (M) modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(module))
                .findFirst()
                .orElse(null);
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().trim().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }
}

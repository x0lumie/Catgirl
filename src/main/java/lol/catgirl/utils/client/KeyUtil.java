package lol.catgirl.utils.client;

import com.mojang.blaze3d.platform.InputConstants;
import lol.catgirl.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public class KeyUtil {

    public static void pressKey(KeyMapping key) {
        InputConstants.Key bind = ((KeyMappingAccessor) key).getKey();
        KeyMapping.click(bind);
    }
}

package lol.catgirl.utils.render.nanovg;

import lol.catgirl.module.client.InterfaceModule;

import java.io.IOException;

public class ResourceManager {

    public static void init() throws IOException {
        FontResources.init();
    }

    public static class FontResources {
        public static FontResource regular;
        public static FontResource productSansBold, minecraft,
        comfortaa, comfortaaBold, sfprobold, roboto,
        segoeUiLightItalic
                ;

        public static void init() throws IOException {
            regular = new FontResource("regular");
            productSansBold = new FontResource("ProductSans-Bold");
            minecraft = new FontResource("Mojangles");
            comfortaa = new FontResource("Comfortaa");
            comfortaaBold = new FontResource("Comfortaa-Bold");
            sfprobold = new FontResource("SF-Pro-Display-Bold");
            roboto = new FontResource("roboto");
            segoeUiLightItalic = new FontResource("segoe-ui-light-italic");
        }
    }

    public static FontResource getSelectedFont() {
        InterfaceModule.FontMode mode =
                InterfaceModule.INSTANCE.fontMode.getValue();

        return switch (mode) {
            case ProductSans -> FontResources.productSansBold;
            case Minecraft -> FontResources.minecraft;
            case Comfortaa -> FontResources.comfortaa;
            case ComfortaaBold -> FontResources.comfortaaBold;
            case SFProDisplayBold -> FontResources.sfprobold;
            case Roboto -> FontResources.roboto;
            case ProductSansRegular -> FontResources.regular;
            case SegoeLight -> FontResources.segoeUiLightItalic;
        };
    }

    public static String getSelectedFontAsString() {
        InterfaceModule.FontMode mode =
                InterfaceModule.INSTANCE.fontMode.getValue();

        return switch (mode) {
            case ProductSans -> "ProductSans-Bold";
            case Minecraft -> "Mojangles";
            case Comfortaa -> "Comfortaa";
            case ComfortaaBold -> "Comfortaa-Bold";
            case SFProDisplayBold -> "SF-Pro-Display-Bold";
            case Roboto -> "roboto";
            case ProductSansRegular -> "regular";
            case SegoeLight -> "segoe-ui-light-italic";
        };
    }
}
package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.StringReplacementEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.StringDecomposer;

@Mixin(StringDecomposer.class)
public abstract class StringDecomposerMixin
{
	@ModifyArg(
		method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
			ordinal = 0),
		index = 0)
	private static String adjustText(String text)
	{
		StringReplacementEvent event = new StringReplacementEvent(text);
		Catgirl.INSTANCE.eventBus.post(event);

		if (event.isCancelled()) return "";
		return event.text;
	}
}
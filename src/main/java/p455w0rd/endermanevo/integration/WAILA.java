package p455w0rd.endermanevo.integration;

import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import p455w0rd.endermanevo.entity.EntityFrienderman;
import p455w0rd.endermanevo.init.ModIntegration.Mods;
import p455w0rd.endermanevo.init.ModLogger;
import p455w0rd.endermanevo.integration.waila.WAILAProviderFrienderman;

public class WAILA {

	public static String toolTipEnclose = TextFormatting.BOLD + "" + TextFormatting.GREEN + "=====================";
	public static String doSneak = TextFormatting.BOLD + "" + TextFormatting.ITALIC + "<Sneak for Info> ";

	public static void init() {
		ModLogger.info("Waila Integation: Enabled");
		FMLInterModComms.sendMessage(Mods.WAILA.getId(), "register", WAILA.class.getName() + ".callbackRegister");
	}

	public static void callbackRegister(IWailaRegistrar registrar) {
		registrar.registerBodyProvider(new WAILAProviderFrienderman(), EntityFrienderman.class);
	}

}

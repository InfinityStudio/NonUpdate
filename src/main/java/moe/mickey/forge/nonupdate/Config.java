package moe.mickey.forge.nonupdate;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
	
	private static Configuration configuration = new Configuration(new File("config/" + NonUpdate.MOD_NAME + ".cfg"));
	
	public static void init() {
		configuration.load();
		loadConfiguration();
		configuration.save();
	}
	
	static boolean onlyPreventMainThread;
	static String redirectAddress;
	
	private static void loadConfiguration() {
		onlyPreventMainThread = configuration.getBoolean("onlyPreventMainThread", "Thread", false, "Only prevent main thread url access.");
		redirectAddress = configuration.getString("redirectAddress", "Address", "127.0.0.1", "Redirect address.");
	}

}
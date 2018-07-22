package nonupdate.forge;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
	
	private static Configuration configuration = new Configuration(new File("config/" + NonUpdate.MOD_NAME + ".cfg"));
	
	public static void init() {
		configuration.load();
		loadConfiguration();
		configuration.save();
	}
	
	static boolean onlyPreventMainThread = false;
	static String redirectAddress = /* "127.0.0.1"*/"0.0.0.0"; // https://github.com/InfinityStudio/NonUpdate/issues/4
	
	private static void loadConfiguration() {
		onlyPreventMainThread = configuration.getBoolean("onlyPreventMainThread", "Thread", onlyPreventMainThread,
				"Only prevent main thread url access.");
		redirectAddress = configuration.getString("redirectAddress", "Address", redirectAddress, "Redirect address.");
	}

}

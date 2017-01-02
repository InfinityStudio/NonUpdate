package moe.mickey.forge.nonupdate;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.relauncher.FMLSecurityManager.ExitTrappedException;

@Mod(modid = NonUpdate.MODID, version = NonUpdate.VERSION, dependencies = "before:*;")
public class NonUpdate {
	
	public static final String MODID = "non_update", VERSION = "1.0";
	
	public static final Logger logger = LogManager.getLogger(NonUpdate.class);
	
	@EventHandler
	public void init(FMLConstructionEvent event) {
		ReflectionHelper.set(ReflectionHelper.getField(System.class, "security"), new SecurityManager() {
			
			List<String> whitelist = getWhiteList();
			
			@Override
			public void checkConnect(String host, int port) {
				if (!whitelist.contains(host)) {
					logger.info("check: " + host);
					Tool.coverString(host, "127.0.0.1");
				}
			}
			
			@Override
			public void checkPermission(Permission perm) {
				String permName = perm.getName() != null ? perm.getName() : "missing";
				if (permName.startsWith("exitVM")) {
					Class<?>[] classContexts = getClassContext();
					String callingClass = classContexts.length > 4 ? classContexts[4].getName() : "none";
					String callingParent = classContexts.length > 5 ? classContexts[5].getName() : "none";
					// FML is allowed to call system exit and the Minecraft applet (from the quit button)
					if (!(callingClass.startsWith("net.minecraftforge.fml.")
							|| "net.minecraft.server.dedicated.ServerHangWatchdog$1".equals(callingClass)
							|| "net.minecraft.server.dedicated.ServerHangWatchdog".equals(callingClass)
							|| ( "net.minecraft.client.Minecraft".equals(callingClass) && "net.minecraft.client.Minecraft".equals(callingParent))
							|| ("net.minecraft.server.dedicated.DedicatedServer".equals(callingClass) && "net.minecraft.server.MinecraftServer".equals(callingParent)))
							)
						throw new ExitTrappedException();
				} else if ("setSecurityManager".equals(permName))
					throw new SecurityException("Cannot replace the FML security manager");
			}
			
			@Override
			public void checkPermission(Permission perm, Object context) { checkPermission(perm); }
			
		});
	}
	
	public static List<String> getWhiteList() {
		try {
			return Files.readLines(new File("nu-whitelist.txt"), Charsets.UTF_8);
		} catch (IOException e) {
			return ImmutableList.of();
		}
	}
	
}

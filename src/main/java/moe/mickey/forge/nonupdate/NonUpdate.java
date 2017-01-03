package moe.mickey.forge.nonupdate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLPermission;
import java.security.Permission;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.relauncher.FMLSecurityManager.ExitTrappedException;

@Config(modid = NonUpdate.MODID)
@Mod(modid = NonUpdate.MODID, version = NonUpdate.VERSION, dependencies = "before:*;")
public class NonUpdate {
	
	public static final String MODID = "non_update", VERSION = "1.0";
	
	public static final Logger logger = LogManager.getLogger(NonUpdate.class);
	
	private static final List<String> DEFAULT_WHITE_LIST = ImmutableList.of(
			"minecraft.net",
			"mojang.com",
			"skin.prinzeugen.net",
			"fleey.org",
			"www.skinme.cc"
	);
	
	public String getFMLPackageName() {
		return "net.minecraftforge.fml.";
	}
	
	@EventHandler
	public void init(FMLConstructionEvent event) {
		ReflectionHelper.set(ReflectionHelper.getField(System.class, "security"), new SecurityManager() {
			
			List<String> whitelist = getWhiteList();
			String redirectAddress = "127.0.0.1";
			boolean onlyPreventMainThread = false;
			
			@Override
			public void checkPermission(Permission perm) {
				if (perm instanceof URLPermission) {
					try {
						if (onlyPreventMainThread) {
							String name = Thread.currentThread().getName();
							if (!(name.equals("Client thread") || name.equals("Server thread")))
								return;
						}
						URL url = new URL(perm.getName());
						String host = url.getHost();
						for (String exp : whitelist)
							if (host.endsWith(exp))
								return;
						logger.info("Redirect: " + host + " -> " + redirectAddress);
						Tool.coverString(host, redirectAddress);
					} catch (Exception e) { logger.warn(e); }
					return;
				}
				String permName = perm.getName() != null ? perm.getName() : "missing";
				if (permName.startsWith("exitVM")) {
					Class<?>[] classContexts = getClassContext();
					String callingClass = classContexts.length > 4 ? classContexts[4].getName() : "none";
					String callingParent = classContexts.length > 5 ? classContexts[5].getName() : "none";
					// FML is allowed to call system exit and the Minecraft applet (from the quit button)
					if (!(callingClass.startsWith(getFMLPackageName())
							|| "net.minecraft.server.dedicated.ServerHangWatchdog$1".equals(callingClass)
							|| "net.minecraft.server.dedicated.ServerHangWatchdog".equals(callingClass)
							|| "net.minecraft.client.Minecraft".equals(callingClass) && "net.minecraft.client.Minecraft".equals(callingParent)
							|| "net.minecraft.server.dedicated.DedicatedServer".equals(callingClass) && "net.minecraft.server.MinecraftServer".equals(callingParent))
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
		File file = new File("nu-whitelist.txt");
		try {
			return Files.readLines(file, Charsets.UTF_8, new LineProcessor<List<String>>() {
				
				List<String> result = Lists.newArrayList();

				@Override
				public boolean processLine(String line) {
					if (!line.startsWith("#"))
						result.add(line);
					return true;
				}

				@Override
				public List<String> getResult() {
					return result;
				}
				
			});
		} catch (IOException e) {
			if (!file.isFile())
				try {
					if (file.createNewFile())
						Files.write(Joiner.on('\n').join(DEFAULT_WHITE_LIST), file, Charsets.UTF_8);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			return ImmutableList.of();
		}
	}
	
}

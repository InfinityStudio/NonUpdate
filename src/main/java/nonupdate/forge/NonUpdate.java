package nonupdate.forge;

import static nonupdate.forge.Config.*;
import static nonupdate.forge.NonUpdate.*;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;


// lower 1.7.10
@cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name(MOD_ID)
@cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions(MOD_PACKAGE)
// upper 1.8
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name(MOD_ID)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions(MOD_PACKAGE)
public class NonUpdate implements
	//lower 1.7.10
	cpw.mods.fml.relauncher.IFMLLoadingPlugin,
	//upper 1.8
	net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
{
	
	public static final String MOD_ID = "non_update", MOD_NAME = "NonUpdate", MOD_VERSION = "2.0.0", MOD_PACKAGE = "nonupdate.forge.";
	
	public static final Logger logger = LogManager.getLogger(NonUpdate.class.getSimpleName());
	
	private static final ImmutableList<String> DEFAULT_WHITE_LIST = ImmutableList.of(
			"# !!! DONT REMOVE THIS LINE !!! #",
			"minecraft.net",
			"mojang.com",
			"skin.prinzeugen.net",
			"fleey.org",
			"www.skinme.cc"
	);
	
	public static String[] getFMLPackageNames() {
		return new String[] { "net.minecraftforge.fml.", "cpw.mods.fml." };
	}
	
	public static ImmutableList<String> getWhiteList() {
		File file = new File("nu-whitelist.txt");
		try {
			ImmutableList.Builder<String> builder = ImmutableList.builder();
			logger.info("Load file: nu-whitelist.txt");
			String whitelist = Files.asCharSource(file, Charsets.UTF_8).read();
			whitelist = whitelist.replace("\r\n", "\n");
			for (String line : whitelist.split("\n")) {
				line = line.trim();
				if (!line.startsWith("#") && !line.endsWith("#") && !line.isEmpty()) {
					builder.add(line);
					logger.info(line);
				}
			}
			return builder.build();
		} catch (IOException e) {
			if (!file.isFile())
				try {
					if (file.createNewFile()) {
						String whitelist = Joiner.on('\n').join(DEFAULT_WHITE_LIST);
						logger.info("Create file: nu-whitelist.txt\n" + whitelist);
						Files.write(whitelist, file, Charsets.UTF_8);
						return DEFAULT_WHITE_LIST;
					}
				} catch (IOException ex) { ex.printStackTrace(); }
			e.printStackTrace();
			return ImmutableList.of();
		}
	}
	
	static {
		Config.init();
		ReflectionHelper.setSecurityManager(new SecurityManager() {
			
			List<String> whitelist = getWhiteList();
			
			String fmlPackageNames[] = getFMLPackageNames();
			
			private boolean isIP(String host) {
				return host.matches("[0-9.]*") || host.contains(":");
			}
			
			@Override
			public void checkConnect(String host, int port) {
				checkConnect(host, port, null);
			}
			
			@Override
			public void checkConnect(String host, int port, @Nullable Object context) {
				if (isIP(host) || port != 80/* http */ && port != 443/* https */) {
					logger.info("Release: " + host + ":" + port);
					return;
				}
				logger.info("Check: " + host + ":" + port);
				if (onlyPreventMainThread) {
					String name = Thread.currentThread().getName();
					if (!(name.equals("Client thread") || name.equals("Server thread"))) {
						logger.info("Release: " + host + ":" + port);
						return;
					}
				}
				for (String exp : whitelist)
						if (host.endsWith(exp)) {
							logger.info("Release: " + host + ":" + port);
							return;
						}
				logger.info("Redirect: " + host + " -> " + redirectAddress);
				Tool.coverString(host, redirectAddress);
			}
			
			@Override
			public void checkPermission(Permission perm) {
				String permName = perm.getName() != null ? perm.getName() : "missing";
				if (permName.startsWith("exitVM")) {
					Class<?>[] classContexts = getClassContext();
					String callingClass = classContexts.length > 4 ? classContexts[4].getName() : "none";
					String callingParent = classContexts.length > 5 ? classContexts[5].getName() : "none";
					// FML is allowed to call system exit and the Minecraft applet (from the quit button)
					boolean isFMLClass = false;
					for (String fmlPackageName : fmlPackageNames)
						if (callingClass.startsWith(fmlPackageName)) {
							isFMLClass = true;
							break;
						}
					if (!(isFMLClass
							|| "net.minecraft.server.dedicated.ServerHangWatchdog$1".equals(callingClass)
							|| "net.minecraft.server.dedicated.ServerHangWatchdog".equals(callingClass)
							|| "net.minecraft.client.Minecraft".equals(callingClass) &&
								"net.minecraft.client.Minecraft".equals(callingParent)
							|| "net.minecraft.server.dedicated.DedicatedServer".equals(callingClass) &&
								"net.minecraft.server.MinecraftServer".equals(callingParent))
							)
						throw new SecurityException("Can't exit by " + callingClass + " | " + callingParent);
				} else if ("setSecurityManager".equals(permName))
					throw new SecurityException("Cannot replace the FML security manager");
			}
			
			@Override
			public void checkPermission(Permission perm, Object context) { checkPermission(perm); }
			
		});
	}
	
	@Override
	public void injectData(Map<String, Object> data) { }

	@Override
	public String getModContainerClass() { return null; }

	@Override
	public String[] getASMTransformerClass() { return null; }
	
	@Override
	public String getSetupClass() { return null; }

	@Override
	public String getAccessTransformerClass() { return null; }
	
}

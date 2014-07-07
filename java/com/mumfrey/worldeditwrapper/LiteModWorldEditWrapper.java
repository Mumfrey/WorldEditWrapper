package com.mumfrey.worldeditwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.play.client.C01PacketChatMessage;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.ServerChatFilter;
import com.mumfrey.liteloader.ServerPluginChannelListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.interfaces.LoadableMod;
import com.mumfrey.liteloader.launch.ClassPathUtilities;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import com.mumfrey.liteloader.util.log.LiteLoaderLogger;
import com.mumfrey.worldeditwrapper.adapter.IWorldEditWrapper;
import com.mumfrey.worldeditwrapper.asm.EventProxy.Action;

/**
 * Litemod for the WorldEdit wrapper, the wrapper will only be loaded if the bundled WorldEdit artefact can be
 * successfully extracted and injected into the classpath, we do this by keeping the wrapper itself at "arms length"
 * via an interface and loading the wrapper itself using reflection.
 * 
 * @author Adam Mummery-Smith
 */
@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "worldeditwrapper.config.json")
public class LiteModWorldEditWrapper implements ServerChatFilter, ServerPluginChannelListener, Tickable
{
	/**
	 * Wrapper version
	 */
	public static final String VERSION = "1.2.0";
	
	/**
	 * Display version, we append the WorldEdit version if it is loaded successfully 
	 */
	private static String displayVersion = VERSION;
	
	/**
	 * Flag which keeps track of whether the server is running so that we can raise an event when a single
	 * player server is created
	 */
	private boolean serverRunning = false;
	
	@Expose()
	@SerializedName("worldEditJarName")
	private String bundledJarName = null;
	
	@Expose
	@SerializedName("rhinoJarName")
	private String bundledRhinoJarName = null;
	
	@Expose
	@SerializedName("useRhino")
	private boolean extractRhino = true;

	/**
	 * Static so that the injected event proxy can call static methods on this class and we can pass them
	 * through to the wrapper statically. 
	 */
	private static IWorldEditWrapper worldEditWrapper;
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.core.CommonPluginChannelListener#getChannels()
	 */
	@Override
	public List<String> getChannels()
	{
		return LiteModWorldEditWrapper.worldEditWrapper != null ? LiteModWorldEditWrapper.worldEditWrapper.getChannels() : new ArrayList<String>();
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.LiteMod#getName()
	 */
	@Override
	public String getName()
	{
		return "WorldEditWrapper";
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.LiteMod#getVersion()
	 */
	@Override
	public String getVersion()
	{
		return LiteModWorldEditWrapper.displayVersion;
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.LiteMod#init(java.io.File)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(File configPath)
	{
		if (this.bundledJarName == null)
		{
			this.bundledJarName = LiteLoader.getInstance().getModMetaData(this, "jarName", "WorldEdit.jar");
		}
		
		if (this.bundledRhinoJarName == null)
		{
			this.bundledRhinoJarName = LiteLoader.getInstance().getModMetaData(this, "rhinoJarName", "rhino-1_7r4.jar");
		}
		
		try
		{
			final LoadableMod<?> modContainer = LiteLoader.getInstance().getModContainer(this.getClass());
			File modLocation = new File(modContainer.getLocation());
			File modPath = modLocation.isFile() ? modLocation.getParentFile() : modLocation;
			if (!modPath.exists()) modPath = LiteLoader.getModsFolder();
			
			final File libPath = new File(modPath, "WorldEdit");
			libPath.mkdirs();
			if (libPath.exists() && libPath.isDirectory())
			{
				if (this.extractAndInjectLibrary("WorldEdit", this.bundledJarName, libPath))
				{
					final Class<? extends IWorldEditWrapper> clsWorldEditWrapper = (Class<? extends IWorldEditWrapper>)Class.forName("com.mumfrey.worldeditwrapper.impl.WorldEditWrapper", true, Launch.classLoader);
					LiteModWorldEditWrapper.worldEditWrapper = clsWorldEditWrapper.newInstance();
					LiteModWorldEditWrapper.worldEditWrapper.init(configPath);
					
					LiteModWorldEditWrapper.displayVersion += " (WorldEdit " + LiteModWorldEditWrapper.worldEditWrapper.getWorldEditVersion() + ")";
					
					if (this.extractRhino)
					{
						this.extractAndInjectLibrary("Rhino JavaScript Engine", this.bundledRhinoJarName, libPath);
					}
				}
			}
		}
		catch (final Throwable th)
		{
			th.printStackTrace();
		}
		
		LiteLoader.getInstance().writeConfig(this);
	}
	
	/**
	 * If you can't work out what this method does then you need help
	 * 
	 * @param jarPath
	 * @return
	 */
	private boolean extractAndInjectLibrary(String libraryName, String resourceName, File libPath)
	{
		final File jarFile = new File(libPath, resourceName);
		if (!jarFile.exists())
		{
			LiteLoaderLogger.info("%s jar does not exist, attempting to extract to %s", libraryName, libPath.getAbsolutePath());
			
			if (!LiteModWorldEditWrapper.extractFile("/" + resourceName, jarFile))
			{
				LiteLoaderLogger.warning("%s jar '%s' could not be extracted, WorldEdit may not function correctly (or at all)", libraryName, resourceName);
				return false;
			}
		}
		
		if (jarFile.exists())
		{
			LiteLoaderLogger.info("%s jar exists, attempting to inject into classpath", libraryName);
			
			try
			{
				ClassPathUtilities.injectIntoClassPath(Launch.classLoader, jarFile.toURI().toURL());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			
			LiteLoaderLogger.info("%s jar was successfully extracted", libraryName);
			return true;
		}

		LiteLoaderLogger.warning("%s jar was not detected, WorldEdit may not function correctly (or at all)", libraryName);
		
		return false;
	}
	
	/**
	 * Extract a file contained within the litemod to the specified path
	 * @param resourceName
	 * @param outputFile
	 * 
	 * @return
	 */
	private static boolean extractFile(String resourceName, File outputFile)
	{
		try
		{
			final InputStream inputStream = LiteModWorldEditWrapper.class.getResourceAsStream(resourceName);
			final OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(outputFile);
			ByteStreams.copy(inputStream, outputSupplier);
		}
		catch (NullPointerException ex)
		{
			return false;
		}
		catch (IOException ex)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param action
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 * @param side
	 * @return
	 */
	public static boolean onPlayerInteract(Action action, EntityPlayerMP player, int x, int y, int z, int side)
	{
		if (LiteModWorldEditWrapper.worldEditWrapper != null)
		{
			return LiteModWorldEditWrapper.worldEditWrapper.onPlayerInteract(action, player, x, y, z, side);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.ServerChatFilter#onChat(net.minecraft.entity.player.EntityPlayerMP, net.minecraft.network.play.client.C01PacketChatMessage, java.lang.String)
	 */
	@Override
	public boolean onChat(EntityPlayerMP player, C01PacketChatMessage chatPacket, String message)
	{
		if (LiteModWorldEditWrapper.worldEditWrapper != null)
		{
			return LiteModWorldEditWrapper.worldEditWrapper.onChat(player, message);
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.ServerPluginChannelListener#onCustomPayload(net.minecraft.entity.player.EntityPlayerMP, java.lang.String, int, byte[])
	 */
	@Override
	public void onCustomPayload(EntityPlayerMP sender, String channel, int length, byte[] data)
	{
		if (LiteModWorldEditWrapper.worldEditWrapper != null)
		{
			LiteModWorldEditWrapper.worldEditWrapper.onCustomPayload(sender, channel, data);
		}			
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.Tickable#onTick(net.minecraft.client.Minecraft, float, boolean, boolean)
	 */
	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		if (LiteModWorldEditWrapper.worldEditWrapper != null)
		{
			boolean serverRunning = minecraft.isIntegratedServerRunning();
			if (serverRunning != this.serverRunning && LiteModWorldEditWrapper.worldEditWrapper != null)
			{
				if (serverRunning)
					LiteModWorldEditWrapper.worldEditWrapper.onJoinWorld(minecraft.getIntegratedServer());
				
				this.serverRunning = serverRunning;
			}
			
			if (serverRunning && clock)
			{
				LiteModWorldEditWrapper.worldEditWrapper.onTick();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.LiteMod#upgradeSettings(java.lang.String, java.io.File, java.io.File)
	 */
	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath)
	{
	}
}

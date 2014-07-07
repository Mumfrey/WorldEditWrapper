package com.mumfrey.worldeditwrapper.impl;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.Sys;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.mumfrey.worldeditwrapper.LiteModWorldEditWrapper;
import com.mumfrey.worldeditwrapper.adapter.IWorldEditWrapper;
import com.mumfrey.worldeditwrapper.asm.EventProxy;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;

/**
 * WorldEdit wrapper impl
 * 
 * @author Adam Mummery-Smith
 */
public class WorldEditWrapper implements IWorldEditWrapper
{
	/**
	 * Plugin channel name for WorldEditCUI packets
	 */
	public static final String WECUI_CHANNEL = "WECUI";
	
	/**
	 * Charset for encoding WorldEditCUI messages
	 */
	public static final Charset UTF8 = Charset.forName("UTF-8");
	
	/**
	 * Config
	 */
	private VanillaWorldEditConfiguration config;

	/**
	 * List of plugin channels
	 */
	private final List<String> pluginChannels;
	
	/**
	 * 
	 */
	private VanillaServerInterface server;
	
	private File workingDir;
	private File craftScriptsPath;
	private File schematicsPath;
	
	private WorldEdit worldEdit;
	
	private String manifestVersion = "Unknown";

	private Set<EntityPlayerMP> propagateActiveItemPlayers = new HashSet<EntityPlayerMP>();

	public WorldEditWrapper()
	{
		this.pluginChannels = new ArrayList<String>();
		this.pluginChannels.add(WorldEditWrapper.WECUI_CHANNEL);
	}

	/* (non-Javadoc)
	 * @see com.mumfrey.worldeditwrapper.IWorldEditWrapper#getChannels()
	 */
	@Override
	public List<String> getChannels()
	{
		return this.pluginChannels;
	}

	@Override
	public void init(File configPath)
	{
		this.manifestVersion = WorldEdit.getVersion();
		if (!this.manifestVersion.equalsIgnoreCase(LiteModWorldEditWrapper.VERSION))
		{
			WorldEdit.setVersion(this.manifestVersion + " (wrapper version " + LiteModWorldEditWrapper.VERSION + ")");
		}
		
		this.workingDir = new File(configPath, "WorldEdit");
		this.workingDir.mkdir();
		
		this.craftScriptsPath = new File(this.workingDir, "craftscripts");
		this.craftScriptsPath.mkdirs();
		
		this.schematicsPath = new File(this.workingDir, "schematics");
		this.schematicsPath.mkdirs();
		
		this.config = new VanillaWorldEditConfiguration(this.workingDir);
		this.config.load();
	}
	
	@Override
	public String getWorldEditVersion()
	{
		return this.manifestVersion;
	}
	
	@Override
	public void onTick()
	{
		if (this.server != null)
		{
			this.server.onTick();
		}
		
		if (this.propagateActiveItemPlayers.size() > 0)
		{
			Iterator<EntityPlayerMP> iterator = this.propagateActiveItemPlayers.iterator();
			while (iterator.hasNext())
			{
				EntityPlayerMP player = iterator.next();
				iterator.remove();
				
	            try
				{
					Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
					player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(0, slot.slotNumber, player.inventory.getCurrentItem()));
				}
				catch (Exception ex) {}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.worldeditwrapper.IWorldEditWrapper#onChat(net.minecraft.entity.player.EntityPlayerMP, java.lang.String)
	 */
	@Override
	public boolean onChat(EntityPlayerMP player, String message)
	{
		if (message.startsWith("/") && this.server != null)
		{
			String[] args = message.split(" ");
			if (this.getWorldEdit().handleCommand(this.server.getLocalPlayer(player), args))
			{
				return false;
			}
			
			if (args.length > 0 && args[0].toLowerCase().equals("/dir"))
			{
				return this.handleOpenDirCommand(player, args);
			}
		}
		
		return true;
	}
	
    private boolean handleOpenDirCommand(EntityPlayerMP player, String[] args)
	{
    	if (args.length < 2)
    	{
    		player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "//dir <cscript|schematic>"));
    		return false;
    	}
    	
    	String dirType = args[1].toLowerCase();
    	if (this.checkOpenDir(dirType, "cscript", this.craftScriptsPath)) return false;
    	if (this.checkOpenDir(dirType, "schematic", this.schematicsPath)) return false;

		player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Expected 'cscript' or 'schematic' for //dir command"));

		return true;
	}

	private boolean checkOpenDir(String dirType, String type, File dir)
	{
		if (type.substring(0, Math.min(dirType.length(), type.length())).equals(dirType))
    	{
    		this.openDir(dir);
    		return true;
    	}
    	
    	return false;
	}

	private void openDir(File dir)
    {
        boolean awtFailed = false;

        try
        {
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            Object objDesktop = desktopClass.getMethod("getDesktop", new Class[0]).invoke(null);
            desktopClass.getMethod("browse", URI.class).invoke(objDesktop, dir.toURI());
        }
        catch (Throwable th)
        {
            awtFailed = true;
        }

        if (awtFailed)
        {
            Sys.openURL("file://" + dir.getAbsolutePath());
        }
    }
	
	/* (non-Javadoc)
	 * @see com.mumfrey.worldeditwrapper.IWorldEditWrapper#onCustomPayload(net.minecraft.entity.player.EntityPlayerMP, java.lang.String, byte[])
	 */
	@Override
	public void onCustomPayload(EntityPlayerMP sender, String channel, byte[] data)
	{
		if (channel.equals(WorldEditWrapper.WECUI_CHANNEL))
		{
			LocalSession session = this.getSession(sender);
			
			if (session != null && !session.hasCUISupport())
			{
				final String text = new String(data, WorldEditWrapper.UTF8);
				session.handleCUIInitializationMessage(text);
			}
		}
	}
	
	/**
	 * @return
	 */
	public WorldEdit getWorldEdit()
	{
		return this.worldEdit;
	}
	
	/**
	 * @return
	 */
	public VanillaWorldEditConfiguration getConfiguration()
	{
		return this.config;
	}
	
	/**
	 * @return
	 */
	public ServerInterface getServerInterface()
	{
		return this.server;
	}
	
	/**
	 * @param player
	 * @return
	 */
	public LocalSession getSession(EntityPlayerMP player)
	{
		if (this.server != null)
		{
			return this.worldEdit.getSession(this.server.getLocalPlayer(player));
		}
		
		return null;
	}
	
	/**
	 * @return
	 */
	public File getWorkingDirectory()
	{
		return this.workingDir;
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.worldeditwrapper.IWorldEditWrapper#onJoinWorld()
	 */
	@Override
	public void onJoinWorld(MinecraftServer integratedServer)
	{
		this.server = new VanillaServerInterface(integratedServer);
		this.worldEdit = new WorldEdit(this.server, this.config);
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.worldeditwrapper.IWorldEditWrapper#onPlayerInteract(com.mumfrey.worldeditwrapper.asm.Events.Action, net.minecraft.entity.player.EntityPlayerMP, int, int, int, int)
	 */
	@Override
	public boolean onPlayerInteract(EventProxy.Action action, EntityPlayerMP player, int x, int y, int z, int side) 
	{
		if (this.worldEdit == null || this.server == null || player.worldObj.isRemote) return false;
		
		boolean cancelled = false;
		
		VanillaPlayer localPlayer = this.server.getLocalPlayer(player);
		VanillaWorld world = VanillaWorld.getLocalWorld(player.worldObj);

		WorldVector vec = new WorldVector(world, x, y, z);
		
		if (action == EventProxy.Action.LEFT_CLICK_BLOCK)
		{
			if (this.worldEdit.handleBlockLeftClick(localPlayer, vec))
				cancelled = true;
			
			if (this.worldEdit.handleArmSwing(localPlayer))
				cancelled = true;
		}
		else if (action == EventProxy.Action.RIGHT_CLICK_BLOCK)
		{
			if (this.worldEdit.handleBlockRightClick(localPlayer, vec))
				cancelled = true;
			
			if (this.worldEdit.handleRightClick(localPlayer))
				cancelled = true;
			
			if (cancelled)
			{
				this.propagateActiveItemPlayers.add(player);
			}
		}
		else if ((action == EventProxy.Action.RIGHT_CLICK_AIR) && (this.worldEdit.handleRightClick(localPlayer)))
		{
			cancelled = true;
		}
		else if ((action == EventProxy.Action.LEFT_CLICK_AIR) && (this.worldEdit.handleArmSwing(localPlayer)))
		{
			cancelled = true;
		}
		
		return cancelled;
	}
}

package com.mumfrey.worldeditwrapper.asm;

import com.mumfrey.worldeditwrapper.LiteModWorldEditWrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

/**
 * Callbacks injected by the interaction transformer, validates state and raises events against the wrapper
 *
 * @author Adam Mummery-Smith
 */
public abstract class EventProxy
{
	/**
	 * Type of click event 
	 */
	public static enum Action
	{
		RIGHT_CLICK_AIR,
		LEFT_CLICK_AIR,
		RIGHT_CLICK_BLOCK,
		LEFT_CLICK_BLOCK
	}
	
	/**
	 * @param manager
	 * @param x
	 * @param y
	 * @param z
	 * @param side
	 * @return
	 */
	public static boolean onBlockClicked(ItemInWorldManager manager, int x, int y, int z, int side)
	{
		if (!manager.getGameType().isAdventure() || manager.thisPlayerMP.isCurrentToolAdventureModeExempt(x, y, z))
		{
			boolean cancelled = LiteModWorldEditWrapper.onPlayerInteract(Action.LEFT_CLICK_BLOCK, manager.thisPlayerMP, x, y, z, side);
			
			if (cancelled)
			{
				manager.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, manager.theWorld));
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param player
	 * @param world
	 * @param itemStack
	 * @param x
	 * @param y
	 * @param z
	 * @param side
	 * @param par8
	 * @param par9
	 * @param par10
	 * @return
	 */
	public static boolean activateBlockOrUseItem(EntityPlayer player, World world, ItemStack itemStack, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		if (player instanceof EntityPlayerMP)
		{
			EntityPlayerMP playerMP = (EntityPlayerMP)player;
			
			boolean cancelled = LiteModWorldEditWrapper.onPlayerInteract(Action.RIGHT_CLICK_BLOCK, playerMP, x, y, z, side);
			
			if (cancelled)
			{
				playerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, playerMP.worldObj));
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param netHandler
	 * @return
	 */
	public static boolean onClickedAir(NetHandlerPlayServer netHandler)
	{
		return LiteModWorldEditWrapper.onPlayerInteract(Action.RIGHT_CLICK_AIR, netHandler.playerEntity, 0, 0, 0, -1);
	}
	
	/**
	 * @param netHandler
	 * @param packet
	 * @return
	 */
	public static boolean onClickedAir(NetHandlerPlayServer netHandler, C0APacketAnimation packet)
	{
		return LiteModWorldEditWrapper.onPlayerInteract(Action.LEFT_CLICK_AIR, netHandler.playerEntity, 0, 0, 0, -1);
	}
}

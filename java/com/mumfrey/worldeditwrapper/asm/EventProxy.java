package com.mumfrey.worldeditwrapper.asm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

import com.mumfrey.liteloader.transformers.event.EventInfo;
import com.mumfrey.liteloader.transformers.event.ReturnEventInfo;
import com.mumfrey.worldeditwrapper.LiteModWorldEditWrapper;

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
	
	public static void onBlockClicked(EventInfo<ItemInWorldManager> e, int x, int y, int z, int side)
	{
		ItemInWorldManager manager = e.getSource();
		
		if (!manager.getGameType().isAdventure() || manager.thisPlayerMP.isCurrentToolAdventureModeExempt(x, y, z))
		{
			boolean cancelled = LiteModWorldEditWrapper.onPlayerInteract(Action.LEFT_CLICK_BLOCK, manager.thisPlayerMP, x, y, z, side);
			
			if (cancelled)
			{
				manager.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, manager.theWorld));
				e.cancel();
			}
		}
	}
	
//	public static void onBlockClicked(EventInfo<NetHandlerPlayServer> e, C08PacketPlayerBlockPlacement packet)
//	{
//		NetHandlerPlayServer netHandler = e.getSource();
//		ItemInWorldManager manager = netHandler.playerEntity.theItemInWorldManager;
//		
//        int x = packet.func_149576_c();
//        int y = packet.func_149571_d();
//        int z = packet.func_149570_e();
//        int side = packet.func_149568_f();
//		
//		boolean cancelled = LiteModWorldEditWrapper.onPlayerInteract(Action.RIGHT_CLICK_BLOCK, netHandler.playerEntity, x, y, z, side);
//		
//		if (cancelled)
//		{
//			manager.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, manager.theWorld));
//			e.cancel();
//		}
//	}
	
	public static void activateBlockOrUseItem(ReturnEventInfo<ItemInWorldManager, Boolean> e, EntityPlayer player, World world, ItemStack itemStack, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		if (player instanceof EntityPlayerMP)
		{
			EntityPlayerMP playerMP = (EntityPlayerMP)player;
			
			boolean cancelled = LiteModWorldEditWrapper.onPlayerInteract(Action.RIGHT_CLICK_BLOCK, playerMP, x, y, z, side);
			
			if (cancelled)
			{
				playerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, playerMP.worldObj));
				e.setReturnValue(false);
			}
		}
	}
	
	public static void onClickedAir(EventInfo<NetHandlerPlayServer> e, C08PacketPlayerBlockPlacement packet)
	{
		NetHandlerPlayServer netHandler = e.getSource();
		if (LiteModWorldEditWrapper.onPlayerInteract(Action.RIGHT_CLICK_AIR, netHandler.playerEntity, 0, 0, 0, -1))
		{
			e.cancel();
		}
	}
	
	public static void onClickedAir(EventInfo<NetHandlerPlayServer> e, C0APacketAnimation packet)
	{
		NetHandlerPlayServer netHandler = e.getSource();
		if (LiteModWorldEditWrapper.onPlayerInteract(Action.LEFT_CLICK_AIR, netHandler.playerEntity, 0, 0, 0, -1))
		{
			e.cancel();
		}
	}
}

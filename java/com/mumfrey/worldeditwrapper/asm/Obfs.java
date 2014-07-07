package com.mumfrey.worldeditwrapper.asm;

import com.mumfrey.liteloader.core.runtime.Obf;

public class Obfs extends Obf
{
	public static final String ON_CLICKED_AIR             = "onClickedAir";
	public static final String ON_BLOCK_CLICKED           = "onBlockClicked";
	public static final String ON_USE_ITEM                = "activateBlockOrUseItem";
	
	// TODO Obfuscation 1.7.10
	public static final Obf WrapperEventProxy             = new Obfs("com.mumfrey.worldeditwrapper.asm.EventProxy");
	public static final Obf ItemInWorldManager            = new Obfs("net.minecraft.server.management.ItemInWorldManager",              "mx");
	public static final Obf NetHandlerPlayServer          = new Obfs("net.minecraft.network.NetHandlerPlayServer",                      "nh");
	public static final Obf C08PacketPlayerBlockPlacement = new Obfs("net.minecraft.network.play.client.C08PacketPlayerBlockPlacement", "jo");
	public static final Obf C0APacketAnimation            = new Obfs("net.minecraft.network.play.client.C0APacketAnimation",            "ip");
	public static final Obf EntityPlayer                  = new Obfs("net.minecraft.entity.player.EntityPlayer",                        "yz");
	public static final Obf World                         = new Obfs("net.minecraft.world.World",                                       "ahb");
	public static final Obf ItemStack                     = new Obfs("net.minecraft.item.ItemStack",                                    "add");

	// Methods
	public static final Obf onBlockClicked                = new Obfs("func_73074_a",  "a", "onBlockClicked");
	public static final Obf activateBlockOrUseItem        = new Obfs("func_73078_a",  "a", "activateBlockOrUseItem");
	public static final Obf processPlayerBlockPlacement   = new Obfs("func_147346_a", "a", "processPlayerBlockPlacement");
	public static final Obf processAnimation              = new Obfs("func_147350_a", "a", "processAnimation");

	public Obfs(String mcpName)
	{
		super(mcpName);
	}
	
	public Obfs(String seargeName, String obfName)
	{
		super(seargeName, obfName, seargeName);
	}
	
	public Obfs(String seargeName, String obfName, String mcpName)
	{
		super(seargeName, obfName, mcpName);
	}
}

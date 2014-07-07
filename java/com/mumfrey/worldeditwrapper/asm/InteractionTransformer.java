package com.mumfrey.worldeditwrapper.asm;

import java.util.Collection;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import com.mumfrey.liteloader.transformers.event.Event;
import com.mumfrey.liteloader.transformers.event.EventInjectionTransformer;
import com.mumfrey.liteloader.transformers.event.InjectionPoint;
import com.mumfrey.liteloader.transformers.event.MethodInfo;
import com.mumfrey.liteloader.transformers.event.inject.MethodHead;

/**
 * Transformer which injects the callbacks we need using ASM
 * 
 * @author Adam Mummery-Smith
 */
public class InteractionTransformer extends EventInjectionTransformer
{
	
	@Override
	protected void addEvents()
	{
		InjectionPoint head = new MethodHead();
		InjectionPoint playerBlockPlacementPoint = new PlayerBlockPlacementPoint();
		
		MethodInfo mdOnBlockClicked = new MethodInfo(Obfs.ItemInWorldManager, Obfs.onBlockClicked, Void.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
		MethodInfo mdActivateBlockOrUseItem = new MethodInfo(Obfs.ItemInWorldManager, Obfs.activateBlockOrUseItem, Boolean.TYPE, Obfs.EntityPlayer, Obfs.World, Obfs.ItemStack, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Float.TYPE, Float.TYPE, Float.TYPE);
		MethodInfo mdProcessPlayerBlockPlacement = new MethodInfo(Obfs.NetHandlerPlayServer, Obfs.processPlayerBlockPlacement, Void.TYPE, Obfs.C08PacketPlayerBlockPlacement);
		MethodInfo mdProcessAnimation = new MethodInfo(Obfs.NetHandlerPlayServer, Obfs.processAnimation, Void.TYPE, Obfs.C0APacketAnimation);
		
		Event onBlockClicked = Event.getOrCreate("onBlockClicked", true);
		Event activateBlockOrUseItem = Event.getOrCreate("activateBlockOrUseItem", true);
		Event onClickedAir = Event.getOrCreate("onClickedAir", true);
		
		this.addEvent(onBlockClicked, mdOnBlockClicked, head).addListener(new MethodInfo(Obfs.WrapperEventProxy, Obfs.ON_BLOCK_CLICKED));
		this.addEvent(activateBlockOrUseItem, mdActivateBlockOrUseItem, head).addListener(new MethodInfo(Obfs.WrapperEventProxy, Obfs.ON_USE_ITEM));
		this.addEvent(onClickedAir, mdProcessPlayerBlockPlacement, playerBlockPlacementPoint).addListener(new MethodInfo(Obfs.WrapperEventProxy, Obfs.ON_CLICKED_AIR));
		this.addEvent(onClickedAir, mdProcessAnimation, head).addListener(new MethodInfo(Obfs.WrapperEventProxy, Obfs.ON_CLICKED_AIR));
		
//		this.addEvent(activateBlockOrUseItem, mdProcessPlayerBlockPlacement, new BeforeInvoke(mdActivateBlockOrUseItem)).addListener(new MethodInfo(Obfs.WrapperEventProxy, Obfs.ON_BLOCK_CLICKED));
	}
}

class PlayerBlockPlacementPoint extends InjectionPoint
{
	@Override
	public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes, Event event)
	{
		boolean foundNotNull = false;
		boolean foundReturn = false;

		ListIterator<AbstractInsnNode> iter = insns.iterator();
		while (iter.hasNext())
		{
			AbstractInsnNode node = iter.next();
			if (node instanceof JumpInsnNode && node.getOpcode() == Opcodes.IFNONNULL && !foundNotNull)
			{
				foundNotNull = true;
			}
			else if (node instanceof InsnNode && node.getOpcode() == Opcodes.RETURN && foundNotNull && !foundReturn)
			{
				foundReturn = true;
			}
			else if (node instanceof LabelNode && foundNotNull && foundReturn)
			{
				nodes.add(insns.get(insns.indexOf(node) + 1));
				return true;
			}
		}
		
		return false;
	}
}

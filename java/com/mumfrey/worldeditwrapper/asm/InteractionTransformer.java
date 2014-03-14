package com.mumfrey.worldeditwrapper.asm;

import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.mumfrey.liteloader.util.log.LiteLoaderLogger;

/**
 * Transformer which injects the callbacks we need using ASM
 * 
 * @author Adam Mummery-Smith
 */
public class InteractionTransformer implements IClassTransformer
{
	private static final String EVENTPROXY_CLASS                    = "com/mumfrey/worldeditwrapper/asm/EventProxy";
	private static final String ON_CLICKED_AIR                      = "onClickedAir";
	private static final String ON_BLOCK_CLICKED                    = "onBlockClicked";
	
	// TODO Obfuscation 1.7.2
	private static final String clsItemInWorldManager               = "net.minecraft.server.management.ItemInWorldManager";
	private static final String clsNetHandlerPlayServer             = "net.minecraft.network.NetHandlerPlayServer";
	
	private static final String clsItemInWorldManagerObf            = "mn";
	private static final String clsNetHandlerPlayServerObf          = "mx";

	private static final String clsC08PacketPlayerBlockPlacementObf = "jc";
	private static final String clsC0APacketAnimationObf            = "ic";
	
	private static final String processPlayerBlockPlacementSig      = "(Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;)V";
	private static final String processPlayerBlockPlacementSigObf   = "(L" + clsC08PacketPlayerBlockPlacementObf + ";)V";
	
	private static final String processAnimationSig                 = "(Lnet/minecraft/network/play/client/C0APacketAnimation;)V";
	private static final String processAnimationSigObf              = "(L" + clsC0APacketAnimationObf + ";)V";
	
	private static final String onBlockClicked                      = "onBlockClicked";
	private static final String activateBlockOrUseItem              = "activateBlockOrUseItem";
	private static final String processPlayerBlockPlacement         = "processPlayerBlockPlacement";
	private static final String processAnimation                    = "processAnimation";

	private static final String onBlockClickedSrg                   = "func_73074_a";
	private static final String activateBlockOrUseItemSrg           = "func_73078_a";
	private static final String processPlayerBlockPlacementSrg      = "func_147346_a";
	private static final String processAnimationSrg                 = "func_147350_a";
	
	private static final String onBlockClickedObf                   = "a";
	private static final String activateBlockOrUseItemObf           = "a";
	private static final String processPlayerBlockPlacementObf      = "a";
	private static final String processAnimationObf                 = "a";
	
	/* (non-Javadoc)
	 * @see net.minecraft.launchwrapper.IClassTransformer#transform(java.lang.String, java.lang.String, byte[])
	 */
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (clsItemInWorldManagerObf.equals(transformedName) || clsItemInWorldManager.equals(transformedName))
		{
			return this.transformItemInWorldManager(basicClass);
		}
		else if (clsNetHandlerPlayServerObf.equals(transformedName) || clsNetHandlerPlayServer.equals(transformedName))
		{
			return this.transformNetHandlerPlayServer(basicClass);
		}
		
		return basicClass;
	}
	
	private byte[] transformItemInWorldManager(byte[] basicClass)
	{
		ClassNode classNode = this.readClass(basicClass);
		
		for (MethodNode method : classNode.methods)
		{
			if ((onBlockClickedObf.equals(method.name) || onBlockClickedSrg.equals(method.name) || onBlockClicked.equals(method.name)) && "(IIII)V".equals(method.desc))
			{
				LiteLoaderLogger.info("Injecting callback for onBlockClicked");
				this.transformOnBlockClicked(classNode, method);
			}
			else if ((activateBlockOrUseItemObf.equals(method.name) || activateBlockOrUseItemSrg.equals(method.name) || activateBlockOrUseItem.equals(method.name)) && method.desc.endsWith("IIIIFFF)Z"))
			{
				LiteLoaderLogger.info("Injecting callback for activateBlockOrUseItem");
				this.transformActivateBlockOrUseItem(classNode, method);
			}
		}
		
		return this.writeClass(classNode);
	}
	
	private void transformOnBlockClicked(ClassNode classNode, MethodNode method)
	{
		Label l1 = new Label();
		LabelNode labelNode = new LabelNode(l1);
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, EVENTPROXY_CLASS, ON_BLOCK_CLICKED, "(L" + classNode.name + ";IIII)Z"));
		insns.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
		insns.add(new InsnNode(Opcodes.RETURN));
		insns.add(labelNode);
		
		method.instructions.insert(insns);
	}
	
	private void transformActivateBlockOrUseItem(ClassNode classNode, MethodNode method)
	{
		Label l1 = new Label();
		LabelNode labelNode = new LabelNode(l1);
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 6));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 7));
		insns.add(new VarInsnNode(Opcodes.FLOAD, 8));
		insns.add(new VarInsnNode(Opcodes.FLOAD, 9));
		insns.add(new VarInsnNode(Opcodes.FLOAD, 10));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, EVENTPROXY_CLASS, "activateBlockOrUseItem", method.desc));
		insns.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
		insns.add(new InsnNode(Opcodes.ICONST_0));
		insns.add(new InsnNode(Opcodes.IRETURN));
		insns.add(labelNode);
		
		method.instructions.insert(insns);
	}
	
	private byte[] transformNetHandlerPlayServer(byte[] basicClass)
	{
		ClassNode classNode = this.readClass(basicClass);
		
		for (MethodNode method : classNode.methods)
		{
			if ((processPlayerBlockPlacementObf.equals(method.name) || processPlayerBlockPlacementSrg.equals(method.name) || processPlayerBlockPlacement.equals(method.name)) && (processPlayerBlockPlacementSigObf.equals(method.desc) || processPlayerBlockPlacementSig.equals(method.desc)))
			{
				LiteLoaderLogger.info("Injecting callback for processPlayerBlockPlacement");
				this.transformProcessPlayerBlockPlacement(classNode, method);
			}
			if ((processAnimationObf.equals(method.name) || processAnimationSrg.equals(method.name) || processAnimation.equals(method.name)) && (processAnimationSigObf.equals(method.desc) || processAnimationSig.equals(method.desc)))
			{
				LiteLoaderLogger.info("Injecting callback for processAnimation");
				this.transformProcessAnimation(classNode, method);
			}
		}
		
		return this.writeClass(classNode);
	}

	private void transformProcessPlayerBlockPlacement(ClassNode classNode, MethodNode method)
	{
		Label l13 = new Label();
		LabelNode labelNode = new LabelNode(l13);
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, EVENTPROXY_CLASS, ON_CLICKED_AIR, "(L" + classNode.name + ";)Z"));
		insns.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
		insns.add(new InsnNode(Opcodes.RETURN));
		insns.add(labelNode);
		
		boolean foundNotNull = false;
		boolean foundReturn = false;

		ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
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
				method.instructions.insert(node, insns);
				return;
			}
		}
	}

	private void transformProcessAnimation(ClassNode classNode, MethodNode method)
	{
		Label l1 = new Label();
		LabelNode labelNode = new LabelNode(l1);
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, EVENTPROXY_CLASS, ON_CLICKED_AIR, "(L" + classNode.name + ";" + Type.getArgumentTypes(method.desc)[0].toString() + ")Z"));
		insns.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
		insns.add(new InsnNode(Opcodes.RETURN));
		insns.add(labelNode);
		
		method.instructions.insert(insns);
	}
	
	/**
	 * @param basicClass
	 * @return
	 */
	private ClassNode readClass(byte[] basicClass)
	{
		ClassReader classReader = new ClassReader(basicClass);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		return classNode;
	}
	
	/**
	 * @param classNode
	 * @return
	 */
	private byte[] writeClass(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}

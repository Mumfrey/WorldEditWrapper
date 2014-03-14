package com.mumfrey.worldeditwrapper.impl;

import java.util.Arrays;
import java.util.List;

import com.sk89q.minecraft.util.commands.Command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/**
 * Command to register with the vanilla server
 * 
 * @author Adam Mummery-Smith
 */
public class WorldEditCommand extends CommandBase
{
	/**
	 * Command being wrapped
	 */
	private final Command command;
	
	/**
	 * Name of this command
	 */
	private final String commandName;
	
	/**
	 * Usage string for the command
	 */
	private final String usageString;
	
	/**
	 * @param command
	 */
	public WorldEditCommand(Command command)
	{
		this.command = command;
		this.commandName = command.aliases()[0];
		this.usageString = String.format("/%s %s", this.commandName, command.usage());
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.command.CommandBase#getCommandAliases()
	 */
	@Override
	public List<String> getCommandAliases()
	{
		return Arrays.asList(this.command.aliases());
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.command.ICommand#getCommandName()
	 */
	@Override
	public String getCommandName()
	{
		return this.commandName;
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.command.ICommand#getCommandUsage(net.minecraft.command.ICommandSender)
	 */
	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return this.usageString;
	}
	
	/* (non-Javadoc)
	 * @see net.minecraft.command.ICommand#processCommand(net.minecraft.command.ICommandSender, java.lang.String[])
	 */
	@Override
	public void processCommand(ICommandSender var1, String[] var2)
	{
		// Stub
	}
}

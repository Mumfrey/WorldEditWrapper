package com.mumfrey.worldeditwrapper.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import com.sk89q.worldedit.util.PropertiesConfiguration;

/**
 * Config
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaWorldEditConfiguration extends PropertiesConfiguration
{
	/**
	 * Name of the properties file
	 */
	private static final String propertiesFilename = "worldedit.properties";
	
	/**
	 * Working dir
	 */
	private final File workingDirectory;
	
	/**
	 * @param workingDirectory
	 */
	public VanillaWorldEditConfiguration(File workingDirectory)
	{
		super(VanillaWorldEditConfiguration.getConfigFile(workingDirectory));
		this.workingDirectory = workingDirectory;
	}
	
	/**
	 * @param workingDir
	 * @return
	 */
	private static File getConfigFile(File workingDir)
	{
		File configFile = new File(workingDir, VanillaWorldEditConfiguration.propertiesFilename);

		try
		{
			if (!configFile.exists())
			{
				final InputStream inputStream = VanillaWorldEditConfiguration.class.getResourceAsStream("/defaults/" + VanillaWorldEditConfiguration.propertiesFilename);
				if (inputStream != null)
				{
					final OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(configFile);
					ByteStreams.copy(inputStream, outputSupplier);
				}
			}
		}
		catch (final IOException ex) {}
		
		return configFile;
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalConfiguration#getWorkingDirectory()
	 */
	@Override
	public File getWorkingDirectory()
	{
		return this.workingDirectory;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.util.PropertiesConfiguration#load()
	 */
	@Override
	public void load()
	{
		super.load();
		
		this.showFirstUseVersion = false;
	}
}
package com.mumfrey.worldeditwrapper.impl;

/**
 *
 * @author Adam Mummery-Smith
 */
public class WorldEditScheduledTask
{
	/**
	 * Task to run
	 */
	private final Runnable task;
	
	/**
	 * Repeat interval in ticks 
	 */
	private final long interval;
	
	/**
	 * Initially set to delay, then re-set to interval every time the task runs
	 */
	private long ticks;

	/**
	 * @param task
	 * @param interval
	 * @param delay
	 */
	public WorldEditScheduledTask(Runnable task, long interval, long delay)
	{
		this.interval = Math.max(1, interval);
		this.ticks = Math.max(1, delay);
		this.task = task;
	}

	/**
	 * Called every tick
	 */
	public void onTick()
	{
		this.ticks--;
		
		if (this.ticks == 0)
		{
			this.task.run();
			this.ticks = this.interval;
		}
	}
}

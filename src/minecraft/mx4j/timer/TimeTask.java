/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.timer;

/**
 * A task that is executed at a specified time. <p>
 * Subclasses implement the periodicity, if needed.
 * Two TimeTasks are compared with their neext execution time.
 *
 * @version $Revision: 1.3 $
 */
public abstract class TimeTask implements Comparable, Runnable
{
   private long executionTime;
   private boolean finished;

   /**
    * Constructor for subclasses
    */
   protected TimeTask()
   {
   }

   /**
    * The method to implement to have this TimeTask to do something.
    */
   public abstract void run();

   /**
    * Returns whether this task is periodic. By default return false.
    *
    * @see #getPeriod
    */
   protected boolean isPeriodic()
   {
      return false;
   }

   /**
    * Returns the period of this task. By default returns 0.
    *
    * @see #isPeriodic
    */
   protected long getPeriod()
   {
      return 0;
   }

   /**
    * Returns whether this task is a fixed rate or fixed delay task.  By default
    * return false
    */
   public boolean getFixedRate()
   {
      return false;
   }

   /**
    * Returns the next time at which the task will be executed, ie the {@link #run} method is called.
    *
    * @see #setNextExecutionTime
    */
   protected long getNextExecutionTime()
   {
      return executionTime;
   }

   /**
    * Sets the next execution time.
    *
    * @see #getNextExecutionTime
    */
   protected void setNextExecutionTime(long time)
   {
      executionTime = time;
   }

   /**
    * Marks this task as finished or not. When a task is finished, its
    * {@link #run} method will not be called anymore.
    *
    * @see #isFinished
    */
   protected void setFinished(boolean value)
   {
      finished = value;
   }

   /**
    * Returns whethere this task is finished.
    *
    * @see #setFinished
    */
   protected boolean isFinished()
   {
      return finished;
   }

   /**
    * Compares 2 TimeTasks by comparing their next execution times
    *
    * @see #getNextExecutionTime
    */
   public int compareTo(Object obj)
   {
      if (obj == null) return 1;
      if (obj == this) return 0;

      TimeTask other = (TimeTask)obj;
      long et = getNextExecutionTime();
      long oet = other.getNextExecutionTime();
      if (et > oet)
         return 1;
      else if (et < oet) return -1;
      return 0;
   }
}

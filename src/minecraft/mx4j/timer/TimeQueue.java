/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.timer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A queue that executes {@link mx4j.timer.TimeTask}s when it is started. <br>
 * Every scheduled task is executed at the specified time; if this time is in the past,
 * the task is executed immediately.
 * Just before the task is executed, the task itself is updated so that its new
 * execution time is set or it is marked as finished (if it is not forever periodic).
 *
 * @version $Revision: 1.5 $
 * @see mx4j.timer.TimeTask
 */
public class TimeQueue
{
   private Thread thread;
   private volatile boolean running;
   private final ArrayList tasks;
   private final boolean daemon;

   /**
    * Creates a new TimeQueue
    */
   public TimeQueue()
   {
      this(false);
   }

   /**
    * Creates a new TimeQueue that will set the thread daemon or not depending on the given argument
    */
   public TimeQueue(boolean daemon)
   {
      tasks = new ArrayList();
      this.daemon = daemon;
   }

   /**
    * Starts this TimeQueue. <br>
    * Tasks are executed only after the queue has been started.
    *
    * @see #stop
    */
   public void start()
   {
      synchronized (this)
      {
         if (!running)
         {
            running = true;
            thread = new Thread(new Loop(), "MBean Timer Notification Thread");
            thread.setDaemon(daemon);
            thread.start();
         }
      }
   }

   /**
    * Stops this TimeQueue. <br>
    * No task is executed when the queue is stopped; however, already scheduled tasks
    * are not removed; restarting the queue has the effect of executing the tasks remained
    * if their time has come.
    *
    * @see #start
    */
   public void stop()
   {
      synchronized (this)
      {
         if (running)
         {
            running = false;
            thread.interrupt();
         }
      }
   }

   /**
    * Returns the number of tasks present in this TimeQueue
    */
   public int size()
   {
      synchronized (this)
      {
         return tasks.size();
      }
   }

   /**
    * Schedules the given task for execution.
    *
    * @see #unschedule
    */
   public void schedule(TimeTask task)
   {
      synchronized (this)
      {
         tasks.add(task);
         // Using tree sets or maps does not work, since they don't rely on equals for add, but on compareTo
         Collections.sort(tasks);
         notifyAll();
      }
   }

   /**
    * Removes the given task from this TimeQueue
    *
    * @see #schedule
    */
   public void unschedule(TimeTask task)
   {
      synchronized (this)
      {
         tasks.remove(task);
      }
   }

   /**
    * Removes all the tasks from this TimeQueue.
    */
   public void clear()
   {
      synchronized (this)
      {
         tasks.clear();
      }
   }

   private TimeTask getTask() throws InterruptedException
   {
      synchronized (this)
      {
         while (tasks.isEmpty())
         {
            wait();
         }

         // Do not remove the task from the queue
         TimeTask task = (TimeTask)tasks.get(0);
         return task;
      }
   }

   private class Loop implements Runnable
   {
      public void run()
      {
         while (running && !thread.isInterrupted())
         {
            try
            {
               TimeTask task = getTask();
               long now = System.currentTimeMillis();
               long executionTime = task.getNextExecutionTime();
               if (executionTime == 0L) executionTime = now;
               long timeToWait = executionTime - now;
               boolean runTask = timeToWait <= 0;
               if (!runTask)
               {
                  // When a new job is scheduled, I wake up, but this job may not be the one to run
                  Object lock = TimeQueue.this;
                  synchronized (lock)
                  {
                     // timeToWait is always strictly > 0, so I don't wait forever
                     lock.wait(timeToWait);
                  }
               }
               else
               {
                  // The task must be run, remove it from the list
                  unschedule(task);

                  if (task.isPeriodic())
                  {
                     // Compute the new execution time.  This is different for
                     // fixed rate and fixed delay tasks
                     if (task.getFixedRate())
                     {
                        task.setNextExecutionTime(executionTime + task.getPeriod());
                     }
                     else
                     {
                        task.setNextExecutionTime(now + task.getPeriod());
                     }

                     // Reschedule on the new time
                     schedule(task);
                  }
                  else
                  {
                     // Mark the task as finished, it will be removed also by the Timer class
                     task.setFinished(true);
                  }

                  try
                  {
                     // Run it !
                     task.run();
                  }
                  catch (Throwable x)
                  {
                     // Problems, for now just print it
                     x.printStackTrace();
                  }
               }
            }
            catch (InterruptedException x)
            {
               Thread.currentThread().interrupt();
               break;
            }
         }
      }
   }
}

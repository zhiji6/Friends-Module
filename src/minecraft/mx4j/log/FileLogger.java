/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.log;

/**
 * This logger logs to a file. <p>
 * It's used by the ModelMBean implementation. <br>
 * Since the constructor takes a parameter, cannot be used as prototype for logging redirection.
 *
 * @version $Revision: 1.5 $
 */
public class FileLogger extends Logger
{
   public FileLogger(String location)
   {
      // TODO
   }

   protected void log(int priority, Object message, Throwable t)
   {
      // TODO
   }
}

/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.log;

/**
 * Log that redirects log calls to Jakarta Commons Logging. <p>
 *
 * @version $Revision: 1.3 $
 */
public class CommonsLogger extends Logger
{
   private org.apache.commons.logging.Log log = null;

   public CommonsLogger()
   {
   }

   protected void setCategory(String category)
   {
      super.setCategory(category);
      log = org.apache.commons.logging.LogFactory.getLog(getCategory());
   }

   protected void log(int priority, Object message, Throwable t)
   {
      switch (priority)
      {
         case Logger.FATAL:
            if (t == null)
               log.fatal(message);
            else
               log.fatal(message, t);
            break;
         case Logger.ERROR:
            if (t == null)
               log.error(message);
            else
               log.error(message, t);
            break;
         case Logger.WARN:
            if (t == null)
               log.warn(message);
            else
               log.warn(message, t);
            break;
         case Logger.INFO:
            if (t == null)
               log.info(message);
            else
               log.info(message, t);
            break;
         case Logger.DEBUG:
            if (t == null)
               log.debug(message);
            else
               log.debug(message, t);
            break;
         case Logger.TRACE:
            if (t == null)
               log.trace(message);
            else
               log.trace(message, t);
            break;
      }
   }
}

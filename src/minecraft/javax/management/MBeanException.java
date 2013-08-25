/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @version $Revision: 1.9 $
 */
public class MBeanException extends JMException
{
   private static final long serialVersionUID = 4066342430588744142L;

   /**
    * @serial The nested exception
    */
   private final Exception exception;

   public MBeanException(Exception x)
   {
      this.exception = x;
   }

   public MBeanException(Exception x, String message)
   {
      super(message);
      this.exception = x;
   }

   public String getMessage()
   {
      return super.getMessage() + " nested exception is " + getTargetException();
   }

   public Exception getTargetException()
   {
      return exception;
   }

   public Throwable getCause()
   {
      return getTargetException();
   }

   public void printStackTrace()
   {
      if (exception == null)
      {
         super.printStackTrace();
      }
      else
      {
         synchronized (System.err)
         {
            System.err.println(this);
            exception.printStackTrace();
         }
      }
   }

   public void printStackTrace(PrintStream s)
   {
      if (exception == null)
      {
         super.printStackTrace(s);
      }
      else
      {
         synchronized (s)
         {
            s.println(this);
            exception.printStackTrace(s);
         }
      }
   }

   public void printStackTrace(PrintWriter w)
   {
      if (exception == null)
      {
         super.printStackTrace(w);
      }
      else
      {
         synchronized (w)
         {
            w.println(this);
            exception.printStackTrace(w);
         }
      }
   }
}

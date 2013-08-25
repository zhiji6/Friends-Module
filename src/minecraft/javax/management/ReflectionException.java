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
 * Thrown when an MBeanServer operation encounter a Java reflection exception such as ClassNotFoundException,
 * NoSuchMethodException, etc; it wraps the actual exception thrown
 *
 * @version $Revision: 1.8 $
 */
public class ReflectionException extends JMException
{
   private static final long serialVersionUID = 9170809325636915553L;

   /**
    * @serial The nested reflection exception
    */
   private Exception exception;

   /**
    * Creates a new ReflectionException
    *
    * @param x The nested Exception
    */
   public ReflectionException(Exception x)
   {
      this.exception = x;
   }

   /**
    * Creates a new ReflectionException
    *
    * @param x       The nested Exception
    * @param message The message
    */
   public ReflectionException(Exception x, String message)
   {
      super(message);
      this.exception = x;
   }

   public String getMessage()
   {
      return super.getMessage() + " nested exception is " + exception;
   }

   /**
    * Returns the nested reflection Exception
    */
   public Exception getTargetException()
   {
      return exception;
   }

   /**
    * Returns the nested reflection Exception
    */
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

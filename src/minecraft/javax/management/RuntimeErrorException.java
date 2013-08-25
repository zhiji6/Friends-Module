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
 * Thrown when an Error is thrown by the MBeanServer; it wraps the actual error thrown.
 *
 * @version $Revision: 1.7 $
 */
public class RuntimeErrorException extends JMRuntimeException
{
   private static final long serialVersionUID = 704338937753949796L;

   /**
    * @serial The nested error
    */
   private Error error;

   /**
    * Creates a new RuntimeErrorException
    *
    * @param error The nested Error
    */
   public RuntimeErrorException(Error error)
   {
      this.error = error;
   }

   /**
    * Creates a new RuntimeErrorException
    *
    * @param error   The nested Error
    * @param message The message
    */
   public RuntimeErrorException(Error error, String message)
   {
      super(message);
      this.error = error;
   }

   public String getMessage()
   {
      return super.getMessage() + " nested error is " + error;
   }

   /**
    * Returns the nested Error
    */
   public Error getTargetError()
   {
      return error;
   }

   /**
    * Returns the nested Error
    */
   public Throwable getCause()
   {
      return getTargetError();
   }

   public void printStackTrace()
   {
      if (error == null)
      {
         super.printStackTrace();
      }
      else
      {
         synchronized (System.err)
         {
            System.err.println(this);
            error.printStackTrace();
         }
      }
   }

   public void printStackTrace(PrintStream s)
   {
      if (error == null)
      {
         super.printStackTrace(s);
      }
      else
      {
         synchronized (s)
         {
            s.println(this);
            error.printStackTrace(s);
         }
      }
   }

   public void printStackTrace(PrintWriter w)
   {
      if (error == null)
      {
         super.printStackTrace(w);
      }
      else
      {
         synchronized (w)
         {
            w.println(this);
            error.printStackTrace(w);
         }
      }
   }
}

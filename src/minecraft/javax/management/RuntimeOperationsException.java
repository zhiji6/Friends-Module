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
 * Thrown when a RuntimeException is thrown by the MBeanServer when executing MBeanServer methods.
 *
 * @version $Revision: 1.9 $
 */
public class RuntimeOperationsException extends JMRuntimeException
{
   private static final long serialVersionUID = -8408923047489133588L;

   /**
    * @serial The nested RuntimeException
    */
   private RuntimeException runtimeException;

   /**
    * Creates a new RuntimeOperationsException
    *
    * @param x The nested RuntimeException
    */
   public RuntimeOperationsException(RuntimeException x)
   {
      this.runtimeException = x;
   }

   /**
    * Creates a new RuntimeOperationsException
    *
    * @param x       The nested RuntimeException
    * @param message The message
    */
   public RuntimeOperationsException(RuntimeException x, String message)
   {
      super(message);
      this.runtimeException = x;
   }

   /**
    * Returns the nested RuntimeException
    */
   public RuntimeException getTargetException()
   {
      return runtimeException;
   }

   /**
    * Returns the nested RuntimeException
    */
   public Throwable getCause()
   {
      return getTargetException();
   }

   public String getMessage()
   {
      return super.getMessage() + " nested runtime exception is " + runtimeException;
   }

   public void printStackTrace()
   {
      if (runtimeException == null)
      {
         super.printStackTrace();
      }
      else
      {
         synchronized (System.err)
         {
            System.err.println(this);
            runtimeException.printStackTrace();
         }
      }
   }

   public void printStackTrace(PrintStream s)
   {
      if (runtimeException == null)
      {
         super.printStackTrace(s);
      }
      else
      {
         synchronized (s)
         {
            s.println(this);
            runtimeException.printStackTrace(s);
         }
      }
   }

   public void printStackTrace(PrintWriter w)
   {
      if (runtimeException == null)
      {
         super.printStackTrace(w);
      }
      else
      {
         synchronized (w)
         {
            w.println(this);
            runtimeException.printStackTrace(w);
         }
      }
   }
}

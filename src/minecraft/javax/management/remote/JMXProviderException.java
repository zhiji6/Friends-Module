/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.IOException;

/**
 * @version $Revision: 1.5 $
 */
public class JMXProviderException extends IOException
{
   /**
    * @serial The Throwable cause
    */
   private Throwable cause;

   private static final long serialVersionUID = -3166703627550447198l;

   public JMXProviderException()
   {
   }

   public JMXProviderException(String message)
   {
      super(message);
   }

   public JMXProviderException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   public Throwable getCause()
   {
      return cause;
   }
}

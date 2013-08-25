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
public class JMXServerErrorException extends IOException
{
   /**
    * @serial The Error cause
    */
   private Error cause;
   private static final long serialVersionUID = 3996732239558744666l;

   public JMXServerErrorException(String message, Error error)
   {
      super(message);
      this.cause = error;
   }

   public Throwable getCause()
   {
      return cause;
   }
}

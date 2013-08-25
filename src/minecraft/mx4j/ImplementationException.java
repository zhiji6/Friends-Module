/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j;

/**
 * Thrown when an internal error in the MX4J implementation is detected.
 * Contact the MX4J mailing list for support when this exception is thrown in your programs.
 *
 * @version $Revision: 1.6 $
 */
public class ImplementationException extends RuntimeException
{
   public ImplementationException()
   {
   }

   public ImplementationException(String message)
   {
      super(message);
   }
}

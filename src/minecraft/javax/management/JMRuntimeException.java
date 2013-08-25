/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.7 $
 */
public class JMRuntimeException extends RuntimeException
{
   private static final long serialVersionUID = 6573344628407841861L;

   public JMRuntimeException()
   {
   }

   public JMRuntimeException(String message)
   {
      super(message);
   }
}

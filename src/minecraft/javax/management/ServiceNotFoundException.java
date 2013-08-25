/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Thrown when the requested service is not found
 *
 * @version $Revision: 1.7 $
 */
public class ServiceNotFoundException extends OperationsException
{
   private static final long serialVersionUID = -3990675661956646827L;

   public ServiceNotFoundException()
   {
   }

   public ServiceNotFoundException(String message)
   {
      super(message);
   }
}

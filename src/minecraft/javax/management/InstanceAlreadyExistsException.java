/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.8 $
 */
public class InstanceAlreadyExistsException extends OperationsException
{
   private static final long serialVersionUID = 8893743928912733931L;

   public InstanceAlreadyExistsException()
   {
   }

   public InstanceAlreadyExistsException(String message)
   {
      super(message);
   }
}

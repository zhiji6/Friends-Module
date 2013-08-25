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
public class InvalidAttributeValueException extends OperationsException
{
   private static final long serialVersionUID = 2164571879317142449L;

   public InvalidAttributeValueException()
   {
   }

   public InvalidAttributeValueException(String message)
   {
      super(message);
   }
}

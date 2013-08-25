/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

/**
 * @version $Revision: 1.6 $
 */
public class InvalidRoleInfoException extends RelationException
{
   private static final long serialVersionUID = 7517834705158932074L;

   // Thrown when the minimum role amount is greater than the maximum
   public InvalidRoleInfoException()
   {
      super();
   }

   public InvalidRoleInfoException(String message)
   {
      super(message);
   }
}
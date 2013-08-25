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
public class InvalidRoleValueException extends RelationException
{
   private static final long serialVersionUID = -2066091747301983721L;

   public InvalidRoleValueException()
   {
      super();
   }

   public InvalidRoleValueException(String s)
   {
      super(s);
   }
}
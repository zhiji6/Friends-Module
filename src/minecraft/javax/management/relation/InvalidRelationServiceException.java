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
public class InvalidRelationServiceException extends RelationException
{
   private static final long serialVersionUID = 3400722103759507559L;

   //Thrown when a relationType name already exists, or no roleInfo provided, or one null RoleInfo
   public InvalidRelationServiceException()
   {
      super();
   }

   public InvalidRelationServiceException(String message)
   {
      super(message);
   }
}
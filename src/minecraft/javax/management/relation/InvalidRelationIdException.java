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
public class InvalidRelationIdException extends RelationException
{
   private static final long serialVersionUID = -7115040321202754171L;

   public InvalidRelationIdException()
   {
      super();
   }

   // Thrown when a relation id already exists in the relation service
   public InvalidRelationIdException(String message)
   {
      super(message);
   }
}
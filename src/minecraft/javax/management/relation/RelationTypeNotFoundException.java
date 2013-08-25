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
public class RelationTypeNotFoundException extends RelationException
{
   private static final long serialVersionUID = 1274155316284300752L;

   // Thrown when there is no relation type with the given name
   public RelationTypeNotFoundException()
   {
      super();
   }

   public RelationTypeNotFoundException(String message)
   {
      super(message);
   }
}
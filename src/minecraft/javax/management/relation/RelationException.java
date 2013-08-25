/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import javax.management.JMException;

/**
 * @version $Revision: 1.7 $
 */
public class RelationException extends JMException
{
   private static final long serialVersionUID = 5434016005679159613L;

   public RelationException()
   {
      super();
   }

   public RelationException(String message)
   {
      super(message);
   }
}
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
public class AttributeNotFoundException extends OperationsException
{
   private static final long serialVersionUID = 6511584241791106926L;

   public AttributeNotFoundException()
   {
   }

   public AttributeNotFoundException(String message)
   {
      super(message);
   }
}

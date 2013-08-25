/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.6 $
 * @serial include
 */
class BooleanValueExp extends QueryEval implements ValueExp
{
   private static final long serialVersionUID = 7754922052666594581L;

   private final boolean val;

   BooleanValueExp(boolean val)
   {
      this.val = val;
   }

   boolean booleanValue()
   {
      return val;
   }

   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      return this;
   }
}

/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * The ValueExp that represents a number.
 *
 * @version $Revision: 1.6 $
 * @serial include
 */
class NumericValueExp extends QueryEval implements ValueExp
{
   private static final long serialVersionUID = -4679739485102359104L;

   /**
    * @serial The number
    */
   private Number val;

   NumericValueExp(Number val)
   {
      this.val = val;
   }

   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      return this;
   }

   boolean isDouble()
   {
      return val instanceof Float || val instanceof Double;
   }

   double doubleValue()
   {
      return val.doubleValue();
   }

   long longValue()
   {
      return val.longValue();
   }
}

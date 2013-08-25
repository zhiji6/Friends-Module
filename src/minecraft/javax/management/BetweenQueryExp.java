/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.7 $
 * @serial include
 */
class BetweenQueryExp extends QueryEval implements QueryExp
{
   private static final long serialVersionUID = -2933597532866307444L;

   /**
    * @serial The lower value
    */
   private final ValueExp exp1;
   /**
    * @serial The value to test
    */
   private final ValueExp exp2;
   /**
    * The upper value
    */
   private final ValueExp exp3;

   BetweenQueryExp(ValueExp exp1, ValueExp exp2, ValueExp exp3)
   {
      this.exp1 = exp1;
      this.exp2 = exp2;
      this.exp3 = exp3;
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      if (exp1 != null) exp1.setMBeanServer(server);
      if (exp2 != null) exp2.setMBeanServer(server);
      if (exp3 != null) exp3.setMBeanServer(server);
   }

   public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      if (exp1 != null && exp2 != null && exp3 != null)
      {
         ValueExp val1 = exp1.apply(name);
         ValueExp val2 = exp2.apply(name);
         ValueExp val3 = exp3.apply(name);

         if (val1 instanceof NumericValueExp && val2 instanceof NumericValueExp && val3 instanceof NumericValueExp)
         {
            NumericValueExp num1 = (NumericValueExp)val1;
            NumericValueExp num2 = (NumericValueExp)val2;
            NumericValueExp num3 = (NumericValueExp)val3;

            if (num1.isDouble() || num2.isDouble() || num3.isDouble())
            {
               return isBetween(new Double(num1.doubleValue()), new Double(num2.doubleValue()), new Double(num3.doubleValue()));
            }
            else
            {
               return isBetween(new Long(num1.longValue()), new Long(num2.longValue()), new Long(num3.longValue()));
            }
         }
/*
         else if (val1 instanceof StringValueExp && val2 instanceof StringValueExp && val3 instanceof StringValueExp)
         {
            String s1 = ((StringValueExp)val1).getValue();
            String s2 = ((StringValueExp)val2).getValue();
            String s3 = ((StringValueExp)val3).getValue();
            return isBetween(s1, s2, s3);
         }
*/
         else
         {
            throw new InvalidApplicationException("Values are not numeric");
         }
      }

      return false;
   }

   private boolean isBetween(Comparable c1, Comparable c2, Comparable c3)
   {
      if (c1 == null && c2 == null && c3 == null) return true;
      if (c1 == null && (c2 == null || c3 == null)) return true;
      if (c1 == null) return false;
      if (c1 != null && (c2 == null || c3 == null)) return false;
      return c1.compareTo(c2) >= 0 && c1.compareTo(c3) <= 0;
   }
}

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
class BinaryRelQueryExp extends QueryEval implements QueryExp
{
   private static final long serialVersionUID = -5690656271650491000L;

   /**
    * @serial The left side expression
    */
   private final ValueExp exp1;
   /**
    * @serial The infix operator
    */
   private final int relOp;
   /**
    * @serial The right side expression
    */
   private final ValueExp exp2;

   BinaryRelQueryExp(int operation, ValueExp exp1, ValueExp exp2)
   {
      this.relOp = operation;
      this.exp1 = exp1;
      this.exp2 = exp2;
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      if (exp1 != null) exp1.setMBeanServer(server);
      if (exp2 != null) exp2.setMBeanServer(server);
   }

   public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      if (exp1 == null && exp2 == null && (relOp == Query.EQ || relOp == Query.GE || relOp == Query.LE))
      {
         return true;
      }

      if (exp1 != null && exp2 != null)
      {
         ValueExp val1 = exp1.apply(name);
         ValueExp val2 = exp2.apply(name);

         if (val1 instanceof NumericValueExp && val2 instanceof NumericValueExp)
         {
            NumericValueExp num1 = (NumericValueExp)val1;
            NumericValueExp num2 = (NumericValueExp)val2;

            if (num1.isDouble() || num2.isDouble())
            {
               return compare(new Double(num1.doubleValue()), new Double(num2.doubleValue()));
            }
            else
            {
               return compare(new Long(num1.longValue()), new Long(num2.longValue()));
            }
         }
         else if (val1 instanceof BooleanValueExp && val2 instanceof BooleanValueExp)
         {
            boolean b1 = ((BooleanValueExp)val1).booleanValue();
            boolean b2 = ((BooleanValueExp)val2).booleanValue();
            return compare(new Long(b1 ? 1 : 0), new Long(b2 ? 1 : 0));
         }
         else if (val1 instanceof StringValueExp && val2 instanceof StringValueExp)
         {
            String s1 = ((StringValueExp)val1).getValue();
            String s2 = ((StringValueExp)val2).getValue();
            return compare(s1, s2);
         }
      }

      return false;
   }

   private boolean compare(Comparable c1, Comparable c2)
   {
      switch (relOp)
      {
         case Query.EQ:
            if (c1 == null && c2 == null) return true;
            if (c1 == null || c2 == null) return false;
            return c1.equals(c2);
         case Query.GE:
            if (c1 == null && c2 == null) return true;
            if (c1 == null && c2 != null) return false;
            if (c1 != null && c2 == null) return true;
            return c1.compareTo(c2) >= 0;
         case Query.LE:
            if (c1 == null && c2 == null) return true;
            if (c1 == null && c2 != null) return true;
            if (c1 != null && c2 == null) return false;
            return c1.compareTo(c2) <= 0;
         case Query.GT:
            if (c1 == null && c2 == null) return false;
            if (c1 == null && c2 != null) return false;
            if (c1 != null && c2 == null) return true;
            return c1.compareTo(c2) > 0;
         case Query.LT:
            if (c1 == null && c2 == null) return false;
            if (c1 == null && c2 != null) return true;
            if (c1 != null && c2 == null) return false;
            return c1.compareTo(c2) < 0;
      }
      return false;
   }
}

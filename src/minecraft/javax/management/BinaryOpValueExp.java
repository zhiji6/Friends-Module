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
 */
class BinaryOpValueExp extends QueryEval implements ValueExp
{
   private static final long serialVersionUID = 1216286847881456786L;

   private final int op;
   private final ValueExp exp1;
   private final ValueExp exp2;

   BinaryOpValueExp(int op, ValueExp exp1, ValueExp exp2)
   {
      this.op = op;
      this.exp1 = exp1;
      this.exp2 = exp2;
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      if (exp1 != null) exp1.setMBeanServer(server);
      if (exp2 != null) exp2.setMBeanServer(server);
   }

   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      if (exp1 != null && exp2 != null)
      {
         ValueExp val1 = exp1.apply(name);
         ValueExp val2 = exp2.apply(name);

         if (val1 instanceof NumericValueExp)
         {
            if (val2 instanceof NumericValueExp)
            {
               NumericValueExp num1 = (NumericValueExp)val1;
               NumericValueExp num2 = (NumericValueExp)val2;

               if (num1.isDouble() || num2.isDouble())
               {
                  double d1 = num1.doubleValue();
                  double d2 = num2.doubleValue();
                  switch (op)
                  {
                     case Query.PLUS:
                        return Query.value(d1 + d2);
                     case Query.MINUS:
                        return Query.value(d1 - d2);
                     case Query.TIMES:
                        return Query.value(d1 * d2);
                     case Query.DIV:
                        return Query.value(d1 / d2);
                  }
               }
               else
               {
                  long l1 = num1.longValue();
                  long l2 = num2.longValue();
                  switch (op)
                  {
                     case Query.PLUS:
                        return Query.value(l1 + l2);
                     case Query.MINUS:
                        return Query.value(l1 - l2);
                     case Query.TIMES:
                        return Query.value(l1 * l2);
                     case Query.DIV:
                        return Query.value(l1 / l2);
                  }
               }
            }
            else
            {
               throw new BadBinaryOpValueExpException(val2);
            }
         }
         else if (val1 instanceof StringValueExp)
         {
            if (val2 instanceof StringValueExp)
            {
               String s1 = ((StringValueExp)val1).getValue();
               String s2 = ((StringValueExp)val2).getValue();
               switch (op)
               {
                  case Query.PLUS:
                     return Query.value(String.valueOf(s1) + String.valueOf(s2));
                  default:
                     throw new BadStringOperationException("Trying to perform an operation on Strings that is not concatenation");
               }
            }
            else
            {
               throw new BadBinaryOpValueExpException(val2);
            }
         }
         else
         {
            throw new BadBinaryOpValueExpException(val1);
         }
      }
      throw new BadBinaryOpValueExpException(null);
   }
}

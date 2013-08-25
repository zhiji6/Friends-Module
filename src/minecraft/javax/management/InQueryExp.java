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
class InQueryExp extends QueryEval implements QueryExp
{
   private static final long serialVersionUID = -5801329450358952434L;

   /**
    * @serial The value to be tested
    */
   private final ValueExp val;
   /**
    * @serial The allowed values
    */
   private final ValueExp[] valueList;

   InQueryExp(ValueExp val, ValueExp[] valueList)
   {
      this.val = val;
      this.valueList = valueList;
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      if (val != null) val.setMBeanServer(server);
      if (valueList != null)
      {
         for (int i = 0; i < valueList.length; ++i)
         {
            ValueExp v = valueList[i];
            if (v != null) v.setMBeanServer(server);
         }
      }
   }

   public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      if (val != null && valueList != null)
      {
         ValueExp valueExp = val.apply(name);
         if (valueExp instanceof NumericValueExp)
         {
            NumericValueExp numExp = (NumericValueExp)valueExp;
            if (numExp.isDouble())
            {
               for (int i = 0; i < valueList.length; ++i)
               {
                  ValueExp exp = valueList[i];
                  if (exp instanceof NumericValueExp)
                  {
                     if (((NumericValueExp)exp).doubleValue() == numExp.doubleValue()) return true;
                  }
               }
            }
            else
            {
               for (int i = 0; i < valueList.length; ++i)
               {
                  ValueExp exp = valueList[i];
                  if (exp instanceof NumericValueExp)
                  {
                     if (((NumericValueExp)exp).longValue() == numExp.longValue()) return true;
                  }
               }
            }
         }
         else if (valueExp instanceof StringValueExp)
         {
            String s1 = ((StringValueExp)valueExp).getValue();
            for (int i = 0; i < valueList.length; ++i)
            {
               ValueExp exp = valueList[i];
               if (exp instanceof StringValueExp)
               {
                  String s2 = ((StringValueExp)exp).getValue();
                  if (s1 == null && s2 == null) return true;
                  if (s1 != null && s2 != null)
                  {
                     if (s1.equals(s2)) return true;
                  }
               }
            }
         }
      }
      return false;
   }
}

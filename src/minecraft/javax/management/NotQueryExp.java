/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * The QueryExp for the 'not' operation
 *
 * @version $Revision: 1.6 $
 * @serial include
 */
class NotQueryExp extends QueryEval implements QueryExp
{
   private static final long serialVersionUID = 5269643775896723397L;

   /**
    * @serial The expression to negate
    */
   private QueryExp exp;

   NotQueryExp(QueryExp exp)
   {
      this.exp = exp;
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      if (exp != null) exp.setMBeanServer(server);
   }

   public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      if (exp != null) return !exp.apply(name);
      return false;
   }
}

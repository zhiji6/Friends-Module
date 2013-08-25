/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * The QueryExp for an 'or' operation
 *
 * @version $Revision: 1.6 $
 * @serial include
 */
class OrQueryExp extends QueryEval implements QueryExp
{
   private static final long serialVersionUID = 2962973084421716523L;

   /**
    * @serial The left-side expression
    */
   private QueryExp exp1;
   /**
    * @serial The right-side expression
    */
   private QueryExp exp2;

   OrQueryExp(QueryExp exp1, QueryExp exp2)
   {
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
      return exp1.apply(name) || exp2.apply(name);
   }
}

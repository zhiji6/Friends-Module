/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;

/**
 * Represents a query expression. Query expressions are created with the {@link Query} class.
 *
 * @version $Revision: 1.7 $
 */
public interface QueryExp extends Serializable
{
   /**
    * Applies this expression on the specified MBean.
    *
    * @param name The <code>ObjectName</code> of the <code>MBean</code> on which the expression is applied.
    * @return True if the query was successfully applied, false otherwise.
    */
   public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException;

   /**
    * Sets the <code>MBeanServer</code> used (possibly) to apply the query expression, for example to retrieve
    * the value of an attribute for the MBean specified in {@link #apply}
    */
   public void setMBeanServer(MBeanServer server);
}

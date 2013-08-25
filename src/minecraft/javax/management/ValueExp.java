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
 * Represents values that can be passed to relational expressions such as strings, numbers, booleans and MBean attribute values.
 *
 * @version $Revision: 1.7 $
 */
public interface ValueExp extends Serializable
{
   /**
    * Applies this expression on the specified MBean.
    *
    * @param name The <code>ObjectName</code> of the <code>MBean</code> on which the expression is applied.
    * @return The value expression that has been applied to the MBean
    */
   public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException;

   /**
    * Sets the <code>MBeanServer</code> used (possibly) to apply the value expression
    */
   public void setMBeanServer(MBeanServer server);
}

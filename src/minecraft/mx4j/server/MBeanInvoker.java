/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * Invokes methods on standard MBeans. <br />
 * Actually three implementations are available: two that uses reflection and one that generates on-the-fly a customized
 * MBeanInvoker per each MBean and that is implemented with direct calls via bytecode generation. <br />
 * The default is the direct call version, that uses the <a href="http://jakarta.apache.org/bcel">BCEL</a> to generate
 * the required bytecode on-the-fly. <br>
 * In the future may be the starting point for MBean interceptors.
 *
 * @version $Revision: 1.6 $
 */
public interface MBeanInvoker
{
   /**
    * Invokes the specified operation on the MBean instance
    */
   public Object invoke(MBeanMetaData metadata, String method, String[] signature, Object[] args) throws MBeanException, ReflectionException;

   /**
    * Returns the value of the specified attribute.
    */
   public Object getAttribute(MBeanMetaData metadata, String attribute) throws MBeanException, AttributeNotFoundException, ReflectionException;

   /**
    * Sets the value of the specified attribute.
    */
   public void setAttribute(MBeanMetaData metadata, Attribute attribute) throws MBeanException, AttributeNotFoundException, InvalidAttributeValueException, ReflectionException;
}

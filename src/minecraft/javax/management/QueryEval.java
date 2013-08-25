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
 * The base class for QueryExp implementations.
 *
 * @version $Revision: 1.7 $
 */
public abstract class QueryEval implements Serializable
{
   private static final long serialVersionUID = 2675899265640874796L;

   private transient MBeanServer server;
   private static ThreadLocal serverPerThread = new ThreadLocal();

   /**
    * Sets the MBeanServer used by the QueryExp implementation to evaluate the expression.
    */
   public void setMBeanServer(MBeanServer server)
   {
      this.server = server;
      serverPerThread.set(server);
   }

   /**
    * Returns the MBeanServer used by the QueryExp implementation to evaluate the expression.
    * This method is static for a mistake in the JMX spec, should not be needed, but it's implemented
    * for sake of compatibility.
    */
   public static MBeanServer getMBeanServer()
   {
      return (MBeanServer)serverPerThread.get();
   }
}

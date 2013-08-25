/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

import java.util.ArrayList;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import mx4j.ImplementationException;

/**
 * MBean that configures the MBeanServer --> MBean interceptor chain.
 *
 * @version $Revision: 1.9 $
 */
public class MBeanServerInterceptorConfigurator implements MBeanServerInterceptorConfiguratorMBean
{
   public static final String OBJECT_NAME = "JMImplementation:type=MBeanServerInterceptorConfigurator";

   private final MBeanServer server;
   private final ArrayList preInterceptors = new ArrayList();
   private final ArrayList postInterceptors = new ArrayList();
   private final ArrayList clientInterceptors = new ArrayList();
   private volatile boolean running;
   private boolean chainModified;
   private MBeanServerInterceptor head;

   /**
    * Creates an instance of this configurator, for the given MBeanServer
    */
   public MBeanServerInterceptorConfigurator(MBeanServer server)
   {
      this.server = server;
      chainModified = true;
   }

   /**
    * Appends the given interceptor, provided by the client, to the existing interceptor chain.
    *
    * @see #registerInterceptor
    */
   public void addInterceptor(MBeanServerInterceptor interceptor)
   {
      synchronized (clientInterceptors)
      {
         clientInterceptors.add(interceptor);
         chainModified = true;
      }
   }

   /**
    * Appends the given interceptor, provided by the client, to the existing interceptor chain and registers it as MBean.
    *
    * @see #addInterceptor
    */
   public void registerInterceptor(MBeanServerInterceptor interceptor, ObjectName name) throws MBeanException
   {
      // First, try register this interceptor. The call will use the old interceptor chain
      try
      {
         server.registerMBean(interceptor, name);
         addInterceptor(interceptor);
      }
      catch (Exception x)
      {
         throw new MBeanException(x, "Could not register interceptor with name " + name);
      }
   }

   /**
    * Removes all the interceptors added via {@link #addInterceptor(MBeanServerInterceptor interceptor)}.
    *
    * @see #addInterceptor
    */
   public void clearInterceptors()
   {
      synchronized (clientInterceptors)
      {
         clientInterceptors.clear();
         chainModified = true;
      }
   }

   /**
    * Adds the given interceptor at the beginning of the interceptor chain, before the custom interceptors that may be added
    * via {@link #addInterceptor}.
    * This method is called by the MBeanServer during initialization, to configure the interceptors needed to work properly.
    */
   public void addPreInterceptor(MBeanServerInterceptor interceptor)
   {
      if (isRunning()) throw new ImplementationException();
      preInterceptors.add(interceptor);
   }

   /**
    * Adds the given interceptor at the end of the interceptor chain, after the custom interceptors that may be added
    * via {@link #addInterceptor}.
    * This method is called by the MBeanServer during initialization, to configure the interceptors needed to work properly.
    */
   public void addPostInterceptor(MBeanServerInterceptor interceptor)
   {
      if (isRunning()) throw new ImplementationException();
      postInterceptors.add(interceptor);
   }

   /**
    * Returns the head interceptor of the interceptor chain.
    * The head interceptor is always present.
    */
   public MBeanServerInterceptor getHeadInterceptor()
   {
      if (!isRunning()) return null;

      if (chainModified) setupChain();

      return head;
   }

   private void setupChain()
   {
      chainModified = false;

      int size = clientInterceptors.size();
      ArrayList chain = new ArrayList(preInterceptors.size() + size + postInterceptors.size());
      chain.addAll(preInterceptors);
      if (size > 0)
      {
         synchronized (clientInterceptors)
         {
            chain.addAll(clientInterceptors);
         }
      }
      chain.addAll(postInterceptors);

      // Set the chain on the first interceptor
      MBeanServerInterceptor first = (MBeanServerInterceptor)chain.get(0);
      first.setChain(chain);

      head = first;
   }

   /**
    * Starts this configurator, so that the MBeanServer is now able to accept incoming calls.
    *
    * @see #stop
    * @see #isRunning
    */
   public void start()
   {
      if (!isRunning())
      {
         running = true;
      }
   }

   /**
    * Stops this configurator, so that the MBeanServer is not able to accept incoming calls.
    *
    * @see #start
    */
   public void stop()
   {
      if (isRunning())
      {
         running = false;
      }
   }

   /**
    * Returns whether this configurator is running and thus if the MBeanServer can accept incoming calls
    *
    * @see #start
    */
   public boolean isRunning()
   {
      return running;
   }
}

/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.server.Unreferenced;
import java.security.AccessControlContext;
import java.util.Map;
import java.util.Set;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.NotificationResult;
import javax.security.auth.Subject;

import mx4j.remote.rmi.RMIConnectionInvoker;
import mx4j.remote.rmi.RMIConnectionSubjectInvoker;

/**
 * @version $Revision: 1.10 $
 */
public class RMIConnectionImpl implements RMIConnection, Unreferenced
{
   private final RMIServerImpl server;
   private final String connectionId;
   private final ClassLoader defaultClassLoader;
   private final Subject subject;
   private final Map environment;
   private RMIConnection chain;
   private AccessControlContext context;

   public RMIConnectionImpl(RMIServerImpl rmiServer, String connectionId, ClassLoader defaultClassLoader, Subject subject, Map environment)
   {
      this.server = rmiServer;
      this.connectionId = connectionId;
      this.defaultClassLoader = defaultClassLoader;
      this.subject = subject;
      this.environment = environment;
   }

   public String getConnectionId() throws IOException
   {
      // Do not forward to the chain
      return connectionId;
   }

   public void close() throws IOException
   {
      // Forward the call to the chain, to free resources
      getChain().close();
      server.clientClosed(this);
   }

   public void unreferenced()
   {
      try
      {
         close();
      }
      catch (IOException ignored)
      {
      }
   }

   private synchronized RMIConnection getChain()
   {
      if (chain == null)
      {
         // TODO: here we hardcode the server invocation chain. Maybe worth to remove this hardcoding ?
         RMIConnection serverInvoker = new RMIConnectionInvoker(server.getMBeanServer(), defaultClassLoader, environment);
         chain = RMIConnectionSubjectInvoker.newInstance(serverInvoker, subject, context, environment);
      }
      return chain;
   }

   public void addNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback, Subject delegate) throws InstanceNotFoundException, IOException
   {
      getChain().addNotificationListener(name, listener, filter, handback, delegate);
   }

   public Integer[] addNotificationListeners(ObjectName[] names, MarshalledObject[] filters, Subject[] delegates) throws InstanceNotFoundException, IOException
   {
      return getChain().addNotificationListeners(names, filters, delegates);
   }

   public ObjectInstance createMBean(String className, ObjectName name, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  IOException
   {
      return getChain().createMBean(className, name, delegationSubject);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  InstanceNotFoundException,
                  IOException
   {
      return getChain().createMBean(className, name, loaderName, delegationSubject);
   }

   public ObjectInstance createMBean(String className, ObjectName name, MarshalledObject params, String[] signature, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  IOException
   {
      return getChain().createMBean(className, name, params, signature, delegationSubject);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, MarshalledObject params, String[] signature, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  InstanceNotFoundException,
                  IOException
   {
      return getChain().createMBean(className, name, loaderName, params, signature, delegationSubject);
   }

   public void unregisterMBean(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, MBeanRegistrationException, IOException
   {
      getChain().unregisterMBean(name, delegationSubject);
   }

   public ObjectInstance getObjectInstance(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, IOException
   {
      return getChain().getObjectInstance(name, delegationSubject);
   }

   public Set queryMBeans(ObjectName name, MarshalledObject query, Subject delegationSubject) throws IOException
   {
      return getChain().queryMBeans(name, query, delegationSubject);
   }

   public Set queryNames(ObjectName name, MarshalledObject query, Subject delegationSubject) throws IOException
   {
      return getChain().queryNames(name, query, delegationSubject);
   }

   public boolean isRegistered(ObjectName name, Subject delegationSubject) throws IOException
   {
      return getChain().isRegistered(name, delegationSubject);
   }

   public Integer getMBeanCount(Subject delegationSubject) throws IOException
   {
      return getChain().getMBeanCount(delegationSubject);
   }

   public Object getAttribute(ObjectName name, String attribute, Subject delegate)
           throws MBeanException,
                  AttributeNotFoundException,
                  InstanceNotFoundException,
                  ReflectionException,
                  IOException
   {
      return getChain().getAttribute(name, attribute, delegate);
   }

   public AttributeList getAttributes(ObjectName name, String[] attributes, Subject delegationSubject)
           throws InstanceNotFoundException, ReflectionException, IOException
   {
      return getChain().getAttributes(name, attributes, delegationSubject);
   }

   public void setAttribute(ObjectName name, MarshalledObject attribute, Subject delegationSubject)
           throws InstanceNotFoundException,
                  AttributeNotFoundException,
                  InvalidAttributeValueException,
                  MBeanException,
                  ReflectionException,
                  IOException
   {
      getChain().setAttribute(name, attribute, delegationSubject);
   }

   public AttributeList setAttributes(ObjectName name, MarshalledObject attributes, Subject delegationSubject)
           throws InstanceNotFoundException,
                  ReflectionException,
                  IOException
   {
      return getChain().setAttributes(name, attributes, delegationSubject);
   }

   public Object invoke(ObjectName name, String operationName, MarshalledObject params, String[] signature, Subject delegationSubject)
           throws InstanceNotFoundException,
                  MBeanException,
                  ReflectionException,
                  IOException
   {
      return getChain().invoke(name, operationName, params, signature, delegationSubject);
   }

   public String getDefaultDomain(Subject delegationSubject) throws IOException
   {
      return getChain().getDefaultDomain(delegationSubject);
   }

   public String[] getDomains(Subject delegationSubject) throws IOException
   {
      return getChain().getDomains(delegationSubject);
   }

   public MBeanInfo getMBeanInfo(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
   {
      return getChain().getMBeanInfo(name, delegationSubject);
   }

   public boolean isInstanceOf(ObjectName name, String className, Subject delegationSubject) throws InstanceNotFoundException, IOException
   {
      return getChain().isInstanceOf(name, className, delegationSubject);
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener, Subject delegate)
           throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      getChain().removeNotificationListener(name, listener, delegate);
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback, Subject delegationSubject)
           throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      getChain().removeNotificationListener(name, listener, filter, handback, delegationSubject);
   }

   public void removeNotificationListeners(ObjectName name, Integer[] listenerIDs, Subject delegationSubject) throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      getChain().removeNotificationListeners(name, listenerIDs, delegationSubject);
   }

   public NotificationResult fetchNotifications(long clientSequenceNumber, int maxNotifications, long timeout) throws IOException
   {
      return getChain().fetchNotifications(clientSequenceNumber, maxNotifications, timeout);
   }

   void setContext(AccessControlContext context)
   {
      this.context = context;
   }
}

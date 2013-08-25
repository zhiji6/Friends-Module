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
import java.rmi.Remote;
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

/**
 * @version $Revision: 1.4 $
 */
public interface RMIConnection extends Remote
{
   public String getConnectionId() throws IOException;

   public void close() throws IOException;

   public ObjectInstance createMBean(String className, ObjectName name, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  IOException;

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  InstanceNotFoundException,
                  IOException;

   public ObjectInstance createMBean(String className, ObjectName name, MarshalledObject params, String[] signature, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  IOException;

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, MarshalledObject params, String[] signature, Subject delegationSubject)
           throws ReflectionException,
                  InstanceAlreadyExistsException,
                  MBeanRegistrationException,
                  MBeanException,
                  NotCompliantMBeanException,
                  InstanceNotFoundException,
                  IOException;

   public void unregisterMBean(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, MBeanRegistrationException, IOException;

   public ObjectInstance getObjectInstance(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, IOException;

   public Set queryMBeans(ObjectName name, MarshalledObject query, Subject delegationSubject) throws IOException;

   public Set queryNames(ObjectName name, MarshalledObject query, Subject delegationSubject) throws IOException;

   public boolean isRegistered(ObjectName name, Subject delegationSubject) throws IOException;

   public Integer getMBeanCount(Subject delegationSubject) throws IOException;

   public Object getAttribute(ObjectName name, String attribute, Subject delegationSubject)
           throws MBeanException,
                  AttributeNotFoundException,
                  InstanceNotFoundException,
                  ReflectionException,
                  IOException;

   public AttributeList getAttributes(ObjectName name, String[] attributes, Subject delegationSubject)
           throws InstanceNotFoundException, ReflectionException, IOException;

   public void setAttribute(ObjectName name, MarshalledObject attribute, Subject delegationSubject)
           throws InstanceNotFoundException,
                  AttributeNotFoundException,
                  InvalidAttributeValueException,
                  MBeanException,
                  ReflectionException,
                  IOException;

   public AttributeList setAttributes(ObjectName name, MarshalledObject attributes, Subject delegationSubject)
           throws InstanceNotFoundException,
                  ReflectionException,
                  IOException;

   public Object invoke(ObjectName name, String operationName, MarshalledObject params, String[] signature, Subject delegationSubject)
           throws InstanceNotFoundException,
                  MBeanException,
                  ReflectionException,
                  IOException;

   public String getDefaultDomain(Subject delegationSubject) throws IOException;

   public String[] getDomains(Subject delegationSubject) throws IOException;

   public MBeanInfo getMBeanInfo(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException;

   public boolean isInstanceOf(ObjectName name, String className, Subject delegationSubject) throws InstanceNotFoundException, IOException;

   public void addNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback, Subject delegationSubject)
           throws InstanceNotFoundException, IOException;

   public void removeNotificationListener(ObjectName name, ObjectName listener, Subject delegationSubject)
           throws InstanceNotFoundException, ListenerNotFoundException, IOException;

   public void removeNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback, Subject delegationSubject)
           throws InstanceNotFoundException, ListenerNotFoundException, IOException;

   public Integer[] addNotificationListeners(ObjectName[] names, MarshalledObject[] filters, Subject[] delegationSubjects) throws InstanceNotFoundException, IOException;

   public void removeNotificationListeners(ObjectName name, Integer[] listenerIDs, Subject delegationSubject) throws InstanceNotFoundException, ListenerNotFoundException, IOException;

   public NotificationResult fetchNotifications(long clientSequenceNumber, int maxNotifications, long timeout) throws IOException;
}

/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.ObjectInputStream;
import java.util.Set;
import javax.management.loading.ClassLoaderRepository;

/**
 * A local client can create, register, unregister and access registered MBeans by means of this interface, that is
 * the core component of JMX.
 * An implementation of this interface can only be obtained from {@link MBeanServerFactory}.
 * Almost all methods require an {@link MBeanPermission} to be invoked under SecurityManager.
 *
 * @version $Revision: 1.8 $
 */
public interface MBeanServer extends MBeanServerConnection
{
   public void addNotificationListener(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException;

   public void addNotificationListener(ObjectName observed, ObjectName listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException;

   public void removeNotificationListener(ObjectName observed, ObjectName listener)
           throws InstanceNotFoundException, ListenerNotFoundException;

   public void removeNotificationListener(ObjectName observed, NotificationListener listener)
           throws InstanceNotFoundException, ListenerNotFoundException;

   public void removeNotificationListener(ObjectName observed, ObjectName listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException, ListenerNotFoundException;

   public void removeNotificationListener(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException, ListenerNotFoundException;

   public MBeanInfo getMBeanInfo(ObjectName objectName)
           throws InstanceNotFoundException, IntrospectionException, ReflectionException;

   public boolean isInstanceOf(ObjectName objectName, String className)
           throws InstanceNotFoundException;

   public String[] getDomains();

   public String getDefaultDomain();

   public ObjectInstance createMBean(String className, ObjectName objectName)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException;

   public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException;

   public ObjectInstance createMBean(String className, ObjectName objectName, Object[] args, String[] parameters)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException;

   public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName, Object[] args, String[] parameters)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException;

   public void unregisterMBean(ObjectName objectName)
           throws InstanceNotFoundException, MBeanRegistrationException;

   public Object getAttribute(ObjectName objectName, String attribute)
           throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException;

   public void setAttribute(ObjectName objectName, Attribute attribute)
           throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException;

   public AttributeList getAttributes(ObjectName objectName, String[] attributes)
           throws InstanceNotFoundException, ReflectionException;

   public AttributeList setAttributes(ObjectName objectName, AttributeList attributes)
           throws InstanceNotFoundException, ReflectionException;

   public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] parameters)
           throws InstanceNotFoundException, MBeanException, ReflectionException;

   public Integer getMBeanCount();

   public boolean isRegistered(ObjectName objectname);

   public ObjectInstance getObjectInstance(ObjectName objectName)
           throws InstanceNotFoundException;

   public Set queryMBeans(ObjectName patternName, QueryExp filter);

   public Set queryNames(ObjectName patternName, QueryExp filter);

   /**
    * Instantiates an object of the given class using the MBeanServer's {@link ClassLoaderRepository}.
    * The given class should have a public parameterless constructor.
    *
    * @param className The class name of the object to be instantiated.
    * @return The newly instantiated object.
    * @throws ReflectionException Wraps a Java reflection exception thrown while trying to create the instance
    * @throws MBeanException      Thrown if the constructor of the object throws an exception
    */
   public Object instantiate(String className)
           throws ReflectionException, MBeanException;

   /**
    * Instantiates an object of the given class using the specified ClassLoader MBean.
    * If <code>loaderName</code> is null, the classloader of the MBeanServer will be used.
    * The given class should have a public parameterless constructor.
    *
    * @param className  The class name of the MBean to be instantiated.
    * @param loaderName The object name of the class loader to be used.
    * @return The newly instantiated object.
    * @throws ReflectionException       Wraps a Java reflection exception thrown while trying to create the instance
    * @throws MBeanException            Thrown if the constructor of the object throws an exception
    * @throws InstanceNotFoundException The specified classloader MBean is not registered in the MBeanServer.
    */
   public Object instantiate(String className, ObjectName loaderName)
           throws ReflectionException, MBeanException, InstanceNotFoundException;

   /**
    * Instantiates an object of the given class using the MBeanServer's {@link ClassLoaderRepository}.
    * The given class should have a public constructor matching the given signature, and will be called
    * passing the given arguments.
    *
    * @param className  The class name of the object to be instantiated.
    * @param args       The arguments passed to the constructor.
    * @param parameters The signature of the constructor.
    * @return The newly instantiated object.
    * @throws ReflectionException Wraps a Java reflection exception thrown while trying to create the instance
    * @throws MBeanException      Thrown if the constructor of the object throws an exception
    */
   public Object instantiate(String className, Object[] args, String[] parameters)
           throws ReflectionException, MBeanException;

   /**
    * Instantiates an object of the given class using the specified ClassLoader MBean.
    * If <code>loaderName</code> is null, the classloader of the MBeanServer will be used.
    * The given class should have a public constructor matching the given signature, and will be called
    * passing the given arguments.
    *
    * @param className  The class name of the MBean to be instantiated.
    * @param loaderName The object name of the class loader to be used.
    * @param args       The arguments passed to the constructor.
    * @param parameters The signature of the constructor.
    * @return The newly instantiated object.
    * @throws ReflectionException       Wraps a Java reflection exception thrown while trying to create the instance
    * @throws MBeanException            Thrown if the constructor of the object throws an exception
    * @throws InstanceNotFoundException The specified classloader MBean is not registered in the MBeanServer.
    */
   public Object instantiate(String className, ObjectName loaderName, Object[] args, String[] parameters)
           throws ReflectionException, MBeanException, InstanceNotFoundException;

   /**
    * Registers the given MBean with the given ObjectName.
    * The ObjectName may be null, but the MBean should then implement {@link MBeanRegistration}.
    *
    * @param mbean      The MBean to be registered.
    * @param objectName The ObjectName of the MBean, may be null.
    * @return An ObjectInstance, containing the ObjectName and the Java class name of the newly registered MBean.
    * @throws InstanceAlreadyExistsException An MBean with the same ObjectName is already registered in the MBeanServer.
    * @throws MBeanRegistrationException     Thrown if a problem is encountered during registration.
    * @throws NotCompliantMBeanException     The given MBean is not a JMX compliant MBean
    */
   public ObjectInstance registerMBean(Object mbean, ObjectName objectName)
           throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException;

   /**
    * @deprecated Use {@link #getClassLoader} to obtain the classloader for deserialization.
    */
   public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] bytes)
           throws InstanceNotFoundException, OperationsException, ReflectionException;

   /**
    * @deprecated Use {@link #getClassLoaderRepository} to obtain the ClassLoaderRepository for deserialization.
    */
   public ObjectInputStream deserialize(String className, byte[] bytes)
           throws OperationsException, ReflectionException;

   /**
    * @deprecated Use {@link #getClassLoaderFor} to obtain the classloader for deserialization.
    */
   public ObjectInputStream deserialize(ObjectName objectName, byte[] bytes)
           throws InstanceNotFoundException, OperationsException;

   /**
    * Returns the ClassLoader that was used for loading the named MBean.
    *
    * @param mbeanName The ObjectName of the MBean.
    * @return The ClassLoader used to load the names MBean.
    * @throws InstanceNotFoundException If the named MBean is not found.
    * @since JMX 1.2
    */
   public ClassLoader getClassLoaderFor(ObjectName mbeanName)
           throws InstanceNotFoundException;

   /**
    * Returns the named classloader MBean. If the given <code>loaderName</code> is null, the classloader
    * of the MBeanServer will be used.
    *
    * @param loaderName The ObjectName of the classloader MBean, or null.
    * @return The named classloader MBean.
    * @throws InstanceNotFoundException If the named classloader MBean is not found.
    * @since JMX 1.2
    */
   public ClassLoader getClassLoader(ObjectName loaderName)
           throws InstanceNotFoundException;

   /**
    * Returns the ClassLoaderRepository for this MBeanServer.
    *
    * @since JMX 1.2
    */
   public ClassLoaderRepository getClassLoaderRepository();
}

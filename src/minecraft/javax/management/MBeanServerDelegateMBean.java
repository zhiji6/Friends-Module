/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Defines the management interface for the MBeanServerDelegate.
 *
 * @version $Revision: 1.6 $
 */
public interface MBeanServerDelegateMBean
{
   /**
    * Returns the implementation name, for example 'MX4J'.
    */
   public String getImplementationName();

   /**
    * Returns the implementation vendor, for example 'The MX4J Team'.
    */
   public String getImplementationVendor();

   /**
    * Returns the implementation version, for example '1.1'.
    */
   public String getImplementationVersion();

   /**
    * Returns the MBeanServer ID.
    *
    * @see javax.management.MBeanServerFactory#findMBeanServer
    */
   public String getMBeanServerId();

   /**
    * Returns the JMX specification name, the string 'Java Management Extensions'.
    */
   public String getSpecificationName();

   /**
    * Returns the JMX specification vendor, the string 'Sun Microsystems'.
    */
   public String getSpecificationVendor();

   /**
    * Returns the JMX specification version, for example '1.2'.
    */
   public String getSpecificationVersion();
}

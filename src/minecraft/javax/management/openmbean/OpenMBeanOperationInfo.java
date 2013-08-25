/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import javax.management.MBeanParameterInfo;

/**
 * Describes operation of an OpenMBean
 *
 * @version $Revision: 1.6 $
 */
public interface OpenMBeanOperationInfo
{

   /**
    * Returs a human readable description about this operation.
    *
    * @return String The human readable operation description
    */
   public String getDescription();

   /**
    * Returns the name of the operation being described by this
    * <code>OpenMBeanOperationInfo</code>
    *
    * @return String the Operation name
    */
   public String getName();


   /**
    * Returns an array of <code>MBeanParameterInfo</code> for the operation
    *
    * @param MBeanParameterInfo An array of Parameterinfo
    */
   public MBeanParameterInfo[] getSignature();

   /**
    * Returns a constant which qualifies the impact of the operation
    * being described by the <code>OpenMBeanOperationInfo</code>.
    * <p/>
    * <p/>
    * The return constant is one of the ff:
    * </p>
    * <p/>
    * <ul>
    * <li>{@link javax.management.MBeanOperationInfo#INFO MBeanOperationInfo.INFO}</li>
    * <li>{@link javax.management.MBeanOperationInfo#ACTION MBeanOperationInfo.ACTION}</li>
    * <li>{@link javax.management.MBeanOperationInfo#ACTION_INFO MBeanOperationInfo.ACTION_INFO}</li>
    * </ul>
    * </p>
    *
    * @return int The operation impact
    */
   public int getImpact();


   /**
    * Return the fully qualified class name of the values being
    * returned by this operation.
    * Note that getReturnType and getReturnOpenType.getClassName
    * should be equal.
    *
    * @return String The fully qualified classname of the return type
    */
   public String getReturnType();


   /**
    * Return the OpenType of the values returned by this <code>OpenMBeanOperationInfo</code>
    *
    * @return OpenType The OpenType object
    */
   public OpenType getReturnOpenType(); // open MBean specific method

   /**
    * Test the specified object for equality.
    * <p/>
    * <p/>
    * This method will return  true if and only if the following
    * conditions are true:
    * </p>
    * <p/>
    * <ul>
    * <li>obj is not null</li>
    * <li>obj also implements OpenMBeanOperationInfo</li>
    * <li>their names are equal</li>
    * <li>their signatures are equal</li>
    * <li>their return opentypes are equal</li>
    * <li>their impacts are equal</li>
    * </ul>
    * <p/>
    * </p>
    *
    * @param obj The object being compared to
    * @return boolean
    */
   public boolean equals(Object obj);


   /**
    * Returns the hashcode of this <code>OpenMBeanOperationInfo</code>
    *
    * @return int The hashcode
    */
   public int hashCode();


   /**
    * Return a String representation
    */
   public String toString();
}

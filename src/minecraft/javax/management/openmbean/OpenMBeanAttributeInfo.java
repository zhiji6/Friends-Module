/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

/**
 * An <code>OpenMBeanAttributeInfo</code> represents an attribute of
 * an <code>OpenMBean</code>
 *
 * @version $Revision: 1.4 $
 * @see OpenMBeanAttributeInfoSupport
 */
public interface OpenMBeanAttributeInfo extends OpenMBeanParameterInfo
{

   /**
    * Returns true if the attribute is readable, false in not.
    *
    * @return boolean true of readable
    */
   public boolean isReadable();

   /**
    * Returns true if the attribute is writable, false in not.
    *
    * @return boolean true of writable
    */
   public boolean isWritable();

   /**
    * Returns true if the attribute described is accessed through a isXXX
    * getter
    * <p/>
    * <p/>
    * Note: applies only to boolean and Boolean values
    * </p>
    *
    * @return boolean true if accessed through a isXXX
    */
   public boolean isIs();

   /**
    * Compares the give <code>Object</code> for equality with this instance.
    * <p/>
    * <p/>
    * The operation returns true if and only if the following statements
    * are all true:
    * <ul>
    * <li>obj is not null</li>
    * <li>obj also implements OpenMBeanAttributeInfo</li>
    * <li>their names are equals</li>
    * <li>their open types are equal</li>
    * <li>access properties (isReadable, isWritable, isIs) are equal</li>
    * <li>default,min,max and legal values are equal</li>
    * </ul>
    *
    * @return boolean true if the above conditions are met
    */
   public boolean equals(Object obj);

   /**
    * Computes the hashCode of this <code>OpenMBeanAttributeInfo</code>
    *
    * @return int The hashCode value
    */
   public int hashCode();

   /**
    * Returns a string representation of this <code>OpenMBeanAttributeInfo</code> instance.
    *
    * @return String The representation as string
    */
   public String toString();
}

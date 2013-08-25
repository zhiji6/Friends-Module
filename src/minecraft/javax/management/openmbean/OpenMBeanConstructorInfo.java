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
 * @version $Revision: 1.3 $
 */
public interface OpenMBeanConstructorInfo
{
   public String getDescription();

   public String getName();

   public MBeanParameterInfo[] getSignature();

   public boolean equals(Object obj);

   public int hashCode();

   public String toString();
}

/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import java.util.Set;

/**
 * @version $Revision: 1.3 $
 */
public interface OpenMBeanParameterInfo
{
   public String getDescription();

   public String getName();

   public OpenType getOpenType();

   public Object getDefaultValue();

   public Set getLegalValues();

   public Comparable getMinValue();

   public Comparable getMaxValue();

   public boolean hasDefaultValue();

   public boolean hasLegalValues();

   public boolean hasMinValue();

   public boolean hasMaxValue();

   public boolean isValue(Object obj);

   public boolean equals(Object obj);

   public int hashCode();

   public String toString();
}

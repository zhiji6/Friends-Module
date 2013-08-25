/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import java.util.Collection;

/**
 * @version $Revision: 1.6 $
 */
public interface CompositeData
{
   public boolean containsKey(String key);

   public boolean containsValue(Object value);

   public boolean equals(Object object);

   public Object get(String key);

   public Object[] getAll(String[] keys);

   public CompositeType getCompositeType();

   public int hashCode();

   public String toString();

   public Collection values();
}

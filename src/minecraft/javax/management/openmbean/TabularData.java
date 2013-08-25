/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.openmbean;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @version $Revision: 1.7 $
 */

/**
 * The TabularData interface specifies the behaviour of a specific type of complex open data objects which represent tabular data structures
 */
public interface TabularData
{
   /**
    * <p>Calculates the index that would be used in this TabularData instance to refer to the specified CompositeData value parameter, if it were added.
    * This method checks for the type validity of the specified value, but does not check if the calculated index is already used to refer to a value in this TabularData instance
    *
    * @param index the CompositeData value whose index in this TabularData instance is to be calculated. It must be of the same composite type as this instances' rowType and cannot be null.
    * @return object[] value that the specified value would have in this TabulatData instance
    * @throws NullPointerException     if index is null
    * @throws InvalidOpenTypeException if index does not conform to this TabularData instance's rowType
    */
   public Object[] calculateIndex(CompositeData index);

   public void clear();

   public boolean containsKey(Object[] key);

   public boolean containsValue(CompositeData value);

   public boolean equals(Object object);

   public CompositeData get(Object[] key);

   public TabularType getTabularType();

   public int hashCode();

   public boolean isEmpty();

   public Set keySet();

   public void put(CompositeData value);

   public void putAll(CompositeData[] values);

   public CompositeData remove(Object[] key);

   public int size();

   public String toString();

   public Collection values();
}

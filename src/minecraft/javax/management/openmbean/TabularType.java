/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.openmbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version $Revision: 1.17 $
 */
public class TabularType extends OpenType implements Serializable
{
   private static final long serialVersionUID = 6554071860220659261L;

   private CompositeType rowType = null;
   private List indexNames = null;

   private transient int m_hashcode = 0;
   private transient String m_classDescription = null;

   public TabularType(String typeName, String description, CompositeType rowType, String[] indexNames) throws OpenDataException
   {
      super(TabularData.class.getName(), typeName, description);
      if (typeName.trim().length() == 0) throw new IllegalArgumentException("TabularType name can't be empty");
      if (description.trim().length() == 0) throw new IllegalArgumentException("TabularType description can't be empty");
      validate(rowType, indexNames);
      this.rowType = rowType;
      ArrayList temp = new ArrayList();
      for (int i = 0; i < indexNames.length; ++i) temp.add(indexNames[i]);
      this.indexNames = Collections.unmodifiableList(temp);
   }

   /**
    * Checks if all of the values of indexNames match items defined in rowType, that the item is not null or zero length
    * we then create an unmodifiable list from the "valid" string array.
    * If any validity checks fail an OpenDataException is thrown
    */
   private void validate(CompositeType rowType, String[] indexNames) throws OpenDataException
   {
      if (rowType == null) throw new IllegalArgumentException("The CompositeType passed in cannot be null");
      if (indexNames == null || indexNames.length == 0) throw new IllegalArgumentException("The String[] indexNames cannot be null or empty");

      for (int i = 0; i < indexNames.length; i++)
      {
         String item = indexNames[i];
         if (item == null || item.length() == 0)
         {
            throw new IllegalArgumentException("An Item in the indexNames[] cannot be null or of zero length");
         }
         if (!(rowType.containsKey(item)))
         {
            throw new OpenDataException("Element value: " + indexNames[i] + " at index: " + i + " is not a valid item name for RowType");
         }
      }
   }

   public CompositeType getRowType()
   {
      return rowType;
   }

   public List getIndexNames()
   {
      return indexNames;
   }

   public boolean isValue(Object object)
   {
      if (!(object instanceof TabularData)) return false;
      TabularData tabularData = (TabularData)object;
      return equals(tabularData.getTabularType());
   }

   public boolean equals(Object object)
   {
      if (object == this) return true;
      if (!(object instanceof TabularType)) return false;
      TabularType tabularType = (TabularType)object;
      return getRowType().equals(tabularType.getRowType()) &&
             getIndexNames().equals(tabularType.getIndexNames()) &&
             getTypeName().equals(tabularType.getTypeName());
   }

   public int hashCode()
   {
      if (m_hashcode == 0)
      {
         int result = getTypeName().hashCode();
         result += getRowType().hashCode();
         List names = getIndexNames();
         for (int i = 0; i < names.size(); ++i)
         {
            Object name = names.get(i);
            result += name.hashCode();
         }
         m_hashcode = result;
      }
      return m_hashcode;
   }

   public String toString()
   {
      if (m_classDescription == null)
      {
         StringBuffer classString = new StringBuffer("TabularType name: ").append(getTypeName());
         classString.append(" rowType: ").append(getRowType());
         classString.append("indexNames: ").append(getIndexNames());
         m_classDescription = classString.toString();
      }
      return m_classDescription;
   }
}

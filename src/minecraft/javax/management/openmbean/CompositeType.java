/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.openmbean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @version $Revision: 1.11 $
 */
public class CompositeType extends OpenType implements Serializable
{
   private static final long serialVersionUID = -5366242454346948798L;

   private TreeMap nameToDescription;
   private TreeMap nameToType;

   private transient String m_classStringValue = null;
   private transient int m_hashcode = 0;

   /**
    * The CompositeType class is the open type class whose instances describe the types of CompositeData values
    * The Java class name of composite data values this composite type represents (ie the class name returned by
    * the getClassName method) is set to the string value returned by CompositeData.class.getName().
    * <p><b>Parameters:<</p>
    * typeName - The name given to the composite type this instance represents; cannot be a null or empty string
    * <p/>
    * description - The human readable description of the composite type this instance represents; cannot be a null or empty string
    * <p/>
    * itemNames - The n/b>ames of the items contained in the composite data values described by this composite type instance, cannot be null, and should contain at
    * least one element, no element can be null, or an empty string.
    * NOTE: the order in which the item names are given is not important to differentiate one CompositeType from another. The item names are stored internally sorted in
    * ascending alphanumeric order
    * <p/>
    * itemDescriptions - the descriptions in the same order as the itemNames, same size as itemNames, with no item null or an empty String.
    * <p/>
    * itemTypes - The openType instances, in the same order as itemNames, describing the items contained in the compositeData values described by this instance.
    * Should be the same size as itemNames and no element can be null.
    * <p/>
    * <p><b>Throws:</b></p>
    * IllegalArgumentException - If typeName or description is a null or empty string, or itemNames or itemDescriptions or itemTypes is null, or any element of
    * itemNames or itemDescriptions is a null or empty string, or any element of itemTypes is null, or itemNames or itemDescriptions or itemTypes are not of the same size.
    * <p/>
    * OpenDataException - If itemNames contains duplicate item names (case sensitive, but leading and trailing whitespaces removed).
    */
   public CompositeType(String typeName, String description, String[] itemNames, String[] itemDescriptions, OpenType[] itemTypes) throws OpenDataException
   {
      super(CompositeData.class.getName(), typeName, description);
      validate(typeName, description, itemNames, itemDescriptions, itemTypes);
      initialize(itemNames, itemDescriptions, itemTypes);
   }

   /**
    * Initialize treeMaps and store the data required: itemName (key) -> itemDescription (value) into nameDescription map, itemName (key) -> itemType (value) into nameType map
    */
   private void initialize(String[] itemNames, String[] itemDescriptions, OpenType[] itemTypes) throws OpenDataException
   {
      m_hashcode = computeHashCode(getTypeName(), itemNames, itemTypes);
      nameToDescription = new TreeMap();
      nameToType = new TreeMap();
      for (int i = 0; i < itemNames.length; i++)
      {
         String item = itemNames[i].trim();
         if (nameToDescription.containsKey(item)) throw new OpenDataException("The key: " + item + " is already mapped to a previous entry");
         nameToDescription.put(item, itemDescriptions[i]);
         nameToType.put(item, itemTypes[i]);
      }
   }

   /**
    * validate strings for zero length and null and itemTypes for null, we know before we get here that all the arrays are of equal length tested in constructor
    * so just get the respective objects at the index
    */
   private void validate(String typeName, String description, String[] itemNames, String[] itemDescriptions, OpenType[] itemTypes) throws IllegalArgumentException
   {
      if (typeName == null || typeName.length() == 0) throw new IllegalArgumentException("typeName can't be null or empty");
      if (description == null || description.length() == 0) throw new IllegalArgumentException("description can't be null or empty");
      if (itemNames == null || itemNames.length == 0) throw new IllegalArgumentException("The String[] of itemNames cannot be null or empty");
      if (itemDescriptions == null || itemDescriptions.length == 0) throw new IllegalArgumentException("The String[] of itemDescriptions cannot be null or empty");
      if (itemTypes == null || itemTypes.length == 0) throw new IllegalArgumentException("The OpenType[] of itemTypes cannot be null or empty");
      if (itemDescriptions.length != itemNames.length || itemTypes.length != itemNames.length) throw new IllegalArgumentException("itemNames, itemDescriptions and itemTypes must all be the same length");
      for (int i = 0; i < itemNames.length; i++)
      {
         String value = itemNames[i];
         String d = itemDescriptions[i];
         if (value == null) throw new IllegalArgumentException("The itemName at index: " + i + " cannot be a null value");
         if (d == null) throw new IllegalArgumentException("The itemDescription at index: " + i + " cannot be a null value");
         if (value.trim().equals("")) throw new IllegalArgumentException("The itemName at index: " + i + " cannot be an empty string");
         if (d.trim().equals("")) throw new IllegalArgumentException("The itemDescription at index: " + i + " cannot be an empty string");
         if (itemTypes[i] == null) throw new IllegalArgumentException("The OpenType at index: " + i + " cannot be a null value");
      }
   }

   /**
    * check if the key itemName is present
    *
    * @param itemName the name of the key to look for
    * @return true if the key is present, false otherwise
    */
   public boolean containsKey(String itemName)
   {
      if (itemName == null || itemName.length() == 0) return false;
      /** pick any treeMap as both Maps map the same values as keys */
      return nameToDescription.containsKey(itemName);
   }

   /**
    * Retrieve the description value for the given key
    *
    * @param itemName the key
    * @return the corresponding value
    */
   public String getDescription(String itemName)
   {
      if (itemName == null || itemName.length() == 0) return null;
      return (String)nameToDescription.get(itemName);
   }

   /**
    * Retrieve the OpenType for the given key
    *
    * @param itemName the key for which to fetch the openType value
    * @return OpenType or null if there is no value for the given key, or no matching key
    */
   public OpenType getType(String itemName)
   {
      if (itemName == null || itemName.length() == 0) return null;
      return (OpenType)nameToType.get(itemName);
   }

   /**
    * Retrieve an unmodifiable set of keys
    *
    * @return a Set of the keys
    */
   public Set keySet()
   {
      return Collections.unmodifiableSet(nameToDescription.keySet());
   }

   /**
    * Test whether object is a value which could be described by this CompositeType instance.
    *
    * @param object the Object to test if is a value which can be described by this CompositeType instance
    * @return true if object is a value which can be described by this CompositeType instance, false otherwise.
    */
   public boolean isValue(Object object)
   {
      if (!(object instanceof CompositeData)) return false;
      CompositeData compositeData = (CompositeData)object;
      return equals(compositeData.getCompositeType());
   }

   /**
    * tests object passed in is equal to the CompositeType instance
    *
    * @param object the Object to test if it is equal to this CompositeType instance
    * @return true if the objects are equal as tested by taking the most significant fields and testing they are equal
    */
   public boolean equals(Object object)
   {
      /** see if we can prevent a fairly expensive equals operation later where we compare the treeMaps */
      if (object == this) return true;

      /** the above test failed lets see if we have an instanceof at least */
      if (!(object instanceof CompositeType)) return false;
      CompositeType type = (CompositeType)object;
      if (!(getTypeName().equals(type.getTypeName()))) return false;
      return (nameToType.equals(type.nameToType));
   }

   /**
    * @return the calculated hashcode
    */
   public int hashCode()
   {
      return m_hashcode;
   }

   /**
    * @return human readable representation of this class
    */
   public String toString()
   {
      if (m_classStringValue == null)
      {
         StringBuffer value = new StringBuffer(100);
         value.append(getClassName()).append(" TypeName: ").append(getTypeName()).append(" contains data:\n");
         for (Iterator i = nameToType.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry)i.next();
            value.append("ItemName: ").append((String)entry.getKey()).append(" OpenType value: ").append(((OpenType)entry.getValue()).toString()).append("\n");
         }
         m_classStringValue = value.toString();
      }
      return m_classStringValue;
   }

   /**
    * obtain the values from the TreeMaps and validate
    */
   private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException
   {
      inputStream.defaultReadObject();
      String[] itemNames = (String[])nameToDescription.keySet().toArray(new String[nameToDescription.size()]);
      String[] itemDescriptions = (String[])nameToDescription.values().toArray(new String[nameToDescription.size()]);
      OpenType[] itemTypes = (OpenType[])nameToType.values().toArray(new OpenType[nameToType.size()]);
      try
      {
         validate(getTypeName(), getDescription(), itemNames, itemDescriptions, itemTypes);
         initialize(itemNames, itemDescriptions, itemTypes);
      }
      catch (OpenDataException e)
      {
         throw new StreamCorruptedException("validation failed for deserialized object, unable to create object in the correct state");
      }
   }

   private int computeHashCode(String name, String[] itemnames, OpenType[] itemtypes)
   {
      int result = name.hashCode();
      for (int i = 0; i < itemnames.length; i++)
      {
         result += itemnames[i].hashCode();
         result += itemtypes[i].hashCode();
      }
      return result;
   }

}

/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;

/**
 * @version $Revision: 1.8 $
 */
public class Attribute implements Serializable
{
   private static final long serialVersionUID = 2484220110589082382L;

   /**
    * @serial The attribute's name
    */
   private final String name;
   /**
    * @serial The attribute's value
    */
   private final Object value;

   private transient int hash;

   public Attribute(String name, Object value)
   {
      if (name == null) throw new RuntimeOperationsException(new IllegalArgumentException("The name of an attribute cannot be null"));

      this.name = name;
      this.value = value;
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj == this) return true;

      try
      {
         Attribute other = (Attribute)obj;
         boolean namesEqual = name.equals(other.name);
         boolean valuesEqual = false;
         if (value == null)
            valuesEqual = other.value == null;
         else
            valuesEqual = value.equals(other.value);
         return namesEqual && valuesEqual;
      }
      catch (ClassCastException ignored)
      {
      }
      return false;
   }

   public int hashCode()
   {
      if (hash == 0) hash = computeHash();
      return hash;
   }

   public String getName()
   {
      return name;
   }

   public Object getValue()
   {
      return value;
   }

   public String toString()
   {
      return new StringBuffer("Attribute's name: ").append(getName()).append(", value: ").append(getValue()).toString();
   }

   private int computeHash()
   {
      int hash = name.hashCode();
      if (value != null) hash ^= value.hashCode();
      return hash;
   }
}

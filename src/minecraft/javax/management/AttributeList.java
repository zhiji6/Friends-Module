/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @version $Revision: 1.8 $
 */
public class AttributeList extends ArrayList
{
   private static final long serialVersionUID = -4077085769279709076L;

   public AttributeList()
   {
   }

   public AttributeList(int initialCapacity)
   {
      super(initialCapacity);
   }

   public AttributeList(AttributeList list)
   {
      super(list);
   }

   public boolean add(Object o)
   {
      if (o instanceof Attribute)
         return super.add(o);
      else
         throw new RuntimeOperationsException(new IllegalArgumentException("Elements of AttributeList can only be Attribute objects"));
   }

   public void add(Attribute a)
   {
      add((Object)a);
   }

   public void add(int index, Object element)
   {
      if (element instanceof Attribute)
      {
         try
         {
            super.add(index, element);
         }
         catch (IndexOutOfBoundsException x)
         {
            throw new RuntimeOperationsException(x);
         }
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Elements of AttributeList can only be Attribute objects"));
      }
   }

   public void add(int index, Attribute element)
   {
      add(index, (Object)element);
   }

   public boolean addAll(Collection c)
   {
      if (c instanceof AttributeList)
      {
         return super.addAll(c);
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Only AttributeList objects can be added to other AttributeList"));
      }
   }

   public boolean addAll(AttributeList c)
   {
      return addAll((Collection)c);
   }

   public boolean addAll(int index, Collection c)
   {
      if (c instanceof AttributeList)
      {
         try
         {
            return super.addAll(index, c);
         }
         catch (IndexOutOfBoundsException x)
         {
            throw new RuntimeOperationsException(x);
         }
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Only AttributeList objects can be added to other AttributeList"));
      }
   }

   public boolean addAll(int index, AttributeList c)
   {
      return addAll(index, (Collection)c);
   }

   public Object set(int index, Object element)
   {
      if (element instanceof Attribute)
      {
         try
         {
            return super.set(index, element);
         }
         catch (IndexOutOfBoundsException x)
         {
            throw new RuntimeOperationsException(x);
         }
      }
      else
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Elements of AttributeList can only be Attribute objects"));
      }
   }

   public void set(int index, Attribute element)
   {
      set(index, (Object)element);
   }
}

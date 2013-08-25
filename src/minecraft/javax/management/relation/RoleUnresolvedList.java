/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package javax.management.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Revision: 1.7 $
 */
public class RoleUnresolvedList extends ArrayList
{
   private static final long serialVersionUID = 4054902803091433324L;

   public RoleUnresolvedList()
   {
   }

   public RoleUnresolvedList(int initialCapacity)
   {
      super(initialCapacity);
   }

   // method accepts any list but list may only contain elements of RoleUnresolved
   public RoleUnresolvedList(List list) throws IllegalArgumentException
   {
      if (list == null)
      {
         throw new IllegalArgumentException("List cannot be null");
      }
      for (Iterator i = list.iterator(); i.hasNext();)
      {
         Object currentIteration = i.next();
         if (!(currentIteration instanceof RoleUnresolved))
         {
            throw new IllegalArgumentException("All elements in the list must be an instance of RoleUnresolved");
         }
         add((RoleUnresolved)currentIteration);
      }
   }

   public void add(RoleUnresolved roleUnresolved) throws IllegalArgumentException
   {
      if (roleUnresolved == null)
      {
         throw new IllegalArgumentException("RoleUnresolved cannot be null");
      }
      super.add(roleUnresolved);
   }

   public void add(int index, RoleUnresolved roleUnresolved) throws IllegalArgumentException, IndexOutOfBoundsException
   {
      if (roleUnresolved == null)
      {
         throw new IllegalArgumentException("RoleUnresolved cannot be null");
      }
      super.add(index, roleUnresolved);
   }

   public void set(int index, RoleUnresolved roleUnresolved) throws IllegalArgumentException, IndexOutOfBoundsException
   {
      if (roleUnresolved == null)
      {
         throw new IllegalArgumentException("RoleUnresolved cannot be null");
      }
      super.set(index, roleUnresolved);
   }

   public boolean addAll(RoleUnresolvedList roleUnresolvedList) throws IndexOutOfBoundsException
   {
      if (roleUnresolvedList == null) return true;
      return super.addAll(roleUnresolvedList);
   }

   public boolean addAll(int index, RoleUnresolvedList roleUnresolvedList) throws IllegalArgumentException, IndexOutOfBoundsException
   {
      if (roleUnresolvedList == null) return true;
      return super.addAll(index, roleUnresolvedList);
   }
}
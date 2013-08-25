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
 * @version $Revision: 1.8 $
 */
public class RoleList extends ArrayList
{
   private static final long serialVersionUID = 5568344346499649313L;

   public RoleList()
   {
   }

   public RoleList(int initialCapacity)
   {
      super(initialCapacity);
   }

   public RoleList(List list) throws IllegalArgumentException
   {
      if (list == null) throw new IllegalArgumentException("list argument must not be null");
      for (Iterator listIterator = list.iterator(); listIterator.hasNext();)
      {
         Object currentListItem = listIterator.next();
         if (!(currentListItem instanceof Role))
         {
            throw new IllegalArgumentException("Item added to the RoleList: " + currentListItem + " does not represent a Role");
         }
         add((Role)currentListItem);
      }
   }

   public void add(Role role) throws IllegalArgumentException
   {
      if (role == null)
      {
         throw new IllegalArgumentException("A role cannot be null");
      }
      super.add(role);
   }

   public void add(int index, Role role) throws IllegalArgumentException, IndexOutOfBoundsException
   {
      if (role == null)
      {
         throw new IllegalArgumentException("Cannot have a null role value");
      }
      super.add(index, role);
   }

   public void set(int index, Role role) throws IllegalArgumentException, IndexOutOfBoundsException
   {
      if (role == null)
      {
         throw new IllegalArgumentException("Cannot have a null role");
      }
      super.set(index, role);
   }

   public boolean addAll(RoleList roleList) throws IndexOutOfBoundsException
   {
      if (roleList == null)
      {
         return true;
      }
      return super.addAll(roleList);
   }

   public boolean addAll(int index, RoleList roleList) throws IllegalArgumentException, IndexOutOfBoundsException
   {
      if (roleList == null)
      {
         return true;
      }
      return super.addAll(index, roleList);
   }
}
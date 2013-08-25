/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Use to gain access to the {@link Descriptor} objects associated with MBean metadata.
 *
 * @version $Revision: 1.6 $
 */
public interface DescriptorAccess
{
   /**
    * Returns a copy of the descriptor
    */
   public Descriptor getDescriptor();

   /**
    * Sets the descriptor with a copy of the given descriptor.
    */
   public void setDescriptor(Descriptor descriptor);
}

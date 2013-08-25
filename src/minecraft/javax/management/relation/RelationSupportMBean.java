/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.relation;

/**
 * @version $Revision: 1.4 $
 */
public interface RelationSupportMBean extends Relation
{
   public Boolean isInRelationService();

   public void setRelationServiceManagementFlag(Boolean flag) throws IllegalArgumentException;
}
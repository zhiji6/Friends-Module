/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Default implementation for the MBeanDescription interface.
 *
 * @version $Revision: 1.5 $
 */
public class MBeanDescriptionAdapter implements MBeanDescription
{
   public String getMBeanDescription()
   {
      return "Manageable Bean";
   }

   public String getConstructorDescription(Constructor ctor)
   {
      return "Constructor exposed for management";
   }

   public String getConstructorParameterName(Constructor ctor, int index)
   {
      switch (index)
      {
         case 0:
            return "param1";
         case 1:
            return "param2";
         case 2:
            return "param3";
         case 3:
            return "param4";
         default:
            return "param" + (index + 1);
      }
   }

   public String getConstructorParameterDescription(Constructor ctor, int index)
   {
      switch (index)
      {
         case 0:
            return "Constructor's parameter n. 1";
         case 1:
            return "Constructor's parameter n. 2";
         case 2:
            return "Constructor's parameter n. 3";
         case 3:
            return "Constructor's parameter n. 4";
         default:
            return "Constructor's parameter n. " + (index + 1);
      }
   }

   public String getAttributeDescription(String attribute)
   {
      return "Attribute exposed for management";
   }

   public String getOperationDescription(Method operation)
   {
      return "Operation exposed for management";
   }

   public String getOperationParameterName(Method method, int index)
   {
      switch (index)
      {
         case 0:
            return "param1";
         case 1:
            return "param2";
         case 2:
            return "param3";
         case 3:
            return "param4";
         default:
            return "param" + (index + 1);
      }
   }

   public String getOperationParameterDescription(Method method, int index)
   {
      switch (index)
      {
         case 0:
            return "Operation's parameter n. 1";
         case 1:
            return "Operation's parameter n. 2";
         case 2:
            return "Operation's parameter n. 3";
         case 3:
            return "Operation's parameter n. 4";
         default:
            return "Operation's parameter n. " + (index + 1);
      }
   }
}

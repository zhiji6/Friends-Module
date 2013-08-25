/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Factory class for constructing query expressions.
 *
 * @version $Revision: 1.9 $
 */
public class Query
{
   public static final int GT = 0;
   public static final int LT = 1;
   public static final int GE = 2;
   public static final int LE = 3;
   public static final int EQ = 4;

   public static final int PLUS = 0;
   public static final int MINUS = 1;
   public static final int TIMES = 2;
   public static final int DIV = 3;

   /**
    * Returns a query expression for the result of the NOT operation on the given expression.
    */
   public static QueryExp not(QueryExp queryExp)
   {
      return new NotQueryExp(queryExp);
   }

   /**
    * Returns a query expression for the result of the AND operation on the two given expressions.
    */
   public static QueryExp and(QueryExp q1, QueryExp q2)
   {
      return new AndQueryExp(q1, q2);
   }

   /**
    * Returns a query expression for the result of the OR operation on the two given expressions.
    */
   public static QueryExp or(QueryExp q1, QueryExp q2)
   {
      return new OrQueryExp(q1, q2);
   }

   /**
    * Returns a query expression for the result of <code>v1</code> GREATER-THAN <code>v2</code>.
    */
   public static QueryExp gt(ValueExp v1, ValueExp v2)
   {
      return new BinaryRelQueryExp(GT, v1, v2);
   }

   /**
    * Returns a query expression for the result of <code>v1</code> GREATER-THAN-OR-EQUAL <code>v2</code>.
    */
   public static QueryExp geq(ValueExp v1, ValueExp v2)
   {
      return new BinaryRelQueryExp(GE, v1, v2);
   }

   /**
    * Returns a query expression for the result of <code>v1</code> LESS-THAN-OR-EQUAL <code>v2</code>.
    */
   public static QueryExp leq(ValueExp v1, ValueExp v2)
   {
      return new BinaryRelQueryExp(LE, v1, v2);
   }

   /**
    * Returns a query expression for the result of <code>v1</code> LESS-THAN <code>v2</code>.
    */
   public static QueryExp lt(ValueExp v1, ValueExp v2)
   {
      return new BinaryRelQueryExp(LT, v1, v2);
   }

   /**
    * Returns a query expression for the result of <code>v1</code> EQUAL <code>v2</code>.
    */
   public static QueryExp eq(ValueExp v1, ValueExp v2)
   {
      return new BinaryRelQueryExp(EQ, v1, v2);
   }

   /**
    * Returns a query expression for the result of <code>v1</code> LESS-THAN-OR-EQUAL <code>v2</code> LESS-THAN-OR-EQUAL <code>v3</code>
    */
   public static QueryExp between(ValueExp v1, ValueExp v2, ValueExp v3)
   {
      return new BetweenQueryExp(v1, v2, v3);
   }

   /**
    * Returns a query expression for the result of <code>val</code> being present as one element of the given array.
    */
   public static QueryExp in(ValueExp val, ValueExp valueList[])
   {
      return new InQueryExp(val, valueList);
   }

   /**
    * Returns the expression value that represent the value of an attribute of a generic MBean.
    */
   public static AttributeValueExp attr(String name)
   {
      return new AttributeValueExp(name);
   }

   /**
    * Returns the expression value that represent the value of an attribute of an MBean of the specified class.
    */
   public static AttributeValueExp attr(String className, String name)
   {
      return new QualifiedAttributeValueExp(className, name);
   }

   /**
    * Returns the expression value that represent the class name of an MBean.
    */
   public static AttributeValueExp classattr()
   {
      return new ClassAttributeValueExp();
   }

   /**
    * Returns the expression value that represent the given string.
    */
   public static StringValueExp value(String val)
   {
      return new StringValueExp(val);
   }

   /**
    * Returns the expression value that represent the given number.
    */
   public static ValueExp value(Number val)
   {
      return new NumericValueExp(val);
   }

   /**
    * Returns the expression value that represent the given number.
    */
   public static ValueExp value(int val)
   {
      return value(new Integer(val));
   }

   /**
    * Returns the expression value that represent the given number.
    */
   public static ValueExp value(long val)
   {
      return value(new Long(val));
   }

   /**
    * Returns the expression value that represent the given number.
    */
   public static ValueExp value(float val)
   {
      return value(new Float(val));
   }

   /**
    * Returns the expression value that represent the given number.
    */
   public static ValueExp value(double val)
   {
      return value(new Double(val));
   }

   /**
    * Returns the expression value that represent the given boolean.
    */
   public static ValueExp value(boolean val)
   {
      return new BooleanValueExp(val);
   }

   /**
    * Returns a expression value for the result of <code>value1</code> PLUS <code>value2</code>
    */
   public static ValueExp plus(ValueExp value1, ValueExp value2)
   {
      return new BinaryOpValueExp(PLUS, value1, value2);
   }

   /**
    * Returns a expression value for the result of <code>value1</code> MINUS <code>value2</code>
    */
   public static ValueExp minus(ValueExp value1, ValueExp value2)
   {
      return new BinaryOpValueExp(MINUS, value1, value2);
   }

   /**
    * Returns a expression value for the result of <code>value1</code> TIMES <code>value2</code>
    */
   public static ValueExp times(ValueExp value1, ValueExp value2)
   {
      return new BinaryOpValueExp(TIMES, value1, value2);
   }

   /**
    * Returns a expression value for the result of <code>value1</code> DIVIDED <code>value2</code>
    */
   public static ValueExp div(ValueExp value1, ValueExp value2)
   {
      return new BinaryOpValueExp(DIV, value1, value2);
   }

   /**
    * Returns a query expression for the result of the wildcard match between the given attribute value and the string pattern.
    */
   public static QueryExp match(AttributeValueExp a, StringValueExp s)
   {
      return new MatchQueryExp(a, s);
   }

   /**
    * Returns a query expression for the result of the match, as initial string, between the given attribute value and the string pattern.
    */
   public static QueryExp initialSubString(AttributeValueExp a, StringValueExp s)
   {
      return new MatchQueryExp(a, new StringValueExp(s.getValue() + "*"));
   }

   /**
    * Returns a query expression for the result of the match, as contained string, between the given attribute value and the string pattern.
    */
   public static QueryExp anySubString(AttributeValueExp a, StringValueExp s)
   {
      return new MatchQueryExp(a, new StringValueExp("*" + s.getValue() + "*"));
   }

   /**
    * Returns a query expression for the result of the match, as final string, between the given attribute value and the string pattern.
    */
   public static QueryExp finalSubString(AttributeValueExp a, StringValueExp s)
   {
      return new MatchQueryExp(a, new StringValueExp("*" + s.getValue()));
   }
}

/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * @version $Revision: 1.7 $
 * @serial include
 */
class MatchQueryExp extends QueryEval implements QueryExp
{
   private static final long serialVersionUID = -7156603696948215014L;

   /**
    * @serial The value to match
    */
   private final AttributeValueExp exp;
   /**
    * @serial The matching pattern
    */
   private final String pattern;

   MatchQueryExp(AttributeValueExp exp, StringValueExp pattern)
   {
      this.exp = exp;
      this.pattern = pattern == null ? null : pattern.getValue();
   }

   public void setMBeanServer(MBeanServer server)
   {
      super.setMBeanServer(server);
      if (exp != null) exp.setMBeanServer(server);
   }

   public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      ValueExp value = exp.apply(name);
      if (value instanceof StringValueExp)
      {
         return wildcardMatch(((StringValueExp)value).getValue(), pattern);
      }
      return false;
   }

   /**
    * Tests whether string s is matched by pattern p.
    * Supports "?", "*", "[", each of which may be escaped with "\";
    * Character classes may use "!" for negation and "-" for range.
    * Not yet supported: internationalization; "\" inside brackets.<P>
    * Wildcard matching routine by Karl Heuer.  Public Domain.<P>
    */
   private boolean wildcardMatch(String s, String p)
   {
      if (s == null && p == null) return true;
      if (s == null) return false;
      if (p == null) return true;

      char c;
      int si = 0, pi = 0;
      int slen = s.length();
      int plen = p.length();

      while (pi < plen) // While still string
      {
         c = p.charAt(pi++);
         if (c == '?')
         {
            if (++si > slen) return false;
         }
         else if (c == '[') // Start of choice
         {
            boolean wantit = true;
            boolean seenit = false;
            if (p.charAt(pi) == '!')
            {
               wantit = false;
               ++pi;
            }
            while (++pi < plen && (c = p.charAt(pi)) != ']')
            {
               if (p.charAt(pi) == '-' && pi + 1 < plen)
               {
                  if (s.charAt(si) >= c && s.charAt(si) <= p.charAt(pi + 1))
                  {
                     seenit = true;
                  }
                  pi += 1;
               }
               else
               {
                  if (c == s.charAt(si))
                  {
                     seenit = true;
                  }
               }
            }

            if ((pi >= plen) || (wantit != seenit)) return false;

            ++pi;
            ++si;
         }
         else if (c == '*') // Wildcard
         {
            if (pi >= plen) return true;

            do
            {
               if (wildcardMatch(s.substring(si), p.substring(pi))) return true;
            }
            while (++si < slen);
            return false;
         }
         else if (c == '\\')
         {
            if (pi >= plen || p.charAt(pi++) != s.charAt(si++)) return false;
         }
         else
         {
            if (si >= slen || c != s.charAt(si++)) return false;
         }
      }
      return (si == slen);
   }
}

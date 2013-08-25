/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

import mx4j.loading.ClassLoaderObjectInputStream;

/**
 * A persister object that stores to files.
 *
 * @version $Revision: 1.10 $
 */
public class FilePersister extends Persister
{
   private File m_store;

   /**
    * Creates a new FilePersister.
    *
    * @param location the directory where the file will be written (must already exist);
    *                 if null the name is used as a location
    * @param name     the file name used to store information
    */
   public FilePersister(String location, String name) throws MBeanException
   {
      if (name == null)
      {
         throw new MBeanException(new IllegalArgumentException("Persist name cannot be null"));
      }

      if (location != null)
      {
         File dir = new File(location);
         if (!dir.exists())
         {
            throw new MBeanException(new FileNotFoundException(location));
         }
         m_store = new File(dir, name);
      }
      else
      {
         m_store = new File(name);
      }
   }

   /**
    * Returns the path where the information is stored.
    */
   public String getFileName()
   {
      return m_store.getAbsolutePath();
   }

   public Object load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      FileInputStream fin = null;
      ObjectInputStream clois = null;
      Object result = null;

      synchronized (this)
      {
         try
         {
            // Create the inputStream
            fin = new FileInputStream(m_store);

            // Use the ClassLoaderObjectInputStream
            clois = new ClassLoaderObjectInputStream(fin, Thread.currentThread().getContextClassLoader());
            try
            {
               // Try load the object using the ContextClassLoader
               result = clois.readObject();
            }
            catch (ClassNotFoundException ex)
            {
               // Close previous streams, FileInputStream does not support reset(),
               // so I have to create a new one
               try
               {
                  clois.close();
               }
               catch (IOException ignored)
               {
               }

               throw new MBeanException(ex);
/*
					// Try using the DefaultLoaderRepository
					fin = new FileInputStream(m_store);
					clois = new ClassLoaderObjectInputStream(fin);
					try
					{
						result = clois.readObject();
					}
					catch(ClassNotFoundException e)
					{
						throw new MBeanException(e);
					}
*/
            }
         }
         catch (IOException ex)
         {
            throw new MBeanException(ex);
         }
         finally
         {
            try
            {
               if (clois != null) clois.close();
            }
            catch (IOException ignored)
            {
            }
         }
      }
      return result;
   }

   public void store(Object data) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      if (data == null) throw new RuntimeOperationsException(new IllegalArgumentException("Cannot store a null object."));
      if (!(data instanceof Serializable)) throw new MBeanException(new NotSerializableException(data.getClass().getName() + " must implement java.io.Serializable"));

      FileOutputStream fos = null;
      ObjectOutputStream oos = null;
      synchronized (this)
      {
         try
         {
            fos = new FileOutputStream(m_store);
            oos = new ObjectOutputStream(fos);
         }
         catch (IOException ex)
         {
            throw new MBeanException(ex);
         }
         try
         {
            //write out the data
            oos.writeObject(data);
            // flush the stream
            oos.flush();
         }
         catch (IOException ex)
         {
            throw new MBeanException(ex);
         }
         finally
         {
            try
            {
               oos.close();
            }
            catch (IOException ex)
            {/* unable to close the stream nothing to do*/
            }
         }
      }
   }
}

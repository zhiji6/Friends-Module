/*
Ziah_'s Client
Copyright (C) 2013  Ziah Jyothi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see [http://www.gnu.org/licenses/].
*/

package com.oneofthesevenbillion.ziah.ZiahsClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.oneofthesevenbillion.ziah.ZiahsClient.eu.infomas.annotation.AnnotationDetector;
import com.oneofthesevenbillion.ziah.ZiahsClient.eu.infomas.annotation.AnnotationDetector.TypeReporter;
import com.oneofthesevenbillion.ziah.ZiahsClient.util.ArrayUtils;

public class ModuleManager {
    private static ModuleManager instance;
    private Map<Module, Class> modules = new HashMap<Module, Class>();
    private Map<Module, Class> loadedModules = new HashMap<Module, Class>();
    private Map<Class, Object> moduleInstances = new HashMap<Class, Object>();
    private LinkedList<Object> moduleOrder = new LinkedList<Object>();
    private Map<Module, List<String>> unsatisfiedModulesAdd = new HashMap<Module, List<String>>();
    private Map<Module, List<String>> unsatisfiedModulesRemove = new HashMap<Module, List<String>>();
    private Map<Class, Module> disabledModules = new HashMap<Class, Module>();
    private ClassLoader modClassLoader;
    private final List<File> modFiles = new ArrayList<File>();
    private final TypeReporter reporter = new TypeReporter() {
        @Override
        public Class<? extends Annotation>[] annotations() {
            return new Class[] {Module.class};
        }

        @Override
        public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
            try {
                Class modClass = ModuleManager.this.modClassLoader.loadClass(className);
                Module modAnnotation = (Module) modClass.getAnnotation(Module.class);
                if (modAnnotation == null || modClass == null) return;
                if (!ZiahsClient.getInstance().getConfig().getData().getProperty("disabledModules." + modClass.getSimpleName(), "false").equalsIgnoreCase("true")) {
                    ModuleManager.this.modules.put(modAnnotation, modClass);
                }else{
                    ModuleManager.this.disabledModules.put(modClass, modAnnotation);
                }
            } catch (ClassNotFoundException e) {
                ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when loading module class", e);
                // Impossible
            }
        }
    };
    private final AnnotationDetector detector = new AnnotationDetector(this.reporter);
    private Map<String, RepoMod> moduleRepoData = new HashMap<String, RepoMod>();
    private List<String> attemptedRepoModules = new ArrayList<String>();

    public ModuleManager() {
        ModuleManager.instance = this;

        this.loadModulesFromDirectoryIntoClasspath();
        this.findModulesInClasspath();
        this.processModuleDependencies();
        this.sortModules();
        this.loadModules(this.moduleOrder);
        this.loadRepoMods();
    }

    public void loadRepoMods() {
        try {
            InputStream in = new URL("http://oneofthesevenbillion.com/ziah/zcmodule.php").openStream();
            List<Byte> data = new ArrayList<Byte>();
            while (true) {
                int curbyte = in.read();
                if (curbyte == -1) break;
                data.add((byte) curbyte);
            }
            in.close();
            String json = new String(ArrayUtils.byteArrayToPrimitive(data.toArray()));
            Gson gson = new Gson();
            Map<String, Map> repoMods = gson.fromJson(json, HashMap.class);
            for (String modId : repoMods.keySet()) {
                Map modData = repoMods.get(modId);
                this.moduleRepoData.put(modId, new RepoMod(modId, (String) modData.get("name"), (String) modData.get("version"), (String) modData.get("description"), ArrayUtils.join(((List) modData.get("requires")).toArray(), ","), ArrayUtils.join(((List) modData.get("recommends")).toArray(), ","), ArrayUtils.join(((List) modData.get("incompatible")).toArray(), ",")));
            }
        } catch (MalformedURLException e) {
            // Impossible
        } catch (UnsupportedEncodingException e) {
            // Probably Impossible
        } catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when downloading module!", e);
        }
    }

    public void loadRepoMod(String modId) {
        try {
            InputStream in = new URL("http://oneofthesevenbillion.com/ziah/zcmodule.php?module=" + modId).openStream();
            List<Byte> data = new ArrayList<Byte>();
            while (true) {
                int curbyte = in.read();
                if (curbyte == -1) break;
                data.add((byte) curbyte);
            }
            in.close();
            String json = new String(ArrayUtils.byteArrayToPrimitive(data.toArray()));
            if (json.equalsIgnoreCase("null")) return;
            Gson gson = new Gson();
            Map modData = gson.fromJson(json, Map.class);
            this.moduleRepoData.put(modId, new RepoMod(modId, (String) modData.get("name"), (String) modData.get("version"), (String) modData.get("description"), ArrayUtils.join(((List) modData.get("requires")).toArray(), ","), ArrayUtils.join(((List) modData.get("recommends")).toArray(), ","), ArrayUtils.join(((List) modData.get("incompatible")).toArray(), ",")));
        } catch (MalformedURLException e) {
            // Impossible
        } catch (UnsupportedEncodingException e) {
            // Probably Impossible
        } catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.WARNING, "Exception when downloading module!", e);
        }
    }

    public boolean isModuleUpToDate(String moduleId, String version) {
            if (!this.moduleRepoData.containsKey(moduleId) && !this.attemptedRepoModules.contains(moduleId)) {
                    this.loadRepoMod(moduleId);
                    this.attemptedRepoModules.add(moduleId);
            }
            if (this.moduleRepoData.containsKey(moduleId)) return this.moduleRepoData.get(moduleId).getVersion().equalsIgnoreCase(version);
            return false;
    }

    public void disableModule(Class modClass, boolean doConfig) {
        if (modClass == null) return;
        if (doConfig) ZiahsClient.getInstance().getConfig().getData().setProperty("disabledModules." + modClass.getSimpleName(), "true");
        try {
            ZiahsClient.getInstance().getConfig().save();
        }catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when saving config", e);
        }
        if (!this.moduleInstances.containsKey(modClass)) return;
        Object modInst = this.moduleInstances.get(modClass);
        for (Module modAnnot : this.loadedModules.keySet()) {
            if (this.loadedModules.get(modAnnot).equals(modClass)) {
                this.loadedModules.remove(modAnnot);
                this.moduleInstances.remove(modInst);
                this.moduleOrder.remove(modInst);
                this.disabledModules.put(modClass, modAnnot);
                ZiahsClient.getInstance().getEventBus().unregisterEventHandler(modClass);
                ZiahsClient.getInstance().unregisterKeys(modClass);
                try {
                    modInst.getClass().getDeclaredMethod("unload").invoke(modInst);
                } catch (NoSuchMethodException e) {
                } catch (Exception e) {
                    ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when unloading module", e);
                }
                break;
            }
        }
    }

    public void enableModule(Class modClass) {
        if (modClass == null) return;
        ZiahsClient.getInstance().getConfig().getData().setProperty("disabledModules." + modClass.getSimpleName(), "false");
        try {
            ZiahsClient.getInstance().getConfig().save();
        }catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when saving config", e);
        }
        if (!this.disabledModules.containsKey(modClass)) return;
        Module modAnnot = this.disabledModules.get(modClass);

        this.modules.put(modAnnot, modClass);
        this.disabledModules.remove(modClass);
        this.processModuleDependencies();
        if (this.moduleInstances.containsKey(modClass)) {
            this.moduleOrder.add(this.moduleInstances.get(modClass));
            LinkedList<Object> moduleList = new LinkedList<Object>();
            moduleList.add(this.moduleInstances.get(modClass));
            this.loadModules(moduleList);
        }
    }

    public void findModulesInClasspath() {
        try {
            List<File> modFileList = new ArrayList<File>(this.modFiles);
            for (File curfile : new ArrayList<File>(modFileList)) {
                if (curfile == null || !curfile.exists() || (curfile.isDirectory() && curfile.listFiles().length <= 0)) modFileList.remove(curfile);
            }
            this.getDetector().detect(modFileList.toArray(new File[modFileList.size()]));
        } catch (IOException e) {
            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when searching for modules in classpath", e);
        }
    }

    public void loadModulesFromDirectoryIntoClasspath() {
        File modDir = new File(ZiahsClient.getInstance().getDataDir(), "modules");
        final String[] fileNames = System.getProperty("java.class.path").split(File.pathSeparator);
        for (int i = 0; i < fileNames.length; ++i) {
            File file = new File(fileNames[i]);
            if (file != null && file.exists() && (!file.isDirectory() || file.listFiles().length > 0)) this.modFiles.add(file);
        }
        if (modDir.mkdirs() || (modDir.exists() && modDir.isDirectory())) {
            try {
                URLClassLoader loader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
                this.modClassLoader = loader;
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                int i = 0;
                for (File file : modDir.listFiles()) {
                    if (file != null && file.exists() && !file.isDirectory() && file.getName().endsWith(".jar")) {
                        this.modFiles.add(file);
                        try {
                            method.invoke(loader, file.toURI().toURL());
                        }catch (IOException e) {
                            // Impossible
                            e.printStackTrace();
                        }catch (IllegalArgumentException e) {
                            // Impossible
                            e.printStackTrace();
                        }catch (IllegalAccessException e) {
                            // Impossible
                            e.printStackTrace();
                        }catch (InvocationTargetException e) {
                            // What the heck does that mean?
                            e.printStackTrace();
                        }
                        i++;
                    }
                }
            } catch (NoSuchMethodException e) {
                // Impossible
            }
        }else{
            ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Unable to create/find modules directory, not loading any modules!");
        }
    }

    public void processModuleDependencies() {
        Map<Module, Class> processingModules = new HashMap<Module, Class>(this.modules);
        for (Module modAnnotation : processingModules.keySet()) {
            Class modClass = processingModules.get(modAnnotation);
            List<String> incompatibleModules = ArrayUtils.arrayToList(modAnnotation.incompatibleModules().split(","));
            List<String> needToBeRemoved = new ArrayList<String>();
            List<String> needToBeAdded = new ArrayList<String>();
            for (String mod : modAnnotation.requiredModules().split(",")) {
                if (mod.trim().length() == 0) continue;
                needToBeAdded.add(mod);
            }
            for (Module curMod : processingModules.keySet()) {
                if (needToBeAdded.contains(curMod.moduleId())) {
                    needToBeAdded.remove(curMod.moduleId());
                }

                if (incompatibleModules.contains(curMod.moduleId())) {
                    needToBeRemoved.add(curMod.moduleId());
                }
            }

            if (!needToBeRemoved.isEmpty()) {
                this.unsatisfiedModulesRemove.put(modAnnotation, needToBeRemoved);
            }

            if (!needToBeAdded.isEmpty()) {
                this.unsatisfiedModulesAdd.put(modAnnotation, needToBeAdded);
            }

            if (needToBeRemoved.size() <= 0 && needToBeAdded.size() <= 0) {
                try {
                    Object modInstance = modClass.newInstance();
                    this.moduleInstances.put(modClass, modInstance);
                    this.modules.remove(modAnnotation);
                    this.loadedModules.put(modAnnotation, modClass);
                    ZiahsClient.getInstance().getLogger().log(Level.INFO, modClass.getSimpleName() + " instantiated.");
                } catch (InstantiationException e) {
                    this.modules.remove(modAnnotation);
                } catch (IllegalAccessException e) {
                    this.modules.remove(modAnnotation);
                }
            }
            this.modules.remove(modAnnotation);
        }
    }

    public void sortModules() {
        HashMap<String, Object> mods = new HashMap<String, Object>();

        for (Object mod : this.moduleInstances.values()) {
            mods.put(mod.getClass().getSimpleName(), mod);
        }

        Collection<Module> modAnnotationList = this.getAllModules().keySet();
        LinkedList<Object> newMods = new LinkedList<Object>(this.moduleInstances.values());

        for (Module mod : modAnnotationList) {
            if (!this.loadedModules.containsKey(mod)) continue;
            Class modClass = this.loadedModules.get(mod);
            if (!this.moduleInstances.containsKey(modClass)) continue;
            Object modInst = this.moduleInstances.get(modClass);

            int earliestIndex = Integer.MAX_VALUE;
            for (String before : mod.loadAfter().split(",")) {
                if (before == null || before.trim().length() == 0) continue;
                if (!mods.containsKey(before)) continue;
                Object beforeObj = mods.get(before);
                int beforeIndex = newMods.indexOf(beforeObj);
                if (beforeIndex < 0) continue;
                earliestIndex = Math.min(earliestIndex, beforeIndex);
            }
            if (earliestIndex == Integer.MAX_VALUE) earliestIndex = 0;

            newMods.remove(modInst);
            newMods.add(earliestIndex, modInst);
        }

        this.moduleOrder.clear();
        this.moduleOrder.addAll(newMods);
    }

    public void loadModules(LinkedList<Object> moduleOrder) {
        for (Object curMod : moduleOrder) {
            try {
                curMod.getClass().getDeclaredMethod("load").invoke(curMod);
                ZiahsClient.getInstance().getLogger().log(Level.INFO, curMod.getClass().getSimpleName() + " loaded.");
            } catch (NoSuchMethodException e) {
            } catch (Exception e) {
                ZiahsClient.getInstance().getLogger().log(Level.SEVERE, "Exception when loading module", e);
            }
        }
    }

    public Module getModule(String modId) {
        for (Module curMod : this.loadedModules.keySet()) {
            if (curMod.moduleId().equalsIgnoreCase(modId)) {
                return curMod;
            }
        }

        for (Module curMod : this.modules.keySet()) {
            if (curMod.moduleId().equalsIgnoreCase(modId)) {
                return curMod;
            }
        }

        for (Module curMod : this.disabledModules.values()) {
            if (curMod.moduleId().equalsIgnoreCase(modId)) {
                return curMod;
            }
        }

        return null;
    }

    public String getModuleLogo(Module mod) {
        if (!this.loadedModules.containsKey(mod)) return null;
        Class modClass = this.loadedModules.get(mod);
        if (!this.moduleInstances.containsKey(modClass)) return null;
        Object modInstance = this.moduleInstances.get(modClass);
        try {
            return (String) modClass.getMethod("getLogo").invoke(modInstance);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<Module, Class> getLoadedModules() {
        return this.loadedModules;
    }

    public Map<Module, Class> getUnloadedModules() {
        return this.modules;
    }

    public Map<Module, Class> getAllModules() {
        Map<Module, Class> modules = new HashMap<Module, Class>();
        modules.putAll(this.loadedModules);
        modules.putAll(this.modules);
        return modules;
    }

    public ClassLoader getModuleClassLoader() {
        return this.modClassLoader;
    }

    public Map<Class, Object> getModuleInstances() {
        return this.moduleInstances;
    }

    public LinkedList<Object> getModuleOrder() {
        return this.moduleOrder;
    }

    public Map<Class, Module> getDisabledModules() {
        return this.disabledModules;
    }

    public static ModuleManager getInstance() {
        return ModuleManager.instance;
    }

    public List<File> getModuleFiles() {
        return this.modFiles;
    }

    public AnnotationDetector getDetector() {
        return this.detector;
    }

    public Map<String, RepoMod> getModuleRepoData() {
        return this.moduleRepoData;
    }
}
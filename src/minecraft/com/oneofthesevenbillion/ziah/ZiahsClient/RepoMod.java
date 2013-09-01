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

public class RepoMod {
    private transient String moduleId;
    private String name;
    private String version;
    private String description;
    private String requires;
    private String recommends;
    private String incompatible;

    public RepoMod(String moduleId, String name, String version, String description, String requires, String recommends, String incompatible) {
        this.moduleId = moduleId;
        this.name = name;
        this.version = version;
        this.description = description;
        this.requires = requires;
        this.recommends = recommends;
        this.incompatible = incompatible;
    }

    public String getId() {
        return this.moduleId;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDescription() {
        return this.description;
    }

    public String getRequiredModules() {
        return this.requires;
    }

    public String getRecommendedModules() {
        return this.recommends;
    }

    public String getIncompatibleModules() {
        return this.incompatible;
    }

    public void setId(String moduleId) {
        this.moduleId = moduleId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequiredModules(String requires) {
        this.requires = requires;
    }

    public void setRecommendedModules(String recommends) {
        this.recommends = recommends;
    }

    public void setIncompatibleModules(String incompatible) {
        this.incompatible = incompatible;
    }
}
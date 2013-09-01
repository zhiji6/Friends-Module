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

package com.oneofthesevenbillion.ziah.ZiahsClient.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EventBus {
    private Map<Object, Map<Class<?extends Event>, Method>> eventHandlers = new HashMap<Object, Map<Class<?extends Event>, Method>>();
    private Map<Class, Object> moduleToEventHandlerMap = new HashMap<Class, Object>();

    public void callEvent(Event event) {
        for (Object handler : this.eventHandlers.keySet()) {
            Method method = this.eventHandlers.get(handler).get(event.getClass());
            if (method != null) {
                EventListener annotation = method.getAnnotation(EventListener.class);
                try {
                    boolean canceled = event.isCanceled();
                    method.invoke(handler, event);
                    if (canceled != event.isCanceled() && event.isCancelable()) {
                        if (event.getCancelPriority().ordinal() > annotation.priority().ordinal()) {
                            event.setCanceled(canceled);
                        }else{
                            event.setCancelPriority(annotation.priority());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Impossible
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // Impossible
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // What the heck does that mean?
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerEventHandler(Class module, Object eventHandler) {
        if (module != null) this.moduleToEventHandlerMap.put(module, eventHandler);
        Map<Class<?extends Event>, Method> eventHandlerEvents = new HashMap<Class<?extends Event>, Method>();
        for (Method method : eventHandler.getClass().getDeclaredMethods()) {
            Class[] parameters = method.getParameterTypes();
            if (method.isAnnotationPresent(EventListener.class) && parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
                method.setAccessible(true);
                eventHandlerEvents.put(parameters[0], method);
            }
        }
        this.eventHandlers.put(eventHandler, eventHandlerEvents);
    }

    public void unregisterEventHandler(Object eventHandler) {
        this.eventHandlers.remove(eventHandler);
        for (Class curModClass : this.moduleToEventHandlerMap.keySet()) {
            if (this.moduleToEventHandlerMap.get(curModClass).equals(eventHandler)) {
                this.moduleToEventHandlerMap.remove(curModClass);
            }
        }
    }

    public void unregisterEventHandler(Class module) {
        Object eventHandler = this.moduleToEventHandlerMap.get(module);
        if (eventHandler == null) return;
        this.eventHandlers.remove(eventHandler);
        this.moduleToEventHandlerMap.remove(module);
    }
}
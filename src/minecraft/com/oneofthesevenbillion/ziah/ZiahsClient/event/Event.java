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

public class Event {
    private boolean isCanceled = false;
    private boolean isCancelable;
    private EventPriority cancelPriority = EventPriority.NORMAL;

    public Event(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    public boolean isCancelable() {
        return this.isCancelable;
    }

    public boolean isCanceled() {
        return this.isCanceled;
    }

    public void setCanceled(boolean cancel) {
        if (!this.isCancelable()) {
            throw new IllegalArgumentException("Attempted to cancel a uncancelable event");
        }
        this.isCanceled = cancel;
    }

    public EventPriority getCancelPriority() {
        return this.cancelPriority;
    }

    public void setCancelPriority(EventPriority cancelPriority) {
        this.cancelPriority = cancelPriority;
    }
}
// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import com.google.inject.ImplementedBy;

import java.util.Date;

@ImplementedBy(SystemClock.class)
public abstract class Clock {

    protected Clock() {
    }

    public abstract long currentClockTimeInMillis();

    public Date currentClockDate() {
        return new Date(currentClockTimeInMillis());
    }

}
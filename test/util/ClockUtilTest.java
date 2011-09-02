// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ClockUtilTest extends FitnesseBaseTestCase {

    @Test
    public void systemClockTimeInMillisShouldIncreaseAsTimeFlies() throws Exception {
        long currentTime = 0, priorTime = 0;
        while (currentTime == priorTime) {
            currentTime = ClockUtil.currentTimeInMillis();
            if (priorTime == 0) {
                priorTime = currentTime;
            }
        }
    }

    @Test
    public void staticTimeMethodShouldDelegateToInstance() throws Exception {
        ClockUtil.inject(newConstantTimeClock(1L));
        assertThat(ClockUtil.currentTimeInMillis(), is(1L));
    }

    private Clock newConstantTimeClock(final long theConstantTime) {
        return new Clock() {
            @Override
            public long currentClockTimeInMillis() {
                return theConstantTime;
            }
        };
    }

    @Test
    public void dateMethodShouldDelegateToCurrentTimeInMillis() throws Exception {
        Clock constantTimeClock = newConstantTimeClock(2);
        assertThat(constantTimeClock.currentClockDate().getTime(), is(2L));
    }

    @Test
    public void staticDateMethodShouldDelegateToInstance() throws Exception {
        ClockUtil.inject(newConstantTimeClock(3L));
        assertThat(ClockUtil.currentDate().getTime(), is(3L));
    }

    @Test
    public void booleanConstructorArgShouldDetermineWhetherToReplaceGlobalInstance() throws Exception {
        Clock constantTimeClock = newConstantTimeClock(4);
        assertThat(ClockUtil.instance.get(), is(not(constantTimeClock)));
        constantTimeClock = newConstantTimeClock(5);
        ClockUtil.inject(constantTimeClock);
        assertThat(ClockUtil.instance.get(), is(constantTimeClock));
    }

    @Test
    public void shouldBeAbleToRestoreDefaultClock() throws Exception {
        long before = ClockUtil.currentTimeInMillis();
        assertThat(ClockUtil.currentTimeInMillis(), is(not(0L)));
        assertTrue(ClockUtil.currentTimeInMillis() - before < 1000);
    }
}

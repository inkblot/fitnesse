package fitnesse.fixtures;

import util.ClockUtil;
import util.DateAlteringClock;
import util.DateTimeUtil;

import java.text.ParseException;

public class ClockFixture {
    public void freezeClockAt(String dateTime) throws ParseException {
        ClockUtil.inject(new DateAlteringClock(DateTimeUtil.getDateFromString(dateTime)).freeze());
    }

}

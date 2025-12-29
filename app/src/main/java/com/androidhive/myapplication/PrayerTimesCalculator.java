package com.androidhive.myapplication;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import java.util.Calendar;


import java.util.Calendar;
import java.util.Date;

public class PrayerTimesCalculator {

    public static class NextPrayer {
        public final String nameFa;
        public final long timeMillis;

        public NextPrayer(String nameFa, long timeMillis) {
            this.nameFa = nameFa;
            this.timeMillis = timeMillis;
        }
    }

    public static NextPrayer computeNext(double lat, double lon) {

        Coordinates coords = new Coordinates(lat, lon);
        CalculationParameters params = CalculationMethod.KARACHI.getParameters();

        Calendar now = Calendar.getInstance();

        // امروز
        DateComponents today = new DateComponents(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH)
        );

        PrayerTimes todayTimes = new PrayerTimes(coords, today, params);

        long nowMillis = System.currentTimeMillis();

        long fajr = todayTimes.fajr.getTime();
        long dhuhr = todayTimes.dhuhr.getTime();
        long asr = todayTimes.asr.getTime();
        long maghrib = todayTimes.maghrib.getTime();
        long isha = todayTimes.isha.getTime();

        if (nowMillis < fajr)
            return new NextPrayer("صبح", fajr);
        if (nowMillis < dhuhr)
            return new NextPrayer("ظهر", dhuhr);
        if (nowMillis < asr)
            return new NextPrayer("عصر", asr);
        if (nowMillis < maghrib)
            return new NextPrayer("مغرب", maghrib);
        if (nowMillis < isha)
            return new NextPrayer("عشاء", isha);

        // بعد از عشاء → فجر فردا
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        DateComponents tomorrowDate = new DateComponents(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH) + 1,
                tomorrow.get(Calendar.DAY_OF_MONTH)
        );

        PrayerTimes tomorrowTimes = new PrayerTimes(coords, tomorrowDate, params);
        return new NextPrayer("صبح", tomorrowTimes.fajr.getTime());
    }

}

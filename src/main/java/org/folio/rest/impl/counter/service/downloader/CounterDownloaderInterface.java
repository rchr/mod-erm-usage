package org.folio.rest.impl.counter.service.downloader;

import io.vertx.core.Vertx;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public abstract class CounterDownloaderInterface {

  public abstract void fetchCounter(Vertx vertx);

  public Map<String, String> createDateParams() {
    LocalDateTime now = LocalDateTime.now();

    Month currentMonth = now.getMonth();
    int monthValue = currentMonth.getValue();

    int yearValue = now.getYear();

    int beginMonth;
    int endMonth;
    int beginYear;
    int endYear;
    int beginDay = 01;
    int endDay;

    if (monthValue == 1) {
      beginMonth = 11;
      endMonth = 11;
      beginYear = yearValue - 1;
      endYear = yearValue - 1;
    } else if (monthValue == 2){
      beginMonth = 12;
      endMonth = 12;
      beginYear = yearValue - 1;
      endYear = yearValue - 1;
    } else {
      beginMonth = monthValue - 2;
      endMonth = monthValue - 2;
      beginYear = yearValue;
      endYear = yearValue;
    }
    endDay = currentMonth.minus(1).maxLength();

    String beginMonthVal = formatVal(beginMonth);
    String endMonthVal = formatVal(endMonth);
    String beginDayVal = formatVal(beginDay);
    String endDayVal = formatVal(endDay);

    String begin = beginYear + "-" + beginMonthVal + "-" + beginDayVal;
    String end = endYear + "-" + endMonthVal + "-" + endDayVal;

    Map<String, String> dateMap = new HashMap<>();
    dateMap.put("begin", begin);
    dateMap.put("end", end);
    return dateMap;
  }

  /**
   * Formats one digit int values into two digits strings (e.g. 1 -> "01").
   */
  private String formatVal(int val) {
    return val < 10 ? "0" + val : String.valueOf(val);
  }

}

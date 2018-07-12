package org.folio.rest.impl.counter.service.downloader;

import io.vertx.core.Vertx;

public class CounterSushi5Downloader extends CounterDownloaderInterface {

  private String serviceUrl;
  private String apiKey;
  private String requestorId;
  private String customerId;
  private String report;
  private int release = 5;
  private String beginDate;
  private String endDate;
  private String platform;

  @Override
  public void fetchCounter(Vertx vertx) {

  }
}

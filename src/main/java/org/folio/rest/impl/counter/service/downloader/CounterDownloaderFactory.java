package org.folio.rest.impl.counter.service.downloader;

import io.vertx.core.Vertx;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.folio.rest.impl.counter.service.handler.Counter4Handler;
import org.folio.rest.jaxrs.model.AggregatorSetting;
import org.folio.rest.jaxrs.model.SushiSetting;

public class CounterDownloaderFactory {

  public static Set<CounterDownloaderInterface> createDownloader(SushiSetting sushiSetting,
      AggregatorSetting aggregatorSetting, Vertx vertx, String tenantName) {
    Set<CounterDownloaderInterface> downloader = new HashSet<>();
    if (aggregatorSetting.getLabel().equals("Nationaler Statistikserver")) {
      downloader = createStatistikServerDownloader(
          aggregatorSetting, sushiSetting, vertx, tenantName);
    }
    return downloader;
  }

  private static Set<CounterDownloaderInterface> createStatistikServerDownloader(
      AggregatorSetting aggregatorSetting, SushiSetting sushiSetting, Vertx vertx, String tenantName) {
    Set<CounterDownloaderInterface> counterDownloaderSet = new HashSet<>();
    String apiKey = aggregatorSetting.getApiKey();
    String customerId = aggregatorSetting.getCustomerId();
    String requestorId = aggregatorSetting.getRequestorId();
    String serviceUrl = aggregatorSetting.getServiceUrl();
    String platformId = sushiSetting.getPlatformId();
    List<String> requestedReports = sushiSetting.getRequestedReports();
    for (String report : requestedReports) {
      Counter4Handler counter4Handler = new Counter4Handler(vertx, tenantName);
      StatistikServerDownloader statistikServerDownloader = new StatistikServerDownloader(
          serviceUrl, apiKey, requestorId, customerId, report, platformId, counter4Handler);
      counterDownloaderSet.add(statistikServerDownloader);
    }
    return counterDownloaderSet;
  }

}

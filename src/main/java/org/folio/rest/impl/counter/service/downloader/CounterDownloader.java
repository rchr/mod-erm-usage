package org.folio.rest.impl.counter.service.downloader;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.List;
import java.util.Set;
import org.folio.rest.impl.AggregatorSettingsAPI;
import org.folio.rest.jaxrs.model.Aggregator;
import org.folio.rest.jaxrs.model.AggregatorSetting;
import org.folio.rest.jaxrs.model.SushiSetting;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;

public class CounterDownloader {

  private final Logger logger = LoggerFactory.getLogger(CounterDownloader.class);

  public void downloadCounterStats(List<SushiSetting> sushiSettings,
      String tenantName, Vertx vertx) {
    for (SushiSetting s : sushiSettings) {
      if (s.getAggregator() != null) {
        downloadCounterViaAggregator(s, tenantName, vertx);
      } else {
        logger.warn("Downloading counter via non aggregator currently not implemented.");
      }
    }
  }

  private void downloadCounterViaAggregator(SushiSetting sushiSetting,
      String tenantName, Vertx vertx) {

    Aggregator aggregator = sushiSetting.getAggregator();
    String aggregatorId = aggregator.getId();
    Handler<AsyncResult<Results>> handler = aggregatorReply -> this
        .handleAggregatorResponse(aggregatorReply, sushiSetting, vertx, tenantName);
    try {
      Criteria idCrit = new Criteria(
          AggregatorSettingsAPI.RAML_PATH + AggregatorSettingsAPI.SCHEMA_PATH);
      idCrit.addField("'id'");
      idCrit.setOperation("=");
      idCrit.setValue(aggregatorId);
      Criterion crit = new Criterion(idCrit);
      PostgresClient.getInstance(vertx, tenantName)
          .get("aggregator_settings", AggregatorSetting.class, crit, false, handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleAggregatorResponse(AsyncResult<Results> reply, SushiSetting sushiSetting,
      Vertx vertx, String tenantName) {
    if (reply.succeeded()) {
      List<AggregatorSetting> aggregatorSettings = (List<AggregatorSetting>) reply.result()
          .getResults();
      if (aggregatorSettings.size() == 0 || aggregatorSettings.size() > 1) {
        logger.error(
            "Aggregator size is 0 or >1. It must be exactly 1. Sushi setting id: " + sushiSetting
                .getId());
      } else {
        AggregatorSetting aggregatorSetting = aggregatorSettings.get(0);
        Set<CounterDownloaderInterface> downloader = CounterDownloaderFactory
            .createDownloader(sushiSetting, aggregatorSetting, vertx, tenantName);
        downloader.stream().forEach(cd -> cd.fetchCounter(vertx));
      }
    } else {
    }
  }


}

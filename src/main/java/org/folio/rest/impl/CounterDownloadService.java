package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.List;
import org.folio.rest.impl.counter.service.downloader.CounterDownloader;
import org.folio.rest.jaxrs.model.SushiSetting;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;
import org.folio.rest.resource.interfaces.PostDeployVerticle;

public class CounterDownloadService implements PostDeployVerticle {

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    this.startDownload(vertx, context);
  }

  private void startDownload(Vertx vertx, Context context) {
    Handler<AsyncResult<io.vertx.ext.sql.ResultSet>> tenantResponeHandler = event -> this
        .handleTenantResponse(event, vertx);
    PostgresClient.getInstance(vertx).select(
        "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE '%_mod_erm_usage'",
        tenantResponeHandler);
  }

  /**
   *
   * @param reply
   * @param vertx
   */
  private void handleTenantResponse(AsyncResult<io.vertx.ext.sql.ResultSet> reply, Vertx vertx) {
    if (reply.succeeded()) {
      io.vertx.ext.sql.ResultSet result = reply.result();
      // for each tenant: get sushi settings and start download of counter statistics
      result.getRows().stream().forEach(entries -> {
        String schemaName = entries.getString("schema_name");
        String tenantName = this.extractTenantName(schemaName);
        Handler<AsyncResult<Results>> handler = sushiReply -> this
            .handleSushiSettingsResponse(sushiReply, tenantName, vertx);
        PostgresClient.getInstance(vertx, tenantName).get("sushi_settings",
            SushiSetting.class, "", true, false, handler);
      });
    } else {
      System.out.println("ERROR HANDLE TENANT RESPONSE: " + reply.cause().getMessage());
    }
  }

  private void handleSushiSettingsResponse(AsyncResult<Results> reply, String tenantName,
      Vertx vertx) {
    if (reply.succeeded()) {
      List<SushiSetting> sushiSettings = (List<SushiSetting>) reply.result().getResults();
      CounterDownloader counterDownloader = new CounterDownloader();
      counterDownloader.downloadCounterStats(sushiSettings, tenantName, vertx);
    } else {
      System.out.println("ERROR HANDLE SUSHI SETTINGS: " + reply.cause().getMessage());
    }
  }

  private String extractTenantName(String schemaName) {
    return schemaName.replace("_mod_erm_usage", "");
  }
}

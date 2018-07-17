package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rx.java.RxHelper;
import java.util.List;
import org.folio.rest.impl.counter.service.downloader.CounterService;
import org.folio.rest.jaxrs.model.SushiSetting;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.interfaces.Results;
import org.folio.rest.resource.interfaces.PostDeployVerticle;
import rx.Observable;
import rx.Scheduler;

public class CounterDownloadService implements PostDeployVerticle {

  private Logger logger = LoggerFactory.getLogger(CounterDownloadService.class);

  static void scheduling(Vertx vertx, Context context) {
    EventBus eventBus = vertx.eventBus();
    // Consumer of the timer events
    MessageConsumer<JsonObject> consumer = eventBus.consumer("scheduler:timer");
    // Listens and prints timer events. When timer completes stops the Vertx
    consumer.handler(
        message -> {
          JsonObject event = message.body();
          if (event.getString("event").equals("complete")) {
            System.out.println("completed");
            CounterDownloadService counterDownloadService = new CounterDownloadService();
            counterDownloadService.startDownload(vertx, context);
            vertx.close();
          } else if (event.getString("event").equals("fire")) {
            System.out.println("fire");
            CounterDownloadService counterDownloadService = new CounterDownloadService();
            counterDownloadService.startDownload(vertx, context);
            vertx.close();
          } else {
            System.out.println(event);
          }
        }
    );
    // Create new timer
    JsonObject timer = (new JsonObject()).put("type", "cron")
        .put("seconds", "0").put("minutes", "23").put("hours", "15")
        .put("days of month", "*").put("months", "*")
        .put("days of week", "*");
    eventBus.send(
        "chime",
        (new JsonObject()).put("operation", "create")
            .put("name", "scheduler:timer")
            .put("description", timer),
        /*(new JsonObject()).put("operation", "create").put("name", "scheduler:timer")
            .put("publish", false).put("max count", 3)
            .put("description", (new JsonObject()).put("type", "interval").put("delay", 1)),*/
        ar -> {
          if (ar.succeeded()) {
            System.out.println("Scheduling started: " + ar.result().body());
          } else {
            System.out.println("Message failed: " + ar.cause());
            vertx.close();
          }
        }
    );
  }

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
//    this.startDownload(vertx, context);


    vertx.deployVerticle("ceylon:herd.schedule.chime/0.2.1", res -> {
      if (res.succeeded()) {
        // Chime has been successfully deployed - start scheduling
        scheduling(vertx, context);
//        this.startDownload(vertx, context);
      } else {
        System.out.println("Deployment failed! " + res.cause());
        vertx.close();
      }
    });



  }

  public void startDownload(Vertx vertx, Context context) {
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
      logger.error("Error while determining tenants: " + reply.cause().getMessage());
    }
  }

  private void handleSushiSettingsResponse(AsyncResult<Results> reply, String tenantName,
      Vertx vertx) {
    if (reply.succeeded()) {
      List<SushiSetting> sushiSettings = (List<SushiSetting>) reply.result().getResults();
      CounterService counterService = new CounterService();
      counterService.downloadCounterStats(sushiSettings, tenantName, vertx);
    } else {
      logger.error("Error while fetching sushi settings: " + reply.cause().getMessage());
    }
  }

  private String extractTenantName(String schemaName) {
    return schemaName.replace("_mod_erm_usage", "");
  }
}

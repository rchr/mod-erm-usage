package org.folio.rest.impl.counter.service.downloader;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.Map;

public class StatistikServerDownloader extends CounterDownloaderInterface {

  private final Logger logger = LoggerFactory.getLogger(StatistikServerDownloader.class);

  private String serviceUrl;
  private String apiKey;
  private String requestorId;
  private String customerId;
  private String report;
  private int release = 4;
  private String beginDate;
  private String endDate;
  private String platform;

  private Handler<AsyncResult<Buffer>> counterHandler;

  public StatistikServerDownloader(String serviceUrl, String apiKey, String requestorId,
      String customerId, String report, String platform,
      Handler<AsyncResult<Buffer>> counterHandler) {
    this.serviceUrl = serviceUrl;
    this.apiKey = apiKey;
    this.requestorId = requestorId;
    this.customerId = customerId;
    this.report = report;
    this.platform = platform;

    Map<String, String> dateParams = createDateParams();
    this.beginDate = dateParams.get("begin");
    this.endDate = dateParams.get("end");

    this.counterHandler = counterHandler;
  }

  @Override
  public void fetchCounter(Vertx vertx) {
    WebClient webClient = WebClient.create(vertx);
    String url = createUrl();
    HttpRequest<Buffer> bufferHttpRequest = webClient.getAbs(url);
    bufferHttpRequest.send(res -> {
      if (res.succeeded()) {
        HttpResponse<Buffer> result = res.result();
        Buffer body = res.result().body();
        this.counterHandler.handle(Future.succeededFuture(body));

       /* // TODO: Insert report into database
        try {
          processResponse(result, vertx, tenant);
        } catch (XPathExpressionException e) {
          e.printStackTrace();
        }*/

      }
    });
  }

  private String createUrl() {
    String url = "https://sushi.redi-bw.de/dev/Sushiservice/GetReport?"
        + "APIKey=" + this.apiKey + "&"
        + "RequestorID=" + this.requestorId + "&"
        + "CustomerID=" + this.customerId + "&"
        + "Report=" + this.report + "&"
        + "Release=" + this.release + "&"
        /*+ "BeginDate=" + this.beginDate + "&"
        + "EndDate=" + this.endDate + "&"*/
        + "BeginDate=2016-03-01&"
        + "EndDate=2016-03-31&"
        + "Platform=" + "Nature";
    return url;
  }

}

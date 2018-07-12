package org.folio.rest.impl.counter.service.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.folio.rest.impl.counter.statistikserver.ExceptionFactory;
import org.folio.rest.impl.counter.statistikserver.SushiException;
import org.folio.rest.persist.PostgresClient;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Counter4Handler implements Handler<AsyncResult<Buffer>> {

  private Vertx vertx;
  private String tenantId;

  private Logger logger = LoggerFactory.getLogger(Counter4Handler.class);

  public Counter4Handler(Vertx vertx, String tenantId) {
    this.vertx = vertx;
    this.tenantId = tenantId;
  }

  @Override
  public void handle(AsyncResult<Buffer> reply) {
    try {
      if (reply.succeeded()) {
        String s = reply.result().toString();
        Document document = stringToXML(s);
        document.getDocumentElement().normalize();

        List<SushiException> exceptions = getExceptions(document);
        if (exceptions.size() > 0) {
          return;
        }

        XPath xPath = XPathFactory.newInstance().newXPath();

        Node customerIdNode = (Node) xPath.evaluate("//*[local-name()='CustomerReference']/ID",
            document, XPathConstants.NODE);
        String customerId = customerIdNode.getFirstChild().getNodeValue();

        Node beginNode = (Node) xPath.evaluate("//*[local-name()='UsageDateRange']/Begin",
            document, XPathConstants.NODE);
        String begin = beginNode.getFirstChild().getNodeValue();

        Node endNode = (Node) xPath.evaluate("//*[local-name()='UsageDateRange']/End",
            document, XPathConstants.NODE);
        String end = endNode.getFirstChild().getNodeValue();

        Node report = (Node) xPath.evaluate("//*[local-name()='Report']/Report",
            document, XPathConstants.NODE);
        NamedNodeMap attributes = report.getAttributes();

        String created = "";
        String name = "";
        String version = "";
        if (attributes.getLength() > 0) {
          created = attributes.getNamedItem("Created").getNodeValue();
          name = attributes.getNamedItem("Name").getNodeValue();
          version = attributes.getNamedItem("Version").getNodeValue();
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("download_time", DateTime.now().toString());
        jsonObject.put("creation_time", created);
        jsonObject.put("release", version);
        jsonObject.put("report_name", name);
        jsonObject.put("begin_date", begin);
        jsonObject.put("end_date", end);
        jsonObject.put("customer_id", customerId);
        jsonObject.put("vendor_id", "");
        jsonObject.put("platform_id", "");
        jsonObject.put("format", "xml");
        jsonObject.put("report", s);
        System.out.println(jsonObject.toString());

        PostgresClient postgresClient = PostgresClient
            .getInstance(vertx, tenantId);
        postgresClient.save("counter_reports", jsonObject, event -> {
          if (event.succeeded()) {
            System.out.println("YES");
          } else {
            System.out.println("NO");
          }
        });

      } else {

      }
    } catch (XPathExpressionException e) {
      e.printStackTrace();
    }
  }

  private Document stringToXML(String input) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document = null;
    try {
      builder = factory.newDocumentBuilder();
      document = builder.parse(new InputSource(new StringReader(input)));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return document;
  }

  private List<SushiException> getExceptions(Document doc) {
    List<SushiException> exceptions = new ArrayList<>();
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList nodes = (NodeList) xPath.evaluate("//*[local-name()='Exception']",
          doc, XPathConstants.NODESET);
      if (nodes != null) {
        for (int i = 0; i < nodes.getLength(); ++i) {
          Element e = (Element) nodes.item(i);
          SushiException ex = ExceptionFactory.createSushiException(e);
          exceptions.add(ex);
          logger.warn("Counter has exception: " + ex.toString());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return exceptions;
  }

}

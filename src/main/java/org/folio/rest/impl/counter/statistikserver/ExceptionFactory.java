package org.folio.rest.impl.counter.statistikserver;

import javax.xml.xpath.XPathExpressionException;
import org.folio.rest.util.XMLUtils;
import org.w3c.dom.Element;

public class ExceptionFactory {

  public static SushiException createSushiException(Element exceptionElement)
      throws XPathExpressionException {
    String number = XMLUtils.getStringValue(exceptionElement, "Number");
    String severity = XMLUtils.getStringValue(exceptionElement, "Severity");
    String message = XMLUtils.getStringValue(exceptionElement, "Message");
    String data = XMLUtils.getStringValue(exceptionElement, "Data");
    int n = Integer.valueOf(number);
    return new SushiException(message, n, severity, data);
  }

}

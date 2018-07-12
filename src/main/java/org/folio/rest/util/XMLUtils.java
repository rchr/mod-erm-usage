package org.folio.rest.util;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

public class XMLUtils {

  public static String getStringValue(Object item, String localNodeName)
      throws XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();
    String pathString = "//*[local-name()='" + localNodeName + "']";
    Node mesageNode = (Node) xPath.evaluate(pathString,
        item, XPathConstants.NODE);
    if (mesageNode.getFirstChild() == null) {
      return "";
    }
    return mesageNode.getFirstChild().getNodeValue();
  }

}

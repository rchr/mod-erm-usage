package org.folio.rest.impl.counter.statistikserver;

public class SushiException {

  private String message;
  private int number;
  private String severity;
  private String data;

  public SushiException(String message, int number, String severity, String data) {
    this.message = message;
    this.number = number;
    this.severity = severity;
    this.data = data;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "Number: " + number + " -- Severity: " + severity + " -- Message: " + message + " -- Data: "
        + data;
  }
}

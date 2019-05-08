package model;

import java.io.Serializable;

public class Parameters implements Serializable {

  private static final long serialVersionUID = 5080735398429945509L;

  String parameterName;
  String value;
  String dataType;

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  @Override
  public String toString() {
    return "Parameters [parameterName=" + parameterName + ", value=" + value + ", dataType="
        + dataType + "]";
  }

}

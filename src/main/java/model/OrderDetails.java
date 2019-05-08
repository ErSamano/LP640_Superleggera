package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderDetails implements Serializable {

  private static final long serialVersionUID = -7740829380867659815L;

  String orderName;
  String modelNumber;

  List<Order> order = new ArrayList<>();

  public String getOrderName() {
    return orderName;
  }

  public void setOrderName(String orderName) {
    this.orderName = orderName;
  }

  public String getModelNumber() {
    return modelNumber;
  }

  public void setModelNumber(String modelNumber) {
    this.modelNumber = modelNumber;
  }

  public List<Order> getOrder() {
    return order;
  }

  public void setOrder(List<Order> order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "OrderDetails [orderName=" + orderName + ", modelNumber=" + modelNumber + "]";
  }

}

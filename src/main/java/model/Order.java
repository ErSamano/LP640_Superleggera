package model;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {

  private static final long serialVersionUID = 5185510987271431887L;

  String orderNum;
  List<Parameters> parameters;
  String block;
  String subBlock;

  public String getOrderNum() {
    return orderNum;
  }

  public void setOrderNum(String orderNum) {
    this.orderNum = orderNum;
  }

  public List<Parameters> getParameters() {
    return parameters;
  }

  public void setParameters(List<Parameters> parameters) {
    this.parameters = parameters;
  }

  public String getBlock() {
    return block;
  }

  public void setBlock(String block) {
    this.block = block;
  }

  public String getSubBlock() {
    return subBlock;
  }

  public void setSubBlock(String subBlock) {
    this.subBlock = subBlock;
  }

  @Override
  public String toString() {
    return "Order [orderNum=" + orderNum + ", parameters=" + parameters + ", block=" + block
        + ", subBlock=" + subBlock + "]";
  }

}

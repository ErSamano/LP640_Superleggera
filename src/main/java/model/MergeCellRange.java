package model;

public class MergeCellRange implements Comparable<MergeCellRange> {

  int cellStart;
  int cellEnd;
  String label;

  public MergeCellRange(int start, int end, String label) {
    this.cellStart = start;
    this.cellEnd = end;
    this.label = label;
  }

  public int getCellStart() {
    return cellStart;
  }

  public void setCellStart(int cellStart) {
    this.cellStart = cellStart;
  }

  public int getCellEnd() {
    return cellEnd;
  }

  public void setCellEnd(int cellEnd) {
    this.cellEnd = cellEnd;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public int compareTo(MergeCellRange o) {
    return this.cellStart - o.cellStart;
  }
}

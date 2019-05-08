package process;

import static util.Constants.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import model.MergeCellRange;
import model.Order;
import model.OrderDetails;
import model.Parameters;

/**
 * This class is used to read an excel and convert all it's data fields to a JSON file
 */
public class FileReaderProcessor {

  private Map<Integer, List<MergeCellRange>> mergeCellMap = new HashMap<>();
  private Map<Integer, String> columnHeaderMap = new HashMap<>();

  /**
   * Method used to parse the excel and builds the object required to convert to JSON
   */
  public void readAndProcessExcel() {

    OrderDetails orderDetails = new OrderDetails();
    List<Order> orderList = new ArrayList<>();

    try (
        FileInputStream inputFile =
            new FileInputStream("src/main/resources/" + EXCEL_FILE_NAME + ".xlsx");
        Workbook workbook = new XSSFWorkbook(inputFile);) {

      Sheet sheet = workbook.getSheetAt(0); // assume only one sheet available

      populateMergeCellRegionMap(sheet); // This method will determine the parent headers(level - by
                                         // - level)

      populateColumnHeaderMap(sheet); // This map builds the actual column header value

      orderDetails.setOrderName(sheet.getRow(0).getCell(0).getStringCellValue());
      orderDetails.setModelNumber(sheet.getRow(1).getCell(0).getStringCellValue());

      Iterator<Row> rowIterator = sheet.iterator();

      String block = EMPTY;
      String subBlock = EMPTY;

      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        int rowNum = row.getRowNum();

        System.out.println("Row number: " + rowNum);

        if (rowNum >= 9) { // Actual data starts from row >= 9
          Order order = new Order();

          Iterator<Cell> cellIterator = row.cellIterator();
          List<Parameters> paramList = new ArrayList<>();

          while (cellIterator.hasNext()) {

            Cell cell = cellIterator.next();
            int colIndex = cell.getColumnIndex();

            if (colIndex == 0) {
              // For order number, block and sub block
              switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                  order.setOrderNum(String.valueOf(cell.getNumericCellValue()));
                  break;

                case Cell.CELL_TYPE_STRING:
                  if ("H1".equals(cell.getStringCellValue()))
                    block = row.getCell(1).getStringCellValue();
                  else if ("H2".equals(cell.getStringCellValue()))
                    subBlock = row.getCell(1).getStringCellValue();
                  continue;
              }
              order.setBlock(block);
              order.setSubBlock(subBlock);

              continue;
            }

            Parameters param = new Parameters();

            switch (cell.getCellType()) {

              case Cell.CELL_TYPE_BLANK:
                break;

              case Cell.CELL_TYPE_NUMERIC:
                param.setParameterName(columnHeaderMap.get(colIndex));
                param.setDataType("Integer");
                param.setValue(String.valueOf(cell.getNumericCellValue()));
                break;

              case Cell.CELL_TYPE_STRING:
                param.setParameterName(columnHeaderMap.get(colIndex));
                param.setDataType("String");
                param.setValue(cell.getStringCellValue());
                break;
            }

            paramList.add(param);
          }
          order.setParameters(paramList);
          orderList.add(order);
        }
      }
      orderDetails.setOrder(orderList);

      buildOrderDetailsJSON(orderDetails);

    } catch (Exception e) {
      System.out.println("Exception in readAndProcessExcel: " + e);
    }
  }

  /**
   * Method used to convert the data objects to JSON
   * 
   * @param orderDetails
   */
  public void buildOrderDetailsJSON(OrderDetails orderDetails) {

    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      // String jsonInString = gson.toJson(orderDetails);
      // System.out.println("JSON String: " + jsonInString);

      gson.toJson(orderDetails, new FileWriter(JSON_OUTPUT_FILE));

    } catch (JsonIOException | IOException e) {
      System.out.println("Exception in buildOrderDetailsJSON: " + e);
    }
  }

  /**
   * Method used to concatenate the parameter header with it's parent(Hierarchical)
   * 
   * @param sheet
   */
  public void populateColumnHeaderMap(Sheet sheet) {

    try {
      int row = COL_PARAM_ROW_INDEX; // Predetermined header row
      Row headerRow = sheet.getRow(row);
      Iterator<Cell> cellIterator = headerRow.cellIterator();

      while (cellIterator.hasNext()) {
        Cell cell = cellIterator.next();
        int colIndex = cell.getColumnIndex();

        String header = getParentLabelName(sheet, headerRow, cell);

        columnHeaderMap.put(colIndex, header);
      }
    } catch (Exception e) {
      System.out.println("Exception in populateColumnHeaderMap: " + e);
    }
  }

  /**
   * Method used to determine the merged cell range which helps us to build the parameter header
   * 
   * @param sheet
   */
  public void populateMergeCellRegionMap(Sheet sheet) {

    try {
      for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
        CellRangeAddress region = sheet.getMergedRegion(i);

        int colStart = region.getFirstColumn();
        int colEnd = region.getLastColumn();
        int rowNum = region.getFirstRow();

        if (sheet.getRow(rowNum).getCell(colStart).getCellType() == Cell.CELL_TYPE_BLANK
            || sheet.getRow(rowNum).getCell(colStart) == null) {
          continue;
        }

        if (rowNum < 2)
          continue; // First two rows takes the order name & model number

        if (rowNum > 5)
          return; // The parent header available till row 5

        MergeCellRange range = new MergeCellRange(colStart, colEnd,
            sheet.getRow(rowNum).getCell(colStart).getStringCellValue());

        if (mergeCellMap.containsKey(rowNum)) {
          mergeCellMap.get(rowNum).add(range);
        } else {
          List<MergeCellRange> list = new ArrayList<>();
          list.add(range);
          mergeCellMap.put(rowNum, list);
        }
      }
    } catch (Exception e) {
      System.out.println("Exception in populateMergeCellRegionMap: " + e);
    }
  }

  /**
   * Recursively go to the top hierarchy and check any label available for this cell(within this
   * index or within the merged range(if any)), append the parent label with the current parameter
   */
  public String getParentLabelName(Sheet sheet, Row row, Cell col) {
    return getParentLabelName(sheet, row.getRowNum(), col.getColumnIndex());
  }

  public String getParentLabelName(Sheet sheet, int currentRow, int currentCol) {
    if (currentRow < 2)
      return EMPTY;

    String temp = getParentLabelName(sheet, currentRow - 1, currentCol);

    Row row = sheet.getRow(currentRow);
    System.out.println("Row Number: " + row.getRowNum());

    if (!EMPTY.equals(row.getCell(currentCol).getStringCellValue())) {
      if (EMPTY.equals(temp)) {
        temp = row.getCell(currentCol).getStringCellValue();
      } else {
        temp = temp.concat(" : ").concat(row.getCell(currentCol).getStringCellValue());
      }
    }
    // Means either it's empty or it's in the mid of merged cell range.
    else {
      String str = getCellValue(currentRow, currentCol);
      if (!EMPTY.equals(str)) {
        if (EMPTY.equals(temp)) {
          temp = str;
        } else {
          temp = temp.concat(" : ").concat(str);
        }
      }
    }
    return temp;
  }

  /**
   * Method finds whether the given cell has any parent header value
   * 
   * @param row
   * @param cell
   * @return
   */
  public String getCellValue(int row, int cell) {
    try {
      if (row > 0 && mergeCellMap.containsKey(row)) {
        List<MergeCellRange> list = (List<MergeCellRange>) mergeCellMap.get(row);

        for (MergeCellRange range : list) {
          if (cell >= range.getCellStart() && cell <= range.getCellEnd()) {
            return range.getLabel();
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Exception in getCellValue: " + e);
    }
    return EMPTY;
  }

  public static void main(String[] args) {
    new FileReaderProcessor().readAndProcessExcel();
  }
}

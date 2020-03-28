package main;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.List;

public class OfficeReader {
    private List<String> nodeNames;
    private List<Float> processingTime;
    private List<String> connectedNodes;

    public OfficeReader(List<String> nodeNames, List<Float> processingTimes, List<String> connectedNodes) {
        this.nodeNames = nodeNames;
        this.processingTime = processingTimes;
        this.connectedNodes = connectedNodes;
    }

    public void readFromFile(String filename) {
        HSSFWorkbook wb = null;
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(filename + ".xls"));
            wb = new HSSFWorkbook(fis);
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("Cant find file " + filename);
            fileNotFoundException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;
        HSSFSheet sheet = wb.getSheetAt(0);
        for (Row row : sheet) {
            for (Cell cell : row) {
                CellType cellType = cell.getCellType();
                if (cellType == CellType.STRING) {
                    if (i == 0) {
                        nodeNames.add(cell.getStringCellValue());
                    } else if (i == 2) {
                        connectedNodes.add(cell.getStringCellValue());
                    }
                } else if (cellType == CellType.NUMERIC) {
                    processingTime.add((float) cell.getNumericCellValue());
                }
                i++;
                if (i >= 3) {
                    i = 0;
                }
            }
        }
    }

    public List<String> getNodeNames() {
        return nodeNames;
    }

    public List<Float> getProcessingTime() {
        return processingTime;
    }

    public List<String> getConnectedNodes() {
        return connectedNodes;
    }
}


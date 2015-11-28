/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Ken
 */
public class ReadFile {
    
    /**
     * Gets extension of given file
     *
     * @param file
     * @return String
     */
    public static String getExtension(File file) {
        if (file.isDirectory()) {
            return "";
        }
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Get data from given training file
     *
     * @param path
     * @return ArrayList<String>
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ArrayList<String> getLines(File file) throws FileNotFoundException, IOException {
        if (getExtension(file).equalsIgnoreCase("xlsx")) {
            return readXlsxFile(file.getAbsolutePath());
        }
        return readTxtFile(file.getAbsolutePath());
    }
    
    private static ArrayList<String> readXlsxFile(String path) throws FileNotFoundException, IOException {
        ArrayList<String> lines = new ArrayList<>();

        // Create file input stream from input file
        FileInputStream fis = new FileInputStream(path);
        // Create Workbook instance holding reference to input file
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // Loop for all sheets
        for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            // Iterate through each row in input file
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                String line = "";
                line += cellIterator.next().getStringCellValue().trim();
                line += "|";
                line += cellIterator.next().getStringCellValue().trim();
                lines.add(line);
            }
        }
        fis.close();

        return lines;
    }
    
    private static ArrayList<String> readTxtFile(String path) {
        ArrayList<String> lines = new ArrayList<>();
        
        
        return lines;
    }
}

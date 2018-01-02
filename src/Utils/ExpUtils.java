package Utils;

import jxl.NumberCell;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.lucene.document.Document;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ExpUtils {

    public static Document doc = null;
    public static int docTermCount = 0;


    /**
     * write exp_output to Excel
     *
     * @param file
     * @param topics
     * @param system - 0: Lemur; 1:Lucene
     */
    public static void writeToExcel(String file, int[] topics, int system) {
        try {
            String[] splitFile = file.split("/");
            String fileName = splitFile[splitFile.length - 1];
            String outputFilePath = "C:/Users/Lab714/Desktop/Exp_output/Exp.xls";

            WritableWorkbook workbook;
            WritableSheet sheet0, sheet1, sheet2, sheet3, sheet4;

            File excel = new File(outputFilePath);

            // create new Excel
            if (!excel.exists()) {
                workbook = Workbook.createWorkbook(excel);
                sheet0 = workbook.createSheet("Overview", 0);
                sheet1 = workbook.createSheet("Original", 1);
                sheet2 = workbook.createSheet("Shawn", 2); //Rocchio

                if (system == 0) {
                    sheet3 = workbook.createSheet("Shawn", 3);
                    sheet4 = workbook.createSheet("Sun", 4);
                } else {
                    sheet3 = workbook.createSheet("Sun", 3);
                }
            }
            // update Excel
            else {
                Workbook wb = Workbook.getWorkbook(excel);
                workbook = Workbook.createWorkbook(excel, wb);
                sheet0 = workbook.getSheet(0);
            }

            WritableFont myFont = new WritableFont(WritableFont.createFont("Calibri"), 10);
            myFont.setColour(Colour.BLACK);

            WritableCellFormat cellFormat = new WritableCellFormat();
            cellFormat.setFont(myFont);
            cellFormat.setAlignment(Alignment.CENTRE);

            WritableCellFormat titleCellFormat = new WritableCellFormat();
            titleCellFormat.setFont(myFont);
            titleCellFormat.setAlignment(Alignment.CENTRE);


            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            String line;
            String split[];

            int titleRow = 0;
            int row = 1;
            int col = 1;

            int allRow1 = 1;
            int allRow2 = 1;
            int allRow3 = 1;
            int allRow4 = 1;
            int allRow5 = 1;

            int allCol1 = 1;
            int allCol2 = 9;
            int allCol3 = 17;
            int allCol4 = 25;
            int allCol5 = 33;

            int methodType = 0;
            switch (fileName) {
                case "Original":
                    methodType = 0;
                    break;
                case "Shawn": //Rocchio
                    methodType = 1;
                    allCol1 += 1;
                    allCol2 += 1;
                    allCol3 += 1;
                    allCol4 += 1;
                    allCol5 += 1;
                    break;
                case "Sun":
                    if (system == 0) {
                        methodType = 3;
                        allCol1 += 3;
                        allCol2 += 3;
                        allCol3 += 3;
                        allCol4 += 3;
                        allCol5 += 3;
                    } else {
                        methodType = 2;
                        allCol1 += 2;
                        allCol2 += 2;
                        allCol3 += 2;
                        allCol4 += 2;
                        allCol5 += 2;
                    }
                    break;
            }

            //method name
            //sheet.addCell(new Label(0, titleRow, fileName, cellFormat));
            Label methodNameLabel = new Label(0, titleRow, fileName, titleCellFormat);
            addLabelCell(methodType, workbook, methodNameLabel);

            sheet0.addCell(new Label(allCol1, 0, fileName, titleCellFormat));
            sheet0.addCell(new Label(allCol2, 0, fileName, titleCellFormat));
            sheet0.addCell(new Label(allCol3, 0, fileName, titleCellFormat));
            sheet0.addCell(new Label(allCol4, 0, fileName, titleCellFormat));
            sheet0.addCell(new Label(allCol5, 0, fileName, titleCellFormat));

            double pn = 0, pr = 0;

            while ((line = br.readLine()) != null) {
                split = line.split("\t");

                if (!line.equals("")) {
                    String field1 = split[0].trim();
                    String field2 = split[1].trim();
                    String field3 = split[2].trim();

                    //System.out.println(split[0] + " " + split[1] + " " + split[2]);

                    if (field1.equals("num_ret") || field1.equals("num_rel") || field1.equals("ndcg")
                            || field1.equals("ndcg15") || field1.equals("R-prec") || field1.equals("bpref")
                            || field1.equals("recip_rank") || field1.equals("num_q") || field1.equals("gm_ap")) {
                        continue;
                    } else {
                        Number number = new Number(col, row, Double.valueOf(field3), titleCellFormat);
                        addNumberCell(methodType, workbook, number);

                        if (field1.contains("P")) {
                            pn += Double.valueOf(field3);
                        }
                        if (field1.contains("at")) {
                            pr += Double.valueOf(field3);
                        }

                        col++;
                        if (field1.equals("at 1.00")) {
                            // pn and pr
                            Number pnNumber = new Number(23, row, pn / 9, titleCellFormat);
                            addNumberCell(methodType, workbook, pnNumber);
                            Number prNumber = new Number(24, row, pr / 11, titleCellFormat);
                            addNumberCell(methodType, workbook, prNumber);

                            if (field2.equals("all")) {
                                sheet0.addCell(new Label(0, 12, "PN", titleCellFormat));
                                sheet0.addCell(new Number(allCol1, 12, pn / 9, cellFormat));
                                sheet0.addCell(new Label(8, 12, "PR", titleCellFormat));
                                sheet0.addCell(new Number(allCol2, 12, pr / 11, cellFormat));
                            }

                            pn = pr = 0;

                            row++;
                            col = 1;
                        }

                        if (field2.equals("all")) {
                            if (allRow1 < 12) {
                                sheet0.addCell(new Label(0, allRow1, field1, titleCellFormat));
                                sheet0.addCell(new Number(allCol1, allRow1, Double.valueOf(field3), cellFormat));
                                allRow1++;
                            } else {
                                sheet0.addCell(new Label(8, allRow2, field1, titleCellFormat));
                                sheet0.addCell(new Number(allCol2, allRow2, Double.valueOf(field3), cellFormat));
                                allRow2++;
                            }
                        } else {
                            if (field1.equals("map")) {
                                sheet0.addCell(new Label(16, 0, field1, titleCellFormat));
                                sheet0.addCell(new Label(16, allRow3, field2, titleCellFormat));
                                sheet0.addCell(new Number(allCol3, allRow3, Double.valueOf(field3), cellFormat));
                                allRow3++;
                            } else if (field1.equals("P5")) {
                                sheet0.addCell(new Label(24, 0, field1, titleCellFormat));
                                sheet0.addCell(new Label(24, allRow4, field2, titleCellFormat));
                                sheet0.addCell(new Number(allCol4, allRow4, Double.valueOf(field3), cellFormat));
                                allRow4++;
                            } else if (field1.equals("P10")) {
                                sheet0.addCell(new Label(32, 0, field1, titleCellFormat));
                                sheet0.addCell(new Label(32, allRow5, field2, titleCellFormat));
                                sheet0.addCell(new Number(allCol5, allRow5, Double.valueOf(field3), cellFormat));
                                allRow5++;
                            }
                        }
                    }
                }
            }


            WritableFont titleFont = new WritableFont(WritableFont.createFont("Calibri"), 10);
            titleFont.setColour(Colour.WHITE);

            WritableCellFormat titleFormat = new WritableCellFormat();
            titleFormat.setFont(titleFont);
            titleFormat.setAlignment(Alignment.CENTRE);

            if (fileName.equals("Original") || fileName.equals("Original 1000")) {
                titleFormat.setBackground(Colour.LIGHT_ORANGE);
            } else if (fileName.equals("Shawn") || fileName.equals("Rocchio 1000")) { //Rocchio
                titleFormat.setBackground(Colour.LIME);
            } else {
                titleFormat.setBackground(Colour.LIGHT_BLUE);
            }


            // row titles
            String[] row_titles = new String[topics.length + 1];
            for (int i = 0; i < topics.length; i++) {
                row_titles[i] = String.valueOf(topics[i]);
            }
            row_titles[row_titles.length - 1] = "all";

            for (int i = titleRow; i < row_titles.length + titleRow; i++) {
                Label label = new Label(0, i + 1, row_titles[i - titleRow], titleFormat);
                addLabelCell(methodType, workbook, label);
            }

            // col titles
            String[] col_titles = {"num_rel_ret", "map",
                    "P5", "P10", "P15", "P20", "P30",
                    "P100", "P200", "P500", "P1000",
                    "at 0.00", "at 0.10", "at 0.20", "at 0.30", "at 0.40", "at 0.50",
                    "at 0.60", "at 0.70", "at 0.80", "at 0.90", "at 1.00", "PN", "PR"};

            for (int i = 0; i < col_titles.length; i++) {
                Label label = new Label(i + 1, titleRow, col_titles[i], titleFormat);
                addLabelCell(methodType, workbook, label);
            }

            workbook.write();
            workbook.close();

            // mark max value
            if (fileName.equals("Sun") || fileName.equals("Sun 1000")) {
                Workbook wb = Workbook.getWorkbook(excel);
                workbook = Workbook.createWorkbook(excel, wb);
                sheet0 = workbook.getSheet(0);

                // colStartIndex, rowStartIndex, colEndIndex, rowEndIndex
                // map to P1000
                markMaxValue(sheet0, 1, 1, 5, 12);

                // 11 point
                markMaxValue(sheet0, 9, 1, 13, 12);

                // map of each topic
                markMaxValue(sheet0, 17, 1, 21, 51);

                // P5 of each topic
                markMaxValue(sheet0, 25, 1, 29, 51);

                // P10 of each topic
                markMaxValue(sheet0, 33, 1, 37, 51);

                workbook.write();
                workbook.close();
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    private static void addLabelCell(int methodType, WritableWorkbook workbook, Label label) throws RowsExceededException, WriteException {
        WritableSheet sheet = workbook.getSheet(methodType + 1);
        sheet.addCell(label);
    }

    private static void addNumberCell(int methodType, WritableWorkbook workbook, Number label) throws RowsExceededException, WriteException {
        WritableSheet sheet = workbook.getSheet(methodType + 1);
        sheet.addCell(label);
    }

    private static void markMaxValue(WritableSheet sheet, int colStartIndex, int rowStartIndex, int colEndIndex, int rowEndIndex) throws RowsExceededException, WriteException {
        WritableFont maxFont = new WritableFont(WritableFont.createFont("Calibri"), 10);
        maxFont.setColour(Colour.RED);

        WritableCellFormat maxCellFormat = new WritableCellFormat();
        maxCellFormat.setFont(maxFont);
        maxCellFormat.setAlignment(Alignment.CENTRE);

        for (int i = rowStartIndex; i < rowEndIndex; i++) {
            double max = 0;

            // get Max value
            for (int j = colStartIndex; j < colEndIndex; j++) {
                String cell = sheet.getCell(j, i).getContents();
                if (cell != null && cell.length() != 0) {
                    NumberCell nc = (NumberCell) sheet.getCell(j, i);
                    double value = nc.getValue();

                    if (value > max) {
                        max = value;
                    }
                }
            }

            // if value == Max value then set text format
            for (int j = colStartIndex; j < colEndIndex; j++) {
                String cell = sheet.getCell(j, i).getContents();
                if (cell != null && cell.length() != 0) {
                    NumberCell nc = (NumberCell) sheet.getCell(j, i);
                    double value = nc.getValue();

                    if (value == max) {
                        sheet.addCell(new Number(j, i, max, maxCellFormat));
                    }
                }
            }
        }
    }

    public static void printTimeUsage(Date start, Date end) {
        System.out.println("===========  " + "Finished" + "  ===========");

        long milliseconds = end.getTime() - start.getTime();
        int h, m, s;
        s = (int) (milliseconds / 1000);
        h = s / 3600;
        s %= 3600;
        m = s / 60;
        s %= 60;

        System.out.println(h + " hours " + m + " minutes " + s + " seconds(" + milliseconds + " milliseconds)");
    }


    public static String getQueryString(int queryNumber) {
        Map<Integer, String> topics = getTopicList("topics");

        String queryString = topics.get(queryNumber).trim();
        if (queryNumber != 344) {
            queryString = queryString.replaceAll("[-/]", " ");

            if (queryNumber != 339) {
                queryString = queryString.replaceAll("[^A-Za-z ]", "");
            }
        }

        return queryString;
    }

    public static Map<Integer, String> getTopicList(String prop) {
        Map<Integer, String> topics = new HashMap<Integer, String>();
        File file = new File(Path.Project_Path + "/res/TopicSet/" + prop + ".txt");
        FileReader fr = null;
        BufferedReader br = null;

        try {
            // initial reader
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
//                String[] splitedLine = line.split(" ");

                topics.put(Integer.valueOf(line.substring(0, 3)), line.substring(4));
                line = br.readLine();
            }

            // close reader
            br.close();
            fr.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return topics;

    }

    public static int getDocTermCount(Document doc) {
        return getDocTermCount(doc, false);
    }

    public static int getDocTermCount(Document doc, boolean cache) {
        int docTermCount;

        if (cache) {
            if (doc.equals(ExpUtils.doc)) {
                return ExpUtils.docTermCount;
            }
            // cache is empty
            else {
                // Calculate docTermCount
                docTermCount = getDocTermCount(doc, false);
                // cache values
                ExpUtils.doc = doc;
                ExpUtils.docTermCount = docTermCount;
                return docTermCount;
            }
        } else {
            StringBuffer strb = new StringBuffer();
            String[] txt = doc.getValues(Defs.FIELD);
            for (int i = 0; i < txt.length; i++) {
                strb.append(txt[i]);
            }
            StringTokenizer tknzr = new StringTokenizer(strb.toString());
            docTermCount = tknzr.countTokens();
            return docTermCount;
        }
    }

    public static void main(String[] args) throws IOException {
        int[] topics = Topic.topics_100;
        String[] methods = {"Original", "Sun"};

        for (int i = 0; i < methods.length; i++) {
            ExpUtils.writeToExcel("C:/Users/LAB714/Desktop/Exp_output/" + methods[i], topics, 1);
        }

    }
}

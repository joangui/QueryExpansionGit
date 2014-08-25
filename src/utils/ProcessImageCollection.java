/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import FileManagement.ReadFiles;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author joan
 */
public class ProcessImageCollection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        String path = "/home/joan/DAMA/Recercaixa/DataSources/CLEF/DATASET_IMAGES_WIKIPEDIA/all_text/metadata2/";
        List<String> files = ReadFiles.readAllFilesInFolderFilterByExtension(path, "xml");
        for (String p : files) {
            File f = new File(p);
            File tempFile = new File("tmp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(f));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            Boolean write = false;

            String currentLine;



            while ((currentLine = reader.readLine()) != null) {

                currentLine=currentLine.toLowerCase();
                if (write == false && currentLine.contains("lang=\"de\"")) {
                    write = true;
                }
                if (write) {
                    writer.write(currentLine + "\n");
                }
                if (write == true && currentLine.contains("/text")) {
                    write = false;
                }
                
                if(currentLine.contains("<?xml")||currentLine.contains("<image id=")||currentLine.contains("</image")||currentLine.contains("<name>"))
                {
                    writer.write(currentLine + "\n");
                }
            }
            writer.close();
            tempFile.renameTo(f);
            reader.close();

        }

    }
}

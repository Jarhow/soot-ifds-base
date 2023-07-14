package basic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class FileUtil {
    public static List<String> readlines(String filePath){
        List<String> ret = new LinkedList<>();
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new FileReader(new File(filePath)));
        } catch (FileNotFoundException e) {
            return ret;
        }
        while(fileScanner.hasNextLine()){
            String line = fileScanner.nextLine();
            ret.add(line);
        }
        return ret;
    }
}

package basic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Util {
    public static boolean isStandardLibrary(String methodSignature) {//判断是不是标准库的方法
        if (methodSignature.startsWith("java") || methodSignature.startsWith("android") || methodSignature.startsWith("androidx") || methodSignature.startsWith("kotlin"))
            return true;
        return false;
    }

    public static Set<Integer> random(int low,int high,int size){//获取指定范围的指定个数的不同数字
        HashSet<Integer>  set=new HashSet<>();
        Random rand=new Random();
        while (set.size()<size){
            int num = rand.nextInt();
            if(num>=low&&num<high){
                set.add(num);
            }
        }
        return set;
    }
    public static boolean listEqual(List a, List b){
        if(a.size() != b.size()) return false;
        for(int ind = 0; ind < a.size(); ind++){
            if(a.get(ind) != b.get(ind)) return false;
        }
        return true;
    }

    public static boolean isSubList(List a, List b){
        if(a.size() > b.size()) return false;
        for(int ind = 0; ind < a.size(); ind++){
            if(a.get(ind) != b.get(ind)) return false;
        }
        return true;
    }

    public static void writeInfoToFile(String fileName, HashMap<String, HashSet<String>> objSet) throws IOException {
        File file = new File(fileName);
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
            for (String info : objSet.keySet()) {
                fout.write(info.getBytes());
                fout.write("\n".getBytes());
                for (String methodSig: objSet.get(info)){
                    fout.write(("   || "+methodSig).getBytes());
                    fout.write("\n".getBytes());
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            fout.close();
        }
    }

    public static String getCurrentTimeFormat(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return df.format(System.currentTimeMillis());
    }
}

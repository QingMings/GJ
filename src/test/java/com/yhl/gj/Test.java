
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        Process proc;
        try {
            ArrayList<String> cmd = new ArrayList<String>();
            cmd.add("python3");
            cmd.add("-u"); // !!!==加上参数u让脚本实时输出==!!!
            cmd.add("/Users/shishifanbuxie/Downloads/testcallpy/sum.py");
            cmd.add("100");

            proc = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
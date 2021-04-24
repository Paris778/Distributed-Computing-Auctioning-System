package project_LogicLayer;

import java.io.FileWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;


public class Logger {

    public static final void makeLog(String string) {

        try {
            FileWriter myWriter = new FileWriter("log.txt", true);
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());

            StringBuilder builder = new StringBuilder();

            builder.append(formatter.format(date));
            builder.append("\t\t");
            builder.append(string + "\n");

            myWriter.write(builder.toString());
            myWriter.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

}

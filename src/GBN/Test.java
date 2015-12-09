package GBN;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */
public class Test {

    public boolean flag;

    public static void main(String[] args) throws IOException {

        InputStream input = null;

        OutputStream output = null;

        try {

            input = new FileInputStream("send4.jpeg");

            output = new FileOutputStream("receive4.jpeg");

            byte[] buf = new byte[1024];

            int bytesRead;

            while ((bytesRead = input.read(buf)) > 0) {
                char [] arr = new String(buf).toCharArray();

                byte[] x = new String(arr).getBytes();
                output.write(x, 0, bytesRead);

            }

        } finally {

            input.close();

            output.close();

        }


//
//        PrintWriter writer = new PrintWriter(new FileWriter("receive4.jpeg"));
//        BufferedImage reader = ImageIO.read(new File("send4.jpeg"));
//        char [] arr = new char[1024];
//        int len;
//        while((len = reader.getClass() read(arr,0,1024))!=-1){
//            char [] s = Arrays.copyOf(arr,len);
//            writer.append(new String(s));
//        }
//
////        reader.close();
//        writer.close();

//        LinkedList<Integer> arr = new LinkedList<>();
//        arr.add(1);
//        arr.add(2);
//        arr.add(3);
//        arr.add(4);
//
//        Iterator<Integer> it = arr.iterator();
//        while(it.hasNext()){
//            int a = it.next();
//            System.out.println(a);
//        }

//        for (int i=0;i<10;i++){
//            int x = i;
//            Timer t = new Timer();
//            t.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    System.out.println(x);
//                }
//            }, 0, 0);
//        }
    }
}

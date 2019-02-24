package north.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

public class NorthUtils {
   public static String readText(String path) {
      try {
         byte[] encoded = Files.readAllBytes(Paths.get("/home/lvuser/" + path));
         return new String(encoded, Charset.defaultCharset());
      } catch(IOException exc) {
         exc.printStackTrace();
         return "";
      }
   }

   public static ByteBuffer read(String path) {
      try {
         FileInputStream in = new FileInputStream("/home/lvuser/" + path);
         byte[] data = new byte[in.available()];
         in.read(data);
         in.close();

         return ByteBuffer.wrap(data);
      } catch (Exception e) { e.printStackTrace(); return ByteBuffer.allocate(0); }
   }

   public static void write(String path, ByteBuffer buffer) {
      assert(buffer.hasArray());
      
      try {
         FileOutputStream out = new FileOutputStream("/home/lvuser/" + path);
         out.write(buffer.array());
         out.close();
      } catch (Exception e) { e.printStackTrace(); }
   }

   public static boolean doesFileExist(String path) {
      return (new File("/home/lvuser/" + path)).exists();
   }

   //NOTE: maps any angle into 0to360
   public static double canonicalizeAngle(double rawAngle) {
      int revolutions = (int) (rawAngle / 360);
      double mod360 = (rawAngle - revolutions * 360);
      return mod360 < 0 ? 360 + mod360 : mod360;
   }

   public static double getFeetPerPulse(double wheelDiameterInInches, double ticksPerRevolution) {
      return (2 * Math.PI * (wheelDiameterInInches / 2) / 12) / ticksPerRevolution; 
   }   

   public static double lerp(double a, double t, double b) {
      return (1 - t) * a + t * b;
   }

   public static double AngleBetween(double angle1, double angle2, boolean clockwise) {
      if(angle2 > angle1) {
         return clockwise ? (angle2 - angle1 - 360) : (angle2 - angle1);
      } else {
         return clockwise ? (angle2 - angle1) : (angle2 - angle1 + 360);
      }
   }
   
   public static boolean IsClockwiseShorter(double angle1, double angle2) {
      if(angle2 > angle1) {
         return Math.abs(angle2 - angle1 - 360) < Math.abs(angle2 - angle1);
      } else {
         return Math.abs(angle2 - angle1) < Math.abs(angle2 - angle1 + 360);
      }
   }

   public static double clamp(double min, double max, double val) {
      return Math.min(Math.max(min, val), max);
   }

   public static PowerDistributionPanel pdb = new PowerDistributionPanel();
   public static double getPercent(double voltage) {
      return clamp(-1, 1, voltage / pdb.getVoltage());
   }
}

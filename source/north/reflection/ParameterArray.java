package north.reflection;

import java.nio.ByteBuffer;

import north.North;
import north.util.NorthUtils;

public class ParameterArray {
   String file_name;
   public double[] values;
   
   public ParameterArray(String file_name) {
      this.file_name = file_name;
      
      if(NorthUtils.doesFileExist(file_name)) {
         values = readParamArrayFile(file_name);
      } else {
         values = new double[]{ 0 };
         writeParamArrayFile(file_name, values);
      }
   }
   
   public void set(int i, double val) {
      if(i < values.length) {
         values[i] = val;
      }
      North.sendCurrentParameters();
   }

   public void append(double val) {

      North.sendCurrentParameters();
   }

   public void remove(int i) {
      
      North.sendCurrentParameters();
   }

   public double get(int i) {
      return values[i];
   }

   public static double[] readParamArrayFile(String file_name) {
      ByteBuffer file = NorthUtils.read(file_name);
      int param_count = file.getInt();
      double[] result = new double[param_count];
      for(int i = 0; i < param_count; i++) {
         result[i] = file.getDouble();
      }
      return result;
   }

   public static void writeParamArrayFile(String file_name, double[] data) {
      ByteBuffer file = ByteBuffer.allocate(4 + 8 * data.length);
      file.putInt(data.length);
      for(double d : data) {
         file.putDouble(d);
      }
      NorthUtils.write(file_name, file);
   }
}
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

      writeParamArrayFile(file_name, values);
      North.sendCurrentParameters();
   }

   public void append(double val) {
      double[] new_values = new double[values.length + 1];
      System.arraycopy(values, 0, new_values, 0, values.length);
      new_values[values.length] = val;
      values = new_values;

      writeParamArrayFile(file_name, values);
      North.sendCurrentParameters();
   }

   public void remove(int i) {
      //TODO: test this, not sure if it works
      double[] new_values = new double[values.length - 1];
      System.arraycopy(values, 0, new_values, 0, i);
      System.arraycopy(values, i + 1, new_values, i, values.length - i - 1);
      values = new_values;

      writeParamArrayFile(file_name, values);
      North.sendCurrentParameters();
   }

   public double get(int i) {
      return values[i];
   }

   //-----------------------------------
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
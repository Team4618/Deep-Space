package north.reflection;

import north.North;
import north.util.NorthUtils;

public class Parameter {
   String file_name;
   double value;

   public Parameter(String file_name) {
      this.file_name = file_name;
      
      if(NorthUtils.doesFileExist(file_name)) {
         double[] values = ParameterArray.readParamArrayFile(file_name);
         assert(values.length == 1);
         value = values[0];
      } else {
         set(0);
      }
   }

   public void set(double val) {
      value = val;
      ParameterArray.writeParamArrayFile(file_name, new double[]{ value });
      North.sendCurrentParameters();
   }

   public double get() {
      return value;
   }
}
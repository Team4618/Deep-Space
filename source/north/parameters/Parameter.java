package north.parameters;

import north.North;
import north.util.NorthUtils;

public class Parameter {
   String file_name;
   double value;

   public Parameter(String group, String name) {
      this.file_name = "G" + group + "P" + name + ".param_array";
      North.getOrCreateGroup(group).parameters.put(name, this);
      
      // if(NorthUtils.doesFileExist(file_name)) {
      //    double[] values = ParameterArray.readParamArrayFile(file_name);
      //    assert(values.length == 1);
      //    value = values[0];
      // } else {
      //    set(0);
      // }

      North.sendCurrentParameters();
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
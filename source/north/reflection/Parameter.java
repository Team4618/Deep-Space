package north.reflection;

public class Parameter {
   String file_name;
   double value;

   public Parameter(String file_name) {
      this.file_name = file_name;
      //TODO: read from file
   }

   public void set(double val) {

   }

   public double get() {
      return value;
   }
}
package north.autonomous;

import north.drivecontroller.IDriveController;

public interface IExecutable {
   //NOTE: returns the executable that will run next cycle
   //      "return this" to continue executing
   //      return null once done execution
   IExecutable execute();
   
   //NOTE: gets called when this becomes the new current_executable
   void init();

   //NOTE: null drive controller runs the default drive controller
   default IDriveController getDriveController() {
      return null;
   }
}
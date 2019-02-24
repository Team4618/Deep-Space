package north.drivecontroller;

public interface IDriveController {
   void init(); //NOTE: gets called when we switch to this controller
   void periodic();
}
package north.autonomous;

public interface IExecutable {
   //NOTE: returns the executable that will run next cycle
   //      "return this" to continue executing
   //      return null once done execution
   IExecutable execute();
}
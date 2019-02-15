package north.autonomous.commands;

public interface ICommandInstance {
   void reset();
   boolean invoke();
}
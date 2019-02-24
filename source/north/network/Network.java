package north.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import static north.network.NetworkDefinitions.*;

public class Network {
   public static Selector selector;
   public static ServerSocketChannel channel;
   public static ArrayList<ConnectedClient> connections = new ArrayList<>();

   public static class ConnectedClient {
      public Selector selector;
      public SocketChannel channel;
      public boolean wantsState;
      
      public ConnectedClient(SocketChannel in_channel) {
         try {
            channel = in_channel;

            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
         } catch(Exception e) { e.printStackTrace(); }
      }

      public void send(ByteBuffer data) {
         try {
               channel.write(data);
         } catch (IOException e) { e.printStackTrace(); }
      }
   }

   public static void init() {
      try {
         selector = Selector.open();
         channel = ServerSocketChannel.open();
         channel.configureBlocking(false);
         channel.bind(new InetSocketAddress(5800));
         channel.register(selector, channel.validOps());
      } catch(Exception e) { e.printStackTrace(); }
   }

   public static void send(ByteBuffer data) {
      for(ConnectedClient client : connections) {
         client.send(data);
      }
   }

   public static void HandlePacket(SocketAddress sender, ByteBuffer data, byte type) {
      switch(type) {
         case SetConnectionFlags: {
            //TODO: set param
            //TODO: send current params
         } break;

         case ParameterOp: {
            
         } break;

         case SetState: {
               //TODO: set state
         } break;

         case UploadAutonomous: {

         } break;

         default:
               System.err.println("Invalid packet type " + type + " received");
      }
   }

   public static ByteBuffer buffer = ByteBuffer.allocate(16384);
   public static void tick() {
      try {
            //TODO: Check for incoming connections
            selector.selectNow();
            for(SelectionKey key : selector.selectedKeys()) {
               if(key.isAcceptable()) {

               }
            }

            //check last ping times of connected things, remove from list if timed out
            Iterator<ConnectedClient> clients = connections.iterator();
            while(clients.hasNext()) {
               ConnectedClient client = clients.next();
               
               //TODO: check if there are any complete packets recieved from this client
               client.selector.select();
               for(SelectionKey key : client.selector.selectedKeys()) {
                  if(key.isReadable()) {
                     // client.channel.read();
                     // HandlePacket(sender, buffer);
                  }
               } 

               if(!client.channel.isConnected()) {
                  System.out.println(client.channel.getLocalAddress().toString() + " has disconnected");
                  clients.remove();
               }
            }

            //send state
            // ByteBuffer statePacket = createStatePacket();
            // send(statePacket);
      } catch (Exception e) { e.printStackTrace(); }
   }
}

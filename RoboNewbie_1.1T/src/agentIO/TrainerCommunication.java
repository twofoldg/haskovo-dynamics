/*******************************************************************************
* RoboNewbie
* NaoTeam Humboldt
* @author Monika Domanska
* @version 1.0
* Adapted for trainer programs by hdb Oct. 2016
* 
* Package agentIO uses code from magmaOffenburg.  http://robocup.fh-offenburg.de/
*******************************************************************************/

package agentIO;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Connection of trainer programs to the SimSpark server, sending of messages via
 * TCP.
 * <p>
 * This class was adapted from agentIO.ServerCommunication. 
 * Differences concern:
 *      - Changed port to 3200 (communication port for monitor).
 *      - Only sending of messages, no receiving.  
 *  For implemtation details see documentation in agentIO.ServerCommunication.
 * 
 * <p/>
 * The source code is partly copied from the RoboCup-Team magmaOffenburg. <br>
 * http://robocup.fh-offenburg.de/html/downloads.htm , downloaded at 14.1.2012. <br>
 * Path in the source directory: <br>
 * src\magma\agent\connection\impl\ServerConnection.java
 * <p/>
 */
public class TrainerCommunication{

  private String host = "127.0.0.1";
  private int port = 3200;
  private DataOutputStream out;
  private Socket socket;

  /**
   * Constructor, establishes the TCP-connection to the server.
   */
  public TrainerCommunication() {

    try {
      socket = new Socket(host, port);
      socket.setTcpNoDelay(true);

      out = new DataOutputStream(socket.getOutputStream());

      System.out.println("Trainer Connection to: " + host + ":" + port);

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (ConnectException e) {
      System.out.println(host
              + ":"
              + port
              + " refused the connection. Is rcssserver3d running? Are you using an IPv6-enabled"
              + " system and the host name translates to an IPv6 address?");
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
    
  
  /**
   * Sends a trainer message to the server.
   * <p/>
   * This method formats a trainer message (String)
   * according to the network protocol and sends it to the server.
   * <p/>
   * The content of the message is not validated.
   * @param msg Trainet message with commands to the server.
   */
 public void sendTrainerMessage(String msg) {
  
    byte[] body = msg.getBytes();

    int len = body.length;
    int byte0 = (len >> 24) & 0xFF;
    int byte1 = (len >> 16) & 0xFF;
    int byte2 = (len >> 8) & 0xFF;
    int byte3 = len & 0xFF;

    try {
      out.writeByte((byte) byte0);
      out.writeByte((byte) byte1);
      out.writeByte((byte) byte2);
      out.writeByte((byte) byte3);
      out.write(body);
      out.flush();
    } catch (IOException e) {
      System.out.println("Error writing to socket. Has the server been shut down?");
    }
  }


}
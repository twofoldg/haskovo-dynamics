/*******************************************************************************
 *  RoboNewbie
 * NaoTeam Humboldt
 * @author Monika Domanska
 * @version 1.1
 *******************************************************************************/

package myAgents;


import agentIO.EffectorOutput;
import agentIO.PerceptorInput;
import agentIO.ServerCommunication;
import directMotion.LookAroundMotion;
import java.io.IOException;
import java.util.logging.Level;

import examples.agentSoccerTeam.SimpleGoalie;
import examples.agentSoccerTeam.SoccerTeamThinking;
import keyframeMotion.KeyframeMotion;
import localFieldView.LocalFieldView;
import util.Logger;

public class GoalKeeperAgent {

  public static void main(String[] args) {

    GoalKeeperAgent agent = new GoalKeeperAgent();

    agent.init();

    agent.run();

    agent.printlog();

    System.out.println("Agent stopped.");
  }

  private Logger log;
  private PerceptorInput percIn;
  private EffectorOutput effOut;
  private KeyframeMotion kfMotion;
  private LocalFieldView localView;
  private SoccerTeamThinking goalKeeperThinking;
  private LookAroundMotion lookAround;

  /** A player is identified in the server by its player ID and its team name.
   There are at most two teams an the field, and every agent of a single team
   must have a unique player ID between 1 and 11.
   If the identification works right, it is visualized in the monitor:
   the robots on the field have either red or blue parts. An unidentified
   robot has grey parts. */
  final String id = "1";
  final String team = "simpleSoccer";
  /** The "beam"-coordinates specify the robots initial position on the field.
   The root of the global field coordinate system is in the middle of the
   field, the system is right-handed. The x-axis points to the opponent goal,
   so the initial position has a negative x-value to beam the robot on its own
   half. The robot can be placed with an initial orientation given in the
   variable beamRot, in degrees, counterclockwise relative to the x-axis. */
  final double beamX =    -4;
  final double beamY =     0;
  final double beamRot =   0;

  /**
   * Initialize the connection to the server, the internal used classes and
   * their relations to each other, and create the robot at a specified position
   * on the field.
   */

  private SimpleGoalie goaltender;

  private void init() {

    ServerCommunication sc = new ServerCommunication();
    sc.initRobot(id, team, beamX, beamY, beamRot);

    log = new Logger();
    percIn = new PerceptorInput(sc);
    effOut = new EffectorOutput(sc);
    kfMotion = new KeyframeMotion(effOut, percIn, log);
    localView = new LocalFieldView(percIn, log, team, id);
    lookAround = new LookAroundMotion(percIn, effOut, log);

    goalKeeperThinking = new SoccerTeamThinking(percIn, localView, kfMotion, log, sc);
    goaltender = new SimpleGoalie(kfMotion, percIn, log);
  }


  /**
   * Main loop of the agent program, where it is synchronized with the
   * simulation server.
   *
   * How long the agent program will run can be changed in variable
   * "agentRunTimeInSeconds". This is just an approximation, because this value
   * is used to calculate a number of server cycles, and the agent will
   * participate in this amount of cycles.
   */
  public void run(){

    int agentRunTimeInSeconds = 1200;

    // The server cycle represents 20ms, so the agent has to execute 50 cycles
    // to run 1s.
    int totalServerCycles = agentRunTimeInSeconds * 50;

    // This loop synchronizes the agent with the server.
    for (int i = 0; i < totalServerCycles; i++) {

      //check for aborting the agent from the console (by hitting return)
      try {
        if (System.in.available() != 0)
          break;
      } catch (IOException ex) {
        java.util.logging.Logger.getLogger(GoalKeeperAgent.class.getName()).log(Level.SEVERE, null, ex);
      }

      sense();

      think();

      act();
    }
  }

  /**
   * Update the world and robot hardware informations, that means process
   * perceptor values provided by the server.
   */
  private void sense() {
    // Receive the server message and parse it to get the perceptor values.
    percIn.update();
    // Proceed and store values of the vision perceptor.
    localView.update();
  }

  /**
   * Decide, what is sensible in the actual situation.
   * Use the knowledge about the field situation updated in sense(), and choose
   * the next movement - it will be realized in act().
   */
  private void think(){
    goaltender.decide();
  }

  /**
   * Move the robot hardware, that means send effector commands to the server.
   */
  private void act(){
    // Calculate effector commands and send them to the server, this method
    // of class KeyframeMotion has to be called in every server cycle.
    kfMotion.executeKeyframeSequence(); // No matter, which move the robot executes, it should
    // always turn its head around. So the LookAroundMotion
    // is called after the KeyframeMotion, to overwrite the
    // commands for the head.
    // Send agent message with effector commands to the server.
    effOut.sendAgentMessage();
  }

  /**
   * Print log informations - if there where any.
   */
  private void printlog() {
    log.printLog();
  }

}

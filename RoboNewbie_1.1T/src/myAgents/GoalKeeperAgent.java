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

  final String id = "1";
  final String team = "Haskovo-Dynamics";

  final double beamX =    -4;
  final double beamY =     0;
  final double beamRot =   0;

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

  private void sense() {
    // Receive the server message and parse it to get the perceptor values.
    percIn.update();
    // Proceed and store values of the vision perceptor.
    localView.update();
  }

  private void think(){
    goaltender.decide();
  }

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

  private void printlog() {
    log.printLog();
  }

}

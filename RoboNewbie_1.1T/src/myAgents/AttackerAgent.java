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
import agentIO.perceptors.GameStatePerceptor;
import directMotion.LookAroundMotion;
import java.io.IOException;
import java.util.logging.Level;

import examples.agentSoccerTeam.Agent_SoccerTeam;
import examples.agentSoccerTeam.SimpleSoccer_withKick;
import examples.agentSoccerTeam.SoccerTeamThinking;
import keyframeMotion.KeyframeMotion;
import localFieldView.LocalFieldView;
import util.Logger;

import static util.GameStateConsts.*;

public class AttackerAgent {

  public static void main(String[] args) {

    AttackerAgent agent = new AttackerAgent();

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
  private SoccerTeamThinking attackerThinking;
  private SimpleSoccer_withKick player;
  private LookAroundMotion lookAround;

  private static enum AfterGoalState {STOP_MOTION,START_INIT_POSE,INIT_POSE, BEAM, FINISHED};
  private static AttackerAgent.AfterGoalState afterGoalState = AttackerAgent.AfterGoalState.STOP_MOTION;

  PlayMode pm;

  final String id = "2";
  final String team = "Haskovo-Dynamics";

  final double beamX =    -0.5;
  final double beamY =     0;
  final double beamRot =   0;
  ServerCommunication sc;

  private void init() {

    sc = new ServerCommunication();
    sc.initRobot(id, team, beamX, beamY, beamRot);

    log = new Logger();
    String agentNumber = sc.finalRobotID;
    percIn = new PerceptorInput(sc);
    effOut = new EffectorOutput(sc);
    kfMotion = new KeyframeMotion(effOut, percIn, log);
    localView = new LocalFieldView(percIn, log, team, agentNumber);
    lookAround = new LookAroundMotion(percIn, effOut, log);

    attackerThinking = new SoccerTeamThinking(percIn,localView, kfMotion, log, sc);

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
        java.util.logging.Logger.getLogger(Agent_SoccerTeam.class.getName()).log(Level.SEVERE, null, ex);
      }

      sense();

      think();

      act();
    }
  }

  private void sense() {
    // Receive the server message and parse it to get the perceptor values.
    percIn.update();
    // Get gamestate
    GameStatePerceptor g = percIn.getGameState();
    pm = g.getPlayMode();
    // Proceed and store values of the vision perceptor.
    localView.update();
  }


  private void think() {

    switch (pm) {
      case BeforeKickOff: {
        break; //do nothing
      }
      case KickOff_Left:
      case KickOff_Right: {
        afterGoalState = AttackerAgent.AfterGoalState.STOP_MOTION;
        break;    //do nothing
      }
      case Goal_Left:
      case Goal_Right: {
        prepareKickOff();   //prepare for kick-off
        break;
      }
      default: {
        attackerThinking.decide(); //in all other game states
        break;
      }
    }
  }


  private void prepareKickOff() {
    switch (afterGoalState) {
      case STOP_MOTION: {
        kfMotion.stopMotion(); // not necessary
        //if a new motion is called (as here in next cycle)

        afterGoalState = AttackerAgent.AfterGoalState.START_INIT_POSE;
        break;
      }
      case START_INIT_POSE: {
        kfMotion.setReturnToInitialPose();
        afterGoalState = AttackerAgent.AfterGoalState.INIT_POSE;
        break;
      }
      case INIT_POSE: {
        if (kfMotion.ready()) {
          afterGoalState = AttackerAgent.AfterGoalState.BEAM;
        }
        break;
      }
      case BEAM: {
        sc.sendBeamMessage();
        afterGoalState = AttackerAgent.AfterGoalState.FINISHED;
        break;
      }
      case FINISHED: {
        break;
      }
    }
  }

  private void act() {
    switch (pm) {
            /*
            case BeforeKickOff:
            case KickOff_Left:
            case KickOff_Right: {
                break;     //dont move
            }
            *
            */
      default:    // in all other game states except after
        kfMotion.executeKeyframeSequence();
        // No matter, which motion the robot executes, it should
        // always turn its head around. So the LookAroundMotion
        // is called after the KeyframeMotion, to overwrite the
        // commands for the head.
        lookAround.look();
        effOut.sendAgentMessage();
        break;
    }
  }

  private void printlog() {
    log.printLog();
  }
}

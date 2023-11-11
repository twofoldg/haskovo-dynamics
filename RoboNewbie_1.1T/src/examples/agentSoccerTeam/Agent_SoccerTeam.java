/*******************************************************************************
*  RoboNewbie
* NaoTeam Humboldt
* @author Hans-Dieter Burkhard, 16.10.2014
* @version 1.1
* 
* Program is a modification of Agent_SimpleSoccer written by Monika Domanska
* 
* changed 14,10.2016 hdb
*******************************************************************************/

package examples.agentSoccerTeam;

import agentIO.EffectorOutput;
import agentIO.PerceptorInput;
import agentIO.ServerCommunication;
import agentIO.perceptors.GameStatePerceptor;
import directMotion.LookAroundMotion;
import java.io.IOException;
import java.util.logging.Level;
import keyframeMotion.KeyframeMotion;
import localFieldView.LocalFieldView;
import util.Logger;
import util.GameStateConsts.PlayMode;
import util.RobotConsts;

/**
 * This agent can be used to start all players of a team.
 * If the player id is set to "0", each call instantiates a new player. 
 * The player numbers are then assigned by the server in the order of starting 
 * the program. 
 * Depending on this number, the players can have different roles to be 
 * specified in class SoccerTeamThinking.
 * 
 * All parameters determining the identity (teamSide, teamName, finalRobotID,
 * finalBeamCoordsX, finalBeamCoordsY, finalBeamCoordsRot) are contained in
 * ServerCommunication.
 * 
 * Usage: 
 * Specify final String team = <name of team> .
 * Run Agent_SoccerTeam for each player you want to have, numbers are 
 * consecutively assigned by the server. 
 * The initial positions (beam-poses) are defined in class util.BeamPoses .
 * The roles of the players are distributed according to their numbers in 
 * class SoccerTeamThinking.
 * You can use the program for the second team as well: 
 * Simply change the team name and proceed as before. 

 * 
 */
public class Agent_SoccerTeam {

  public static void main(String args[]) {
    
    Agent_SoccerTeam agent = new Agent_SoccerTeam();
    
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
  private SoccerTeamThinking soccerTeamThinking;
  private LookAroundMotion lookAround;
  
  /** A player is identified in the server by its player ID and its team name. 
   There are at most two teams an the field, and every agent of a single team 
   must have a unique player ID between 1 and 11. 
   If the identification works right, it is visualized in the monitor: 
   the robots on the field have either red or blue parts. An unidentified 
   robot has grey parts. 
   With player id set to "0", the program can be used to start all 
   players of a team one after the other as described above.  
   You can use a number between 1 and 11 instead to start a player with that 
   identity.
   */
  final String id = "0";
  final String team = "SoccerTeam";
  
  /** If the player id is set to "0",the "beam"-coordinates given below are
   overwritten by the server according to the definitions in the 
   class util.BeamPoses.
   The "beam"-coordinates specify the robots initial position on the field.
   The root of the global field coordinate system is in the middle of the 
   field, the system is right-handed. The x-axis points to the opponent goal, 
   so the initial position has a negative x-value to beam the robot on its own
   half. The robot can be placed with an initial orientation given in the 
   variable beamRot, in degrees, counterclockwise relative to the x-axis. */
  final double beamX =    -0.3;
  final double beamY =     0.0;
  final double beamRot =   0;
  
    ServerCommunication sc;
    PlayMode pm;

    private static enum AfterGoalState {STOP_MOTION,START_INIT_POSE,INIT_POSE, BEAM, FINISHED};
    private static AfterGoalState afterGoalState = AfterGoalState.STOP_MOTION;

    /**
   * Initialize the connection to the server, the internal used classes and 
   * their relations to each other, and create the robot at a specified position 
   * on the field. 
   */
  private void init() {
  
    log = new Logger();

    sc = new ServerCommunication();
    sc.initRobot(id, team, beamX, beamY, beamRot);
   
    String agentNumber = sc.finalRobotID;
    percIn = new PerceptorInput(sc);
    effOut = new EffectorOutput(sc);
    kfMotion = new KeyframeMotion(effOut, percIn, log);
    localView = new LocalFieldView(percIn, log, team, agentNumber);
    lookAround = new LookAroundMotion(percIn, effOut, log);  
    soccerTeamThinking = new SoccerTeamThinking(percIn, localView, kfMotion, log, sc);   
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
        java.util.logging.Logger.getLogger(Agent_SoccerTeam.class.getName()).log(Level.SEVERE, null, ex);
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
    // Get gamestate
       GameStatePerceptor g = percIn.getGameState();
       pm = g.getPlayMode();
    // Proceed and store values of the vision perceptor.
    localView.update();
  }
     /**
     * Decide, what is sensible in the actual situation. Use the knowledge about
     * the field situation updated in sense(), and choose the next movement - it
     * will be realized in act().
     *
     * In RoboCup games (according to soccer rules), the kick-off team can kick
     * already in game state kick_off. The other team must wait and not kick
     * before the game state changes to play-on.  Crossing the middle line is 
     * not allowed for both teams before game state "play-on".
     *
     * Left team has kick-off at the start of the game (after pressing "k" by referee).
     * After scoring, the other team gets kick-off by the server.
     *
     * For our purposes we have modified some related parameters in the simspark 
     * config files. It is supposed that the referee presses "k" (kick-off)
     * and "b" (drop ball). Then both teams can start acting, kick the ball and
     * cross the middle line without restrictions. 
     *
     *
     * In the implementation below:
     * - nothing is done at before-Kick-off and kick-off, 
     * - after goals, the robot is prepared for kick-off: it changes its joints 
     *   to its initial pose. Then it is beamed to initial beam position. 
     * - The decide method is called in all other game states. The switch in 
     * method act is organized in a similar way.
     *
     * Special decide methods could be implemented and called for other game
     * states, e.g. for kick-in and corner-kick. Then the switch in act-method
     * should be changed accordingly.
     *
     */
  
    private void think() {
  
        switch (pm) {
            case BeforeKickOff: {
                break; //do nothing
            }
            case KickOff_Left:
            case KickOff_Right: {
                afterGoalState = AfterGoalState.STOP_MOTION;
                break;    //do nothing
            }
            case Goal_Left:
            case Goal_Right: {
                prepareKickOff();   //prepare for kick-off
                break;
            }
            default: {
                soccerTeamThinking.decide(); //in all other game states 
                break;
            }
        }
    }
    
  
    private void prepareKickOff() {
        switch (afterGoalState) {
            case STOP_MOTION: {
                kfMotion.stopMotion(); // not necessary 
                         //if a new motion is called (as here in next cycle)
                
                afterGoalState = AfterGoalState.START_INIT_POSE;
                break;
            }
            case START_INIT_POSE: {
                kfMotion.setReturnToInitialPose();
                afterGoalState = AfterGoalState.INIT_POSE;
                break;
            }
            case INIT_POSE: {
                if (kfMotion.ready()) {
                    afterGoalState = AfterGoalState.BEAM;
                }
                break;
            }
            case BEAM: {
                sc.sendBeamMessage();
                afterGoalState = AfterGoalState.FINISHED;
                break;
            }
            case FINISHED: {
                break;
            }
        }
    }
 
    /**
     * Move the robot hardware, that means send effector commands to the server.
     * 
     * The cases should correspond to the cases in method think.
     */
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
                /**
                 * Calculate effector commands and send them to the server, this
                 * method of class KeyframeMotion has to be called in every
                 * server cycle where keyframe motions called by method think
                 * should be performed.
                 */
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

    /**
     * Print log informations - if there where any.
     */
    private void printlog() {
        log.printLog();
    }
}

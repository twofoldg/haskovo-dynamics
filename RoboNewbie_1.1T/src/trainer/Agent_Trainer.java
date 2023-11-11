/**
 * *****************************************************************************
 * RoboNewbie NaoTeam Humboldt
 *
 * @author Hans-Dieter Burkhard
 * @version 1.1      Oct. 29th, 2016
 * 
 * Beta-Version, Tests needed
 * *****************************************************************************
 */
package trainer;

import agentIO.EffectorOutput;
import agentIO.PerceptorInput;
import agentIO.ServerCommunication;
import agentIO.perceptors.*;
import util.Logger;
import trainer.TrainerCommandExecutor.Team;
import util.GameStateConsts.PlayMode;
import util.GameStateConsts;

/**
 * This agent provides examples for testing players. The class TrainerThinking
 * contains two examples for usages. See class TrainerThinking for the details.
 *
 * To perform a test: First start the player to be tested (blue team), then
 * start this program (red team). The program does not need a human start
 * command at "BeforeKickOff".
 *
 * The structure follows the structure of the package examples.agentSimpleSocker
 * (see there for more details).
 * 
 * The program can be extended with automatic evaluations and statistics of the 
 * results of experiments. Therewith, it could be used for Machine Learning. Extend
 * the controlExperiments-method of class TrainerThinking accordingly
 * 
 */

public class Agent_Trainer {

    public static void main(String args[]) {
        Agent_Trainer agent = new Agent_Trainer();

        // Establish the connection to the server.
        agent.init();

        // Run the agent program synchronized with the server cycle.
        // Parameter: Time in seconds the agent program will run. 
        // Should be long enough to perform all intended tests.
        agent.run(300);

        agent.printlog();

        System.out.println("Agent stopped.");
    }
    private Logger log;
    private PerceptorInput percIn;
    private EffectorOutput effOut;
    private TrainerCommandExecutor trainer;
    
    private TrainerThinking trainerThinking;
    
    /**
     * As usual.
     */
    static final String id = "1";
    static final String team = "Trainer";
    /**
     * The "beam"-coordinates specify the robots initial position on the field.
     * Take care that the position does not disturb the experiments. For automatic 
     * evaluations, place it to a position where the measurements can be easily done.
     */
    static final double beamX = -4.5;
    static final double beamY = 3;
    static final double beamRot = 0;
    /**
     * Initialize the connection to the server, the internal used classes and
     * their relations to each other, and create the robot at a specified
     * position on the field.
     */
    private GameStatePerceptor gameState;
    private static PlayMode playMode;
    private PlayMode lastPlayMode = PlayMode.BeforeKickOff;

    public static PlayMode getPlayMode() {
        return playMode;
    }

    private void init() {

        // connection to the server
        ServerCommunication sc = new ServerCommunication();

        // internal agent classes
        log = new Logger();
        percIn = new PerceptorInput(sc);
        effOut = new EffectorOutput(sc);

        trainer = new TrainerCommandExecutor();
        trainerThinking = new TrainerThinking(percIn, trainer, log);

        // simulated robot hardware on the soccer field
        sc.initRobot(id, team, beamX, beamY, beamRot);
    }

    /**
     * Main loop of the agent program, where it is synchronized with the
     * simulation server.
     *
     * @param timeInSec Time in seconds the agent program will run
     */
    private void run(int timeInSec) {
        // The server executes about 50 cycles per second. 
        int cycles = timeInSec * 50;
        int cycles2sec = 100;

        // do nothing for 2 seconds, just stay synchronized with the server
        for (int i = 0; i < cycles2sec; i++) {
            sense();
            act();
        }

        // Loop synchronized with server.
        for (int i = 0; i < cycles; i++) {

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
    }

   /*
    * Synchronize trainer actions with the game states. Method TrainerThinking of 
    * class TrainerThinking implements a state machine for the control of 
    * experiments.
    */
    
    private void think() {

        gameState = percIn.getGameState();
        lastPlayMode = playMode;
        playMode = gameState.getPlayMode();

        //  System.out.print(percIn.getServerTime() +  "   playmode: " + playMode );

        /*
         * The trainer sets game state "Play_On" by a dropBall-command. It gives
         * access to the ball for all players. Since commands are delayed, the
         * command could be sent twice. The tests in the game states
         * "KickOff_Left" and "KickOff_Right" prevent from that.
         */

        switch (playMode) {
            case BeforeKickOff: {
                trainer.dropBall();
                break;
            }
            case KickOff_Left: {
                if (playMode != lastPlayMode) {
                    trainer.dropBall();
                }
                break;
            }
            case KickOff_Right: {
                if (playMode != lastPlayMode) {
                    trainer.dropBall();
                }
                break;
            }
            /*
             * In principle, trainer commands should be given only at game state
             * "PlayOn" when all players can act. Exceptions concern the
             * commands implemented in some cases of this switch command.
             */              
            case PlayOn: {
                trainerThinking.controlExperiments(); // settings for experiments
                break;
            }
            /*
             * There should be no game state changes by the trainer after goals. Just
             * wait for the server to set "KickOff". Explanation: The server
             * acts by some constraints at the game states "Goal_Left" and
             * "Goal_Right". As long as the ball is in the goal, the game state
             * can not be changed. Early game state changes by the trainer could
             * lead to server crashes.
             *
             * The pause aufter goals is also needed when agents prepare for
             * beaming after goals (cf. Agent_SoccerTeam).
             */
            case Goal_Left: {
                if (playMode != lastPlayMode) {    // prevent from calling several times
                    trainerThinking.monitorGameStates(GameStateConsts.PlayMode.Goal_Left);
                }
                break;
            }
            case Goal_Right: {
                if (playMode != lastPlayMode) {     // prevent from calling several times
                    trainerThinking.monitorGameStates(GameStateConsts.PlayMode.Goal_Right);
                }
                break;
            }

            default: {
                /*
                 * This is needed for games states like corner kicks where the
                 * server otherwise may wait for a human start command. It is
                 * necessary to beam the ball away  because a ball outside the
                 * field can make the server turning back to the former game
                 * state (e.g. for corner kicks). Note that all changes by
                 * trainer commands are delayed by one cycle like effector
                 * commands.
                 */

                if (playMode != lastPlayMode) {   // prevent from calling several times
                    trainerThinking.monitorGameStates(playMode);
                    trainer.beamBall(0, 0);
                }
                trainer.kickOff(Team.RIGHT);
                break;

            }
        }
    }

    /**
     * Send the trainer commands, that means send effector commands to the
     * server. Motion commands are also sent if motions are implemented by this
     * agent..
     *
     * Notice: At least the "syn" effector has to be sent in every server cycle.
     * Look up "agent sync mode" for details.
     */
    private void act() {
        effOut.sendAgentMessage();
    }

    /**
     * Print logged informations.
     */
    private void printlog() {
        log.printLog();
    }
}

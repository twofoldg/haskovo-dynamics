/*******************************************************************************
* RoboNewbie
* NaoTeam Humboldt
* @author Hans-Dieter Burkhard 
* @version 1.1     Oct. 29th, 2016
*******************************************************************************/

package trainer;

import agentIO.PerceptorInput;
import util.GameStateConsts;
import util.Logger;


/**
 * This class implements a state machine for the control of experiments. There are
 * 2 examples for controlling experiments (to be choosen in case START) with 
 * - an attacker  (e.g. player 2 in Agent_SoccerTeam) resp.
 * - a goalkeeper (e.g. player 1 in Agent_SoccerTeam).
 * Other experiments can be implemented in a similar way. Thereby, the state machine 
 * should be independent from the game states (otherwise, the settings in class 
 * trainer must be taken into account).
 * 
 * The program can be extended with automatic evaluations and statistics of the 
 * results of experiments. Therewith, it could be used for Machine Learning.
 * 
 * THe initialization at the very beginning and the final calculations after all
 * experiments could be made in dependence of the variable experimentNumber. 
 * The evaluations of single experiments can be done inside the state 
 * machine. 
 * 
 */
public class TrainerThinking {

    private Logger log;
    private PerceptorInput percIn;
    private TrainerCommandExecutor trainer;
    
    private  int experimentNumber = 0;    // for counting experiments
    // you can srop experimenting after reaching a maximal number.
    private double serverTime, startExpTime; // for counting elapsed time 
    private double maxExpTime = 20; // time in seconds to run an experiment  

    private static enum ExperimentState {
        INIT, START, RUNNING, TIME_OVER, FINISHED
    };
    private static ExperimentState expState = ExperimentState.INIT;

    /**
     * Constructor.
     *
     * @param All passed object must be already initalized, and correctly
     * synchronized with the simulation server. None of the arguments can be
     * null.
     */
    public TrainerThinking(PerceptorInput percIn, TrainerCommandExecutor trainer, Logger log) {
        this.percIn = percIn;
        this.trainer = trainer;
        this.log = log;
    }


   /* 
    * The method controlExperiments is called from class Agent_Trainer in each cycle as long
    * as the game state remains "PlayOn". It controls the experiments one after each
    * other. The duration of aingle experiment is limited by maxExpTime.
    */
      
    public void controlExperiments() {

        serverTime = percIn.getServerTime();
        
//     System.out.println(serverTime +  "   expstate: " + expState);

        switch (expState) {
            case INIT: {
                prepareExperiment();
                expState = ExperimentState.START;
                break;
            }
            case START: {
                startExpTime = serverTime;
                experimentNumber++;
                /*
                 * there are different experiments, choose one of them
                 */
                startExperiment1();      // with attacker
                // startExperiment2();        // with goalkeeper
                expState = ExperimentState.RUNNING;
                break;
            }
            case RUNNING: {
                if (TimeOver()) {
                    expState = ExperimentState.TIME_OVER;
                }
                break;
            }
            case TIME_OVER: {
                finishExperiment(GameStateConsts.PlayMode.PlayOn);
                expState = ExperimentState.FINISHED;
                break;
            }
            case FINISHED: {
                // if something should be done here ...
                expState = ExperimentState.INIT;
                break;
            }
            default: {
                break;
            }
        }
    }

    /*
     * The method monitorGameStates is called from class Agent_Trainer at each
     * cycle in game state others than "KickOff" and "PlayOn". This is usually
     * the case when the server switches from "PlayOn" to another game state
     * like after goals, at corner kicks etc. It can be used to control players
     * during these game states (but be aware about the durations according to
     * the settings in class Agent_Trainer).
     */
    public void monitorGameStates(GameStateConsts.PlayMode playMode) {
        finishExperiment(playMode);
        trainer.dropBall();
        expState = ExperimentState.INIT;
    }

    private void prepareExperiment() {
        // Do something if needed, e.g. let player prepare for beaming
        // by return to initial position. 
        // But make sure that there is enough time to complete.
        // 
        // In case of automatic evaluations, the initialization for the 
        // experiment can be done here.
    }

    /*
     * Ball and players are beamed to the specified position. The beaming does
     * not stop an ongoing motion of a player. See class TrainerCommandExecutor
     * for details.
     */
    private void startExperiment1() {
        // here you start an experiment
        maxExpTime = 40;
        trainer.beamBall(2.3f, 0);
        trainer.moveRotatePlayer(TrainerCommandExecutor.Team.LEFT, 2, 2f, 0f, 0.375f, -90f);
        // moves player 2 (attacker) in Agent_SoccerTeam   
        // use other player number (second parameter in moveRotatePlayer(...) ) if needed.
    }

    private void startExperiment2() {
        // here you start an experiment
        float x_speed;
        float y_speed;
        float z_speed;

        maxExpTime = 20;

        x_speed = -8f;
        y_speed = -2f + ((float) experimentNumber) / 10f;
        z_speed = 0;
        // could use values from a file, from Machine Learning etc. 

        trainer.beamBall(0f, 0f, 0.05f, x_speed, y_speed, z_speed);
        // gives the ball a certain speed starting from the defined position
        trainer.moveRotatePlayer(TrainerCommandExecutor.Team.LEFT, 1, -4.5f, 0f, 0.375f, -90f);
        // moves player 1 (goalie) in Agent_SoccerTeam   
        // use other player number (second parameter) if needed
        }

    private boolean TimeOver() {
        return (serverTime - startExpTime > maxExpTime);
    }

    private void finishExperiment(GameStateConsts.PlayMode playMode) {  
        
//          System.out.println("finish at " + playMode);
          
        trainer.dropBall();   
        // prepare for next experiment: set gamestate to "play-on".   
        // In case of automatic evaluations, the calculation for an experment can be
        // done here. It might depend on the game state.
    }
}

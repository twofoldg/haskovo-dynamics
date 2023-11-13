/*******************************************************************************
 *  RoboNewbie
 * NaoTeam Humboldt
 * @author Monika Domanska
 * @version 1.1
 *******************************************************************************/

package examples;

import agentIO.EffectorOutput;
import agentIO.PerceptorInput;
import agentIO.ServerCommunication;
import util.Logger;
import util.RobotConsts;

/**
 * This agent shows basic concepts of using the RoboNewbie framework and gives
 * examples for interacting with the simulation server and using the classes
 * EffectorOutput and PerceptorInput.
 */
public class GoalKeeper_Agent {

    private Logger log;
    private PerceptorInput percIn;
    private EffectorOutput effOut;

    static final String id = "1";
    static final String team = "Haskovo_Red";

    static final double beamX =    5;
    static final double beamY =     0;
    static final double beamRot =   180;

    public void init() {

        // connection to the server
        ServerCommunication sc = new ServerCommunication();

        // internal agent classes
        log = new Logger();
        percIn = new PerceptorInput(sc);
        effOut = new EffectorOutput(sc);

        // simulated robot hardware on the soccer field
        sc.initRobot(id, team, beamX, beamY, beamRot);
    }

    public void run(int timeInSec) {
        // The server executes about 50 cycles per second.
        int cycles = timeInSec * 50;
        int cycles2sec = 100;

        double degree;

        for (int i = 0; i < cycles2sec; i++){
            sense();
            act();
        }


        // Loop synchronized with server.
        for (int i = 0; i < cycles; i++) {

            sense();

            degree = Math.toDegrees(percIn.getJoint(RobotConsts.LeftShoulderPitch));
            if (degree < 30) {
                effOut.setJointCommand(RobotConsts.LeftShoulderPitch,  1.0);
                effOut.setJointCommand(RobotConsts.RightShoulderPitch, 1.0);
            } else {
                effOut.setJointCommand(RobotConsts.LeftShoulderPitch, 0.0);
                effOut.setJointCommand(RobotConsts.RightShoulderPitch, 0.0);
            }

            log.log("reached angle: " + degree);


            // "Hardware" access to the effectors (simulated motors).
            act();
        }
    }

    private void sense() {
        // Receive the server message and parse it to get the perceptor values.
        percIn.update();
    }

    private void act(){
        // Send agent message with effector commands to the server.
        effOut.sendAgentMessage();
    }

    public void printlog() {
        log.printLog();
    }

}

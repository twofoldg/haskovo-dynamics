/**
 * *****************************************************************************
 * RoboNewbie NaoTeam Humboldt
 *
 * @author Hans-Dieter Burkhard
 * @version 1.1   Oct. 29th, 2016
 * 
 * Program is an adaptation of TrainerCommandExecutor from magmaOffenburg,
 * author Maximilian Kroeg, 2015.
******************************************************************************
 */
package trainer;

import agentIO.TrainerCommunication;
import util.GameStateConsts;

/**
 * Gives access to what a referee can do through the monitor. I.e. 
 * - place a player to a specified position, 
 * - place the ball to a specified position (and can give a speed in any direction),
 * - drop the ball to give access to all players (sets game state "PlayOn")
 * - change play modes.
 * It uses the monitor port 3200 (see agentIO.TrainerCommunication).
 * 
 * IMPORTANT: Because of the delay of messages by one cycle:
 * The results of commands will not apply (and not be observed) in the next cycle,
 * but one cycle later. 
 */
public class TrainerCommandExecutor {

    private TrainerCommunication connection;

    public TrainerCommandExecutor() {
        connection = new TrainerCommunication();
    }

    /**
     * Method to set the ball to specified position and speed.
     *
     * @param x position x on field
     * @param y position y on field
     * @param z position z on field
     * @param vx speed in x-direction
     * @param vy speed in y-direction
     * @param vz speed in z-direction
     *    
     *  use z = 0.042f for positions of ball on the ground 
     */
    public void beamBall(float x, float y, float z, float vx, float vy, float vz) {
        
        String msg = "(ball (pos " + x + " " + y + " " + z + ") (vel " + vx
					+ " " + vy + " " + vz + "))";   
        connection.sendTrainerMessage(msg);     
    }

    /**
     * Convenient-method to set the ball on ground to a specified position.
     *
     * @param x position x on field
     * @param y position y on field
     */
    public void beamBall(float x, float y) {
        beamBall(x, y, 0.042f, 0, 0, 0);
    }

    /**
     * Drops the ball at its current position and move all players away by the
     * free kick radius. After drop ball, all players have access to the ball.
     * If the ball is outside the field, it is brought back within bounds.
     */
    public void dropBall() {
        String msg = "(dropBall)";
        connection.sendTrainerMessage(msg);
    }

    /**
     * Start kickoff with any team (random?)
     */
    public void kickOff() {
        kickOff(Team.DONT_CARE);
    }

    /**
     *
     * @param k the team (as defined below) which gets the kickoff
     */
    public void kickOff(Team k) {
        String msg = "(kickOff " + k + ")";
        connection.sendTrainerMessage(msg);
    }

    /**
     * Method to move the player to a specified position
     *
     * @param team (as defined below)
     * @param playerNumber
     * @param x
     * @param y
     * @param z  
     * 
     * For placing the player exactly to the ground, use z = 0.375f 
     * (according to specification of torso center in the simspark wiki).
     * 
     * Beam works by moving the torso (center) upright to the desired position.
     * The positions of limbs relative to the torso are not changed, e.g. a sitting robot
     * will appear as sitting in the air (and then fall down). Moreover, ongoing motions
     * are simply continued at the new position. This leads to unpredictable behavior.
     * 
     * The keyframe motion ResetInitialPose can be used to stop and overwrite an
     * ongoing motion. It must be called and completed (!) before the beam command.
     * 
     * By their programs, players can be beamed to their initial pose after goals. 
     * If this is used, take care not to interfere with that by trainer commands.
     * 
     * 
     */
    public void movePlayer(Team team, int playerNumber, float x, float y, float z) {
        String msg = "(agent (unum " + playerNumber + ") (team " + team
                + ") (pos " + x + " " + y + " " + z + "))";
        connection.sendTrainerMessage(msg);
    }

    /**
     * Method to move the player to specified position with rotation. 
     * Cf. explanations from above.
     *
     * @param team (as defined below)
     * @param playerNumber
     * @param x
     * @param y
     * @param z
     * @param rot x, y, z are absolute values in field coordinates. That is,
     * (0,0) is the center of the field.
     *
     *  <rot> is in degrees, 
     *   0 pointing into y-direction, -90 in x-direction
     * 
     */
    public void moveRotatePlayer(Team team, int playerNumber, float x, float y,
            float z, float rot) {
        String msg = "(agent (unum " + playerNumber + ") (team " + team
                + ") (move " + x + " " + y + " " + z + " " + rot + "))";
        connection.sendTrainerMessage(msg);
    }

    /*
     * Method to set the game state (play mode). 
     * Where <playmode> is one of the predefined, case sensitive, play mode 
     * values as defined in enum PlayMode of class util.GameStateConsts. 
     * Possible playmodes are given as strings in the play_modes expression of
     * the init expression the monitor receives when it connects.
     * 
     * Note that game states can set constraints to the work of the server,
     * the players and the trainer. 
     * 
     * Hence not all trainer commands are available at every game state.
     * ToDo: which commands are available at which game states
     * 
     * If "PlayOn" can not be set, dropBall can be an alternative.
     * 
     * For the players: Actions can be restricted to certain game states by
     * the implementation. Should be checked before experiments are performed.
     * .
     */
    public void setPlayMode(GameStateConsts.PlayMode playmode) {
        String msg = "(playMode " + GameStateConsts.getPlayModeAsString(playmode) + " )";
        connection.sendTrainerMessage(msg);

    }

    public enum Team {

        LEFT("Left"), RIGHT("Right"), DONT_CARE("None");
        private String text;

        Team(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }
}

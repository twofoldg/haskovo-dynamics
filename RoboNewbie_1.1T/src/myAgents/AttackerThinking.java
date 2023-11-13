package myAgents;

import agentIO.PerceptorInput;
import directMotion.LookAroundMotion;
import keyframeMotion.KeyframeMotion;
import localFieldView.BallModel;
import localFieldView.GoalPostModel;
import localFieldView.LocalFieldView;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import util.FieldConsts;
import util.Logger;

import java.util.HashMap;

public class AttackerThinking {
    Logger log;
    PerceptorInput percIn;
    LocalFieldView localView;
    KeyframeMotion motion;
    BallModel ball;
    GoalPostModel oppGoalLPost, oppGoalRPost;
    private double lookTime;
    private boolean robotIsWalking;

    /**
     * Constructor.
     *
     * @param All passed object must be already initalized, and correctly synchronized with the simulation server. None of the arguments can be null.
     */
    public AttackerThinking(PerceptorInput percIn, LocalFieldView localView,
                              KeyframeMotion kfMotion, Logger log) {
        this.percIn = percIn;
        this.localView = localView;
        this.motion = kfMotion;
        //this.motion.setLogging(true);
        this.log = log;
        this.ball = this.localView.getBall();
        HashMap<FieldConsts.GoalPostID, GoalPostModel> goalPosts = this.localView.getGoals();
        this.oppGoalLPost = goalPosts.get(FieldConsts.GoalPostID.G1R);
        this.oppGoalRPost = goalPosts.get(FieldConsts.GoalPostID.G2R);
        this.lookTime = LookAroundMotion.LOOK_TIME;
        this.robotIsWalking = false;
    }

    public void decide() {
        double TOLLERATED_DEVIATION = Math.toRadians(6);
        double TOLLERATED_DISTANCE = 0.6; // in meters

        if (motion.ready()) {

            //log.log("new decision");
            double serverTime = percIn.getServerTime();

            // if the robot has fallen down
            if (percIn.getAcc().getZ() < 2) {
                if (percIn.getAcc().getY() > 0) {
                    motion.setStandUpFromBack();
                } else {
                    motion.setRollOverToBack();
                }
            } // if the robot has the actual ball coordinates
            else if ((serverTime - ball.getTimeStamp()) < lookTime) {

                Vector3D ballCoords = ball.getCoords();
//        log.log("2. robot has the actual ball coordinates, horizontal angle: "
//                + Math.toDegrees(ballCoords.getAlpha())
//                + " distance: " + ballCoords.getNorm()) ;

                // if the ball is not in front of the robot
                if (Math.abs(ballCoords.getAlpha()) > TOLLERATED_DEVIATION) {
//          log.log("3. the ball is not in front of the robot. ") ;
                    if (robotIsWalking) {
                        motion.setStopWalking_Plovdiv2014();
                        robotIsWalking = false;
                    } else {
                        if (ballCoords.getAlpha() > 0) {
                            motion.setTurnLeftSmall();
                        } else {
                            motion.setTurnRightSmall();
                        }
                    }
                } // if the robot is far away from the ball
                else if (ballCoords.getNorm() > TOLLERATED_DISTANCE) {
//          log.log("3. the robot is far away from the ball.");
                    motion.setWalkForward_Rijeka2013();
                    robotIsWalking = true;
                } // if the robot has the actual goal coordinates
                else if ((serverTime - oppGoalLPost.getTimeStamp() < lookTime)
                        && (serverTime - oppGoalRPost.getTimeStamp() < lookTime)) {
//          log.log("5. the robot has the actual goal coordinates");

                    // if the ball does not lie between the robot and the goal
                    if ((oppGoalLPost.getCoords().getAlpha() <= ballCoords.getAlpha())
                            || (oppGoalRPost.getCoords().getAlpha() >= ballCoords.getAlpha())) {
//            log.log("6. the ball does not lie between the robot and the goal");
                        if (robotIsWalking) {
                            motion.setStopWalking_Plovdiv2014();
                            robotIsWalking = false;
                        } else {
                            if (oppGoalLPost.getCoords().getAlpha() <= ballCoords.getAlpha()) {
                                motion.setSideStepLeft();
                            } else {
                                motion.setSideStepRight();
                            }
                        }
                    } // if the robot is in a good scoring position
                    else {
//            log.log("7. the robot is in a good scoring position");
                        motion.setKick_Rijeka2013();
                        robotIsWalking = true;
                    }
                } // if the robot cannot sense the goal coordinates from its actual position
                else {
//          log.log("5. goal coordinates missing");
                    motion.setTurnLeft();
                }
            } // if the robot cannot sense the ball coordinates from its actual position
            else {
                motion.setTurnLeft();
//        log.log("1. ball coordinates missing");
            }
        }
    }
}

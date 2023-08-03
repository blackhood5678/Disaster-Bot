package nl.saxion.ptbc.frog_pilot.Controllers;

import nl.saxion.ptbc.frog_pilot.Classes.Coordinate;
import nl.saxion.ptbc.frog_pilot.Controllers.ThePilot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutomaticDriving {

    //=============== Control Parameters ======================================
    //Rover's width (unit 1meter) : the rover is square.
    public double ROVER_WIDTH = 10;// calculated from jump test.
    //safe factor (unit 1meter) : the factor for bug's wall following
    public double SAFE_FACTOR = 3;
    // danger factor : the factor for the rover's safe navigation
    public double DANGER_FACTOR = 0.7;
    // goal factor : the factor for checking goal arrival
    public double GOAL_FACTOR = 1;
    //angle precision (unit 1degree) : degree tolerance limit
    public double ANGLE_PRECISION = 10;

    //============== Rover's action commands =========================================

    // turn_left command
    private String cmd_turn_left = "PILOT DRIVE 1000 -90 2";
    // turn right command
    private String cmd_turn_right = "PILOT DRIVE 1000 90 2";
    // go forward command
    private String cmd_forward = "PILOT DRIVE 1000 0 2";

    // go backward command
    private String cmd_backward = "PILOT DRIVE -1000 0 2";

    // you can define more action commands for detailed use.
    // or you can set command directly like:  thePilot.sasaServer.send("PILOT DRIVE -700 0 1");
    //    private String cmd_turn_left_back = "PILOT DRIVE -1000 -90 1";
    //    private String cmd_turn_right_back = "PILOT DRIVE -1000 90 1";

    //============== Navigation =========================================
    private List<Coordinate> collisions= new ArrayList<>();
    private Coordinate currentPosition;
    private Coordinate oldPosition;

    //============== Goal and arrival =========================================
    private Coordinate GOAL_POINT;
    private boolean fArrival = false;

    //============ Getters & Setters =======================================

    public List<Coordinate> getCollisions() {
        return collisions;
    }
    public void setCollision(Coordinate collisions) {
        this.collisions.add(collisions);
    }
    public void collisionsClear(){
        this.collisions.clear();
    }
    public Coordinate getCurrentPosition() {
        return currentPosition;
    }
    public void setCurrentPosition(Coordinate currentPosition) {
        this.currentPosition = currentPosition;
    }
    public void setOldPosition() {
        this.oldPosition = this.currentPosition;
    }

    //========================== Algorithm =======================================

    //Auto function
    //This function provides automatic drive
    //Caution: TimeUnit.SECONDS.sleep(n) function si often used for correct receiving rover and Obstacles' positions.
    public void Auto(Coordinate GOAL_POINT){
        this.GOAL_POINT=GOAL_POINT;
        while (true) {
            try{
                TimeUnit.SECONDS.sleep(3);
                Drive();
                if (IsArrived()){
                    break;
                }
            }catch ( InterruptedException e){
                System.out.println(e);
            }
        }
    }

    //IsArrived function
    // check if the goal reached.
    public boolean IsArrived(){
        return fArrival;
    }

    //getObsInDistance function
    //This function get the obstacle list in given distance.
    private List<Coordinate> getObsInDistance(List<Coordinate> collisions,  double distance) {
        List<Coordinate> obs;
        obs= collisions.stream().filter(coll ->
                (Math.abs((coll.getX()-currentPosition.getX())*(coll.getX()-currentPosition.getX())+(coll.getY()-currentPosition.getY())*(coll.getY()-currentPosition.getY())) <= distance*distance)// && Math.abs(coll.getZ())<10)
        ).collect(Collectors.toList());
        return obs;
    }

    //Drive function
    // run the rover for 1 action
    // this functions works after the rover's current position decided.
    // This function may excute several command
    // And this requires correct current position and obstacle data
    // So Sleep is required before or after the function excution.
    public void Drive(){

        if (currentPosition == null)
            return;

        if(coordDistance(currentPosition, GOAL_POINT) < GOAL_FACTOR * ROVER_WIDTH / 2){
            System.out.println("Arrived to goal, so Stop, (distance: " +coordDistance(currentPosition, GOAL_POINT) + ")");
            fArrival = true;
            return;
        };

        double danger_distance = DANGER_FACTOR * ROVER_WIDTH/2;
        double safe_distance = SAFE_FACTOR * ROVER_WIDTH/2;

        if (getObsInDistance(this.collisions, danger_distance).size()>0){

            List<Coordinate> obs = getObsInDistance(this.collisions, danger_distance);
            Coordinate danger_ob = closestCoordinate(obs);
            double angle = getAngle(currentPosition, danger_ob);

            double angleTmp = angle-currentPosition.getZ();
            if(angleTmp > 180)
                angleTmp = angleTmp - 360;
            if(angleTmp < -180)
                angleTmp = angleTmp + 360;

            String dc;
            if(Math.abs(angleTmp) > 90) {
                dc = "PILOT DRIVE 1000 0 2";
            }else{
                dc = "PILOT DRIVE -1000 0 2";
            }
            ThePilot.serverSendMessage(dc);
            ThePilot.addDriveCommandToPilotDBMissionLog(dc);

            System.out.println("action danger, size "+ getObsInDistance(this.collisions, danger_distance).size());
            for(int i= 0 ; i <obs.size(); i++ ){
                System.out.println("ob"+i+" "+obs.get(i).getX()+"|"+obs.get(i).getY()+"|"+obs.get(i).getZ()+"|");
            }
        } else if (getObsInDistance(this.collisions, safe_distance).size()>0) {
            avoidOb(closestCoordinate(getObsInDistance(this.collisions, safe_distance)), currentPosition);
            System.out.println("avoid obs, size "+ getObsInDistance(this.collisions, safe_distance).size());
            // turn to the 90 degree to the nearest.
            // go forward
        }else{
            System.out.println("go straight");
            //go straight to the goal.
            //turn to the goal
            goTo(GOAL_POINT, currentPosition);

            // go forward and forward
        }

    }

    private double coordDistance(Coordinate a, Coordinate b) {
        return Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }

    private Coordinate closestCoordinate(List<Coordinate> coordinates) {
        Coordinate closest = null;
        double dist = 0.0;
        for (Coordinate c : coordinates) {
            double d = coordDistance(currentPosition, c);
            if (closest == null) {
                closest = c;
                dist = d;
            } else if (d < dist)
                closest = c;
            dist = d;
        }
        System.out.println("curOB: "+ closest.getX() + "  " + closest.getY());
        return closest;
    }

    private double getAngle(Coordinate a, Coordinate b) {
        double rise = b.getY() - a.getY();
        double run = b.getX() - a.getX();

        double angle = Math.atan(rise/run);// * 180 / Math.PI;

        if (rise <0 && run <0)
            angle = - angle + Math.PI/2*3;
        if (rise < 0 && run > 0)
            angle = -angle+Math.PI/2;
        if ( rise > 0 && run < 0)
            angle = -angle + Math.PI/2*3;
        if ( rise >0 && run > 0)
            angle = Math.PI/2 - angle;

        angle = angle / Math.PI * 180;

        if (angle > 180)
            angle = -360 + angle;
        return angle;
    }

    // Drive to destination (only use when there are no obstacles in between).
    private void goTo(Coordinate destination, Coordinate currentPosition) {
        double angle = getAngle(currentPosition, destination);
        double distance = coordDistance(currentPosition, destination);
        System.out.println("Target: "+angle+ " current: "+ currentPosition.getZ()+"destination: "+destination.getX()+", "+destination.getY()+"distance:"+distance);


        double angleTmp = angle-currentPosition.getZ();
        if(angleTmp > 180)
            angleTmp = angleTmp - 360;
        if(angleTmp < -180)
            angleTmp = angleTmp + 360;
        if (angleTmp > ANGLE_PRECISION){
            ThePilot.serverSendMessage(cmd_turn_right);
            ThePilot.addDriveCommandToPilotDBMissionLog(cmd_turn_right);
            return;
        } else if (angleTmp < -ANGLE_PRECISION) {
            ThePilot.serverSendMessage(cmd_turn_left);
            ThePilot.addDriveCommandToPilotDBMissionLog(cmd_turn_left);
            return;
        }
        ThePilot.serverSendMessage(cmd_forward);
        ThePilot.addDriveCommandToPilotDBMissionLog(cmd_forward);
        return;
    }

    private void avoidOb(Coordinate destination, Coordinate currentPosition) {
        double angle = getAngle(currentPosition, destination);
        double distance = coordDistance(currentPosition, destination);

        double angleTmp = angle-currentPosition.getZ()-90;
        if(true){
            String dc = "PILOT DRIVE 1000 90 2";
            ThePilot.serverSendMessage(dc);
            ThePilot.addDriveCommandToPilotDBMissionLog(dc);
            try{
                TimeUnit.SECONDS.sleep(4);
                dc = "PILOT DRIVE 500 0 1";
                ThePilot.serverSendMessage(dc);
                ThePilot.addDriveCommandToPilotDBMissionLog(dc);
            }catch ( InterruptedException e){

            }
        }else{
            if(angleTmp > 180)
                angleTmp = angleTmp - 360;
            if(angleTmp < -180)
                angleTmp = angleTmp + 360;

            if (angleTmp > ANGLE_PRECISION){
                ThePilot.serverSendMessage(cmd_turn_right);
                ThePilot.addDriveCommandToPilotDBMissionLog(cmd_turn_right);
                return;
            } else if (angleTmp < -ANGLE_PRECISION) {
                ThePilot.serverSendMessage(cmd_turn_left);
                ThePilot.addDriveCommandToPilotDBMissionLog(cmd_turn_left);
                return;
            }
            if(distance > (SAFE_FACTOR*0.8)/2*ROVER_WIDTH/2){
                ThePilot.serverSendMessage("PILOT DRIVE 1000 0 2");
                ThePilot.addDriveCommandToPilotDBMissionLog("PILOT DRIVE 1000 0 2");
            }
            return;
        }
        return;
    }
}

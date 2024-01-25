package org.firstinspires.ftc.teamcode.commands;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.RunCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;

//import org.firstinspires.ftc.teamcode.Constants;
//import org.firstinspires.ftc.teamcode.auto.AutonomousController;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.auto.NewAutonomousController;

public class CommandManager {
    private final Robot robot;
    /** Command that opens the box release*/
    private final Command openBoxCommand;
    /** Command that opens the box release */
    private final Command closeBoxCommand;
    /** Default command for DriveSubsystem */
    private final Command defaultDriveCommand;
    private final Command resetGyroCommand;
//    /** Command for DriveSubsystem */
//    private final Command driveCommand;
    private final Command snapRightCommand;
    private final Command snapLeftCommand;
    private final Command snapUpCommand;
    private final Command snapDownCommand;
    private final Command defaultIntakeCommand;
    /** Command that moves the arm up and enters launching drone mode */
    private final Command droneModeCommand;
    /** Command that launches the drone */
    private final Command droneLaunchCommand;
    /** Command that exits drone launching mode */
    private final Command droneCancelCommand;
    /** Default command for ElbowSubsystem */
    private final Command defaultElbowCommand;
    /** Default command for LinearSlideSubsystem */
    private final Command defaultSlideCommand;
    /** Default command for WinchSubsystem */
    private final Command defaultWinchCommand;
    /** Command that lowers arm and intake and enters intake mode */
    private final Command intakeModeCommand;
    ///** Command that spits out pixels */
    //private final Command outtakeModeCommand;
    /** Command that exits intake mode */
    private final Command intakeCancelCommand;
    /** Command that picks pixels up and exits intake mode */
    private final Command pickupPixelsCommand;
    /** Command that moves the arm to low scoring position */
    private final Command highScoringPositionCommand;
    /** Command that moves the arm to medium scoring position */
    private final Command mediumScoringPositionCommand;
    /** Command that moves the arm to high scoring position */
    private final Command lowScoringPositionCommand;
    /** Command that moves the arm to home position */
    private final Command homePostionCommand;
    /** Command run at the start of driver controlled */
    private final Command setupCommand;

//    /** Command run at the start of autonomous */
//    private final Command autoSetupCommand;
//    private final Command autoDriveToSpikeMarksCommand;

    public CommandManager(Robot robot) {
        this.robot = robot;

        openBoxCommand = new SequentialCommandGroup(
                new InstantCommand(() -> robot.getBoxReleaseSubsystem().openBox(), robot.getBoxReleaseSubsystem()),
                new WaitCommand(1000),
                new InstantCommand(() -> robot.getBoxReleaseSubsystem().closeBox(), robot.getBoxReleaseSubsystem())
        );

        closeBoxCommand = new SequentialCommandGroup(
                new InstantCommand(() -> robot.getBoxReleaseSubsystem().closeBox(), robot.getBoxReleaseSubsystem())
        );

        defaultDriveCommand = new RunCommand(() -> robot.getDriveSubsystem().drive(robot.getDriverGamepad(), robot.isPressed(robot.getDriverGamepad().getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER)) ? 0.3 : 1.0), robot.getDriveSubsystem());

        resetGyroCommand = new InstantCommand(() -> robot.getDriveSubsystem().resetGyro());

        // Snap right
        snapRightCommand = new SnapCommand(robot.getDriveSubsystem(), robot.getDriverGamepad(), -90);
        // Snap left
        snapLeftCommand = new SnapCommand(robot.getDriveSubsystem(), robot.getDriverGamepad(), 90);
        // Snap up
        snapUpCommand = new SnapCommand(robot.getDriveSubsystem(), robot.getDriverGamepad(), 0);
        // Snap down
        snapDownCommand = new SnapCommand(robot.getDriveSubsystem(), robot.getDriverGamepad(), 180);

        droneModeCommand = new SequentialCommandGroup(
                new InstantCommand(() -> robot.setState(Robot.RobotState.SHOOTING_DRONE)),
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getDroneLaunchPosition())
        );

        droneLaunchCommand = new SequentialCommandGroup(
                new InstantCommand(() -> robot.setState(Robot.RobotState.DRIVING)),
                new InstantCommand(robot.getDroneSubsystem()::release, robot.getDroneSubsystem())
        );

        droneCancelCommand = new InstantCommand(() -> robot.setState(Robot.RobotState.DRIVING));

        // TODO: Add a command to outtake
        defaultIntakeCommand = new RunCommand(() -> {
            if (robot.getState() == Robot.RobotState.INTAKE || robot.getState() == Robot.RobotState.LOADING_PIXELS) {
                if (robot.getOperatorGamepad().getButton(GamepadKeys.Button.DPAD_UP)) robot.getIntakeSubsystem().outtake();
                else robot.getIntakeSubsystem().intake();
            }
            else robot.getIntakeSubsystem().stopMotor();
        });

        defaultElbowCommand = new RunCommand(() -> {
            if (robot.getOperatorGamepad().getLeftY() >= 0.1) robot.getElbowSubsystem().moveManually(1);
            else if (robot.getOperatorGamepad().getLeftY() <= -0.1) robot.getElbowSubsystem().moveManually(-1);
            else robot.getElbowSubsystem().stopMotor();
        }, robot.getElbowSubsystem());

        // TODO: Check this after
        defaultSlideCommand = new RunCommand(() -> {
            if (robot.getOperatorGamepad().getRightY() >= 0.1) robot.getLinearSlideSubsystem().moveManually(1);
            else if (robot.getOperatorGamepad().getRightY() <= -0.1) robot.getLinearSlideSubsystem().moveManually(-1);
            else robot.getLinearSlideSubsystem().stopMotor();
        }, robot.getLinearSlideSubsystem());


        // TODO: Who is supposed to use the winch???
        defaultWinchCommand = new RunCommand(() -> {
            if (robot.getOperatorGamepad().getButton(GamepadKeys.Button.A)) robot.getWinchSubsystem().winch();
            else if (robot.getOperatorGamepad().getButton(GamepadKeys.Button.B)) robot.getWinchSubsystem().unwinch();
            else robot.getWinchSubsystem().stopMotor();
        }, robot.getWinchSubsystem());

        intakeModeCommand = new SequentialCommandGroup(
                new InstantCommand(() -> robot.setState(Robot.RobotState.INTAKE)),
                new InstantCommand(robot.getBoxReleaseSubsystem()::closeBox, robot.getBoxReleaseSubsystem()),
                new InstantCommand(robot.getIntakeSubsystem()::downPosition, robot.getIntakeSubsystem()),
                new MoveSlideCommand(robot.getLinearSlideSubsystem(), robot.getLinearSlideSubsystem().getInPosition()),
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getIntakePosition()),
                new InstantCommand(robot.getIntakeSubsystem()::intake, robot.getIntakeSubsystem())
                // Start the intake to be redundant
        );

        // Emergency stop trying to pick up the pixels
        intakeCancelCommand = new InstantCommand(() -> {
            CommandScheduler.getInstance().cancel(getPickupPixelsCommand());
            robot.getIntakeSubsystem().stopMotor();
            robot.getIntakeSubsystem().upPosition();
            robot.setState(Robot.RobotState.DRIVING);
        });

        pickupPixelsCommand = new SequentialCommandGroup(
                // Set the scoring state to loading pixels
                new InstantCommand(() -> robot.setState(Robot.RobotState.LOADING_PIXELS)),
                new MoveSlideCommand(robot.getLinearSlideSubsystem(), robot.getLinearSlideSubsystem().getInPosition()),
                // Stop the intake and move it up
                new InstantCommand(robot.getIntakeSubsystem()::stopMotor, robot.getIntakeSubsystem()),
                new InstantCommand(robot.getIntakeSubsystem()::upPosition, robot.getIntakeSubsystem()),
                // Move the elbow to level position
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getLevelPosition()),
//              // Set the state to driving again
                new InstantCommand(() -> robot.setState(Robot.RobotState.DRIVING))
        );

        lowScoringPositionCommand = new SequentialCommandGroup(
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getLowScoringPosition()),
                new MoveSlideCommand(robot.getLinearSlideSubsystem(), robot.getLinearSlideSubsystem().getLowScoringPosition())
        );

        mediumScoringPositionCommand = new SequentialCommandGroup(
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getMediumScoringPosition()),
                new MoveSlideCommand(robot.getLinearSlideSubsystem(), robot.getLinearSlideSubsystem().getMediumScoringPosition())
        );

        highScoringPositionCommand = new SequentialCommandGroup(
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getHighScoringPosition()),
                new MoveSlideCommand(robot.getLinearSlideSubsystem(), robot.getLinearSlideSubsystem().getHighScoringPosition())
        );

        homePostionCommand = new SequentialCommandGroup(
                new MoveSlideCommand(robot.getLinearSlideSubsystem(), robot.getLinearSlideSubsystem().getInPosition()),
                new MoveElbowCommand(robot.getElbowSubsystem(), robot.getElbowSubsystem().getDrivingPosition())
        );

        setupCommand = new SequentialCommandGroup(
                new InstantCommand(robot.getDroneSubsystem()::startPosition, robot.getDroneSubsystem()),
                new InstantCommand(robot.getBoxReleaseSubsystem()::closeBox, robot.getBoxReleaseSubsystem())
        );
    }

    public Command getOpenBoxCommand() {
        return openBoxCommand;
    }

    public Command getCloseBoxCommand() {
        return closeBoxCommand;
    }

    public Command getDefaultDriveCommand() {
        return defaultDriveCommand;
    }

    public Command getResetGyroCommand() {
        return resetGyroCommand;
    }

//    public Command getDriveCommand() {
//        return driveCommand;
//    }

    public Command getSnapRightCommand() {
        return snapRightCommand;
    }

    public Command getSnapLeftCommand() {
        return snapLeftCommand;
    }

    public Command getSnapUpCommand() {
        return snapUpCommand;
    }

    public Command getSnapDownCommand() {
        return snapDownCommand;
    }

    public Command getDroneModeCommand() {
        return droneModeCommand;
    }

    public Command getDroneLaunchCommand() {
        return droneLaunchCommand;
    }

    public Command getDroneCancelCommand() {
        return droneCancelCommand;
    }

    public Command getDefaultElbowCommand() {
        return defaultElbowCommand;
    }

    public Command getDefaultSlideCommand () {
        return defaultSlideCommand;
    }

    public Command getDefaultWinchCommand () {
        return defaultWinchCommand;
    }

    public Command getIntakeModeCommand () {
        return intakeModeCommand;
    }

//    public Command getOuttakeModeCommand () {
//        return outtakeModeCommand;
//    }

    public Command getIntakeCancelCommand () {
        return intakeCancelCommand;
    }

    public Command getPickupPixelsCommand () {
        return pickupPixelsCommand;
    }

    public Command getLowScoringPositionCommand () {
        return lowScoringPositionCommand;
    }

    public Command getMediumScoringPositionCommand () {
        return mediumScoringPositionCommand;
    }

    public Command getHighScoringPositionCommand () {
        return highScoringPositionCommand;
    }

    public Command getHomePostionCommand () {
        return homePostionCommand;
    }

    public Command getSetupCommand() {
        return setupCommand;
    }

//    public Command getAutoSetupCommand() {
//        return autoSetupCommand;
//    }

//    public Command getAutoDriveToSpikeMarksCommand() {
//        return autoDriveToSpikeMarksCommand;
//    }

//    public Command getAutoDriveLeftCommand() {
//        return autoDriveLeftCommand;
//    }

//    public Command getAutoDriveMiddleCommand() {
//        return autoDriveMiddleCommand;
//    }

//    public Command getAutoDriveRightCommand() {
//        return autoDriveRightCommand;
//    }

    public Command getAutoSetupCommand() {
        return new AutoSetupCommand(robot.getDriveSubsystem(), robot.getIntakeSubsystem());
    }

    public Command getAutoDriveAndPlacePurpleCommand(NewAutonomousController.SpikeMark position, boolean blue) {
        return new AutoDriveAndPlacePurpleCommand(robot.getDriveSubsystem(), robot.getIntakeSubsystem(), position, blue);
    }

    public Command getAutoDriveFromPurpleCommand(NewAutonomousController.SpikeMark position, boolean blue, boolean placingYellow) {
        return new AutoDriveFromPurpleCommand(robot.getDriveSubsystem(), position, blue, placingYellow);
    }

    public Command getAutoPlaceYellowAndHideCommand(NewAutonomousController.SpikeMark position, boolean blue) {
        return new AutoPlaceYellowAndHideCommand(robot.getDriveSubsystem(), robot.getElbowSubsystem(), robot.getLinearSlideSubsystem(), robot.getBoxReleaseSubsystem(), position, blue);
    }
}

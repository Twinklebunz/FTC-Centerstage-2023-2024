package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.ToggleButtonReader;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Instructions.InstructionExecutor;
import org.firstinspires.ftc.teamcode.subsystems.ClawSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ElbowSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LinearSlideSubsystem;

public class Robot {
    // OpMode
    private final OpMode opMode;

    // Gamepads
    private final Gamepad driverGamepad;
    private final Gamepad operatorGamepad;

    //Define subsystems here.
    private final DriveSubsystem driveSubsystem;
    private final IntakeSubsystem intakeSubsystem;
    private final ElbowSubsystem elbowSubsystem;
    private final ClawSubsystem clawSubsystem;
    private final LinearSlideSubsystem linearSlideSubsystem;

    private final InstructionExecutor instructionExecutor;

    private boolean usingPIDControllers;

    enum DriveState {
        DRIVER_CONTROLLED,
        SNAPPING,
        DETECTING_TAG,
        CENTERING_TAG,
        STEPPING_LEFT,
        STEPPING_RIGHT
    }

    enum ScoringState {
        STARTING,
        INTAKE,
        LOWERING_CLAW,
        WAITING,
        RAISING_CLAW,
        LOADED_DRIVING,
        SCORING,
        DRIVING,
        MANUAL
    }

    ElapsedTime timer;

    private DriveState driveState = DriveState.DRIVER_CONTROLLED;
    private ScoringState scoringState = ScoringState.STARTING;

    public Robot(OpMode opMode, boolean usingPID) {
        this.opMode = opMode;

        this.usingPIDControllers = usingPID;
        if (!usingPID) scoringState = ScoringState.MANUAL;

        // Initialize the gamepads
        driverGamepad = opMode.gamepad1;
        operatorGamepad = opMode.gamepad2;

        // Initialize the subsystems
        driveSubsystem = new DriveSubsystem(opMode.hardwareMap);
        intakeSubsystem = new IntakeSubsystem(opMode.hardwareMap);
        elbowSubsystem = new ElbowSubsystem(opMode.hardwareMap);
        clawSubsystem = new ClawSubsystem(opMode.hardwareMap);
        linearSlideSubsystem = new LinearSlideSubsystem(opMode.hardwareMap);

        instructionExecutor = new InstructionExecutor();
    }

    public Robot(OpMode opMode) {
        this(opMode, true);
    }

    // Perform actions that happen when the robot starts
    public void start() {
        linearSlideSubsystem.retract();
    }

    // Main robot control
    public void run() {
        // Control all of the subsystems

        // Drive subsystem controls (driver):
        // Left joystick up and down moves the robot forward and back
        // Left joystick left and right moves the robot left and right
        // Right joystick left and right turns the robot left and right
        driveLoop();

        // FOR DRIVERS:
        // Press A to enter intake mode
        // While in intake mode press X to pick up
        // Press B to enter driving mode


        if (operatorGamepad.a) scoringState = ScoringState.INTAKE;
        if (operatorGamepad.b) scoringState = ScoringState.LOADED_DRIVING;
//        if (operatorGamepad.b) scoringState = ScoringState.DRIVING;

        if (operatorGamepad.dpad_up) elbowSubsystem.raiseManually();
        else if (operatorGamepad.dpad_down) elbowSubsystem.lowerManually();
        else elbowSubsystem.stop();

        if (operatorGamepad.dpad_right) linearSlideSubsystem.extendManually();
        else if (operatorGamepad.dpad_left) linearSlideSubsystem.retractManually();
        else linearSlideSubsystem.stop();

        if (operatorGamepad.y) clawSubsystem.openClaw();

        scoringLoop();
//        runPIDControllers();

        opMode.telemetry.addData("Scoring State: ", scoringState);

        // Instruction controls (driver):
        // Left trigger adds a left align
        // Right trigger adds a right align
        // Both triggers add a center align
        // Left bumper adds a step left
        // Right bumper adds a step right
        // A enters the instructions
        // B cancels the instructions
//        if (isPressed(driverGamepad.left_trigger) && isPressed(driverGamepad.right_trigger))
//            instructionExecutor.addInstruction(this::alignCenter);
//        else if (isPressed(driverGamepad.left_trigger))
//            instructionExecutor.addInstruction(this::alignLeft);
//        else if (isPressed(driverGamepad.right_trigger))
//            instructionExecutor.addInstruction(this::alignRight);
//        if (driverGamepad.left_bumper) instructionExecutor.addInstruction(this::stepLeft);
//        if (driverGamepad.right_bumper) instructionExecutor.addInstruction(this::stepRight);
//        if (driverGamepad.a) instructionExecutor.executeInstructions();
//        if (driverGamepad.b) instructionExecutor.clearInstructions();

        opMode.telemetry.update();
    }

    private void driveLoop() {
        switch (driveState) {
            case DRIVER_CONTROLLED:
                driveSubsystem.drive(driverGamepad.left_stick_y, -driverGamepad.left_stick_x, -driverGamepad.right_stick_x);
                if (driverGamepad.a) {
//                    driveSubsystem.autoSnap();
//                    driveState = DriveState.SNAPPING;
                }
                break;
            case SNAPPING:
                if (driveSubsystem.isFinishedSnapping()) {
                    driveState = DriveState.DETECTING_TAG;
                }
                break;
            case DETECTING_TAG:
                break;
            case CENTERING_TAG:
                if (driveSubsystem.isCentered()) {
                    instructionExecutor.getNextInstruction();
                }
                break;
            case STEPPING_LEFT:
                if (driveSubsystem.isFinishedSteppingLeft()) {
                    driveState = DriveState.DRIVER_CONTROLLED;
                }
                break;
            case STEPPING_RIGHT:
                if (driveSubsystem.isFinishedSteppingRight()) {
                    driveState = DriveState.DRIVER_CONTROLLED;
                }
                break;
            default:
                driveState = DriveState.DRIVER_CONTROLLED;
        }
    }

    public void scoringLoop() {
        switch (scoringState) {
            case STARTING:
                intakeSubsystem.mediumPosition();
                clawSubsystem.openClaw();
//                linearSlideSubsystem.retract();
                intakeSubsystem.stop();
                runPIDControllers();
                break;
            case INTAKE:
                intakeSubsystem.downPosition();
                intakeSubsystem.intake();
                clawSubsystem.openClaw();
                linearSlideSubsystem.retract();
                elbowSubsystem.drivingPosition();
                runPIDControllers();
                if (operatorGamepad.x) scoringState = ScoringState.LOWERING_CLAW;
                break;
            case LOWERING_CLAW:
                elbowSubsystem.intakePosition();
                runPIDControllers();
                if (elbowSubsystem.isAtTarget()) {
                    elbowSubsystem.stop();
                    clawSubsystem.closeClaw();
                    timer = new ElapsedTime();
                    scoringState = ScoringState.WAITING;
                }
                break;
            case WAITING:
//                if (timer.milliseconds() >= 500) scoringState = ScoringState.RAISING_CLAW;
                if (timer.milliseconds() >= 500) {
                    clawSubsystem.closeClaw();
                    intakeSubsystem.stop();
                    intakeSubsystem.mediumPosition();
                    elbowSubsystem.levelPosition();
                    scoringState = ScoringState.RAISING_CLAW;
                }
                break;
            case RAISING_CLAW:
                runPIDControllers();
                if (elbowSubsystem.isAtTarget()) {
                    scoringState = ScoringState.LOADED_DRIVING;
                }
                break;
            case LOADED_DRIVING:
//                clawSubsystem.closeClaw();
                intakeSubsystem.mediumPosition();
//                elbowSubsystem.levelPosition();
                intakeSubsystem.stop();
//                runPIDControllers();
                break;
            case SCORING:
                break;
            case DRIVING:
                intakeSubsystem.stop();
                intakeSubsystem.mediumPosition();
                linearSlideSubsystem.retract();
                elbowSubsystem.drivingPosition();
                runPIDControllers();
                break;
            case MANUAL:
                runManually();
                break;
        }
    }

    public void runManually() {

        driveLoop();

        // Elbow Subsystem
        if (operatorGamepad.x) elbowSubsystem.raiseManually();
        else if (operatorGamepad.y) elbowSubsystem.lowerManually();
        else elbowSubsystem.stop();

        // Intake Subsystem
        if (operatorGamepad.dpad_up) intakeSubsystem.mediumPosition();
        if (operatorGamepad.dpad_down) intakeSubsystem.downPosition();
        if (operatorGamepad.dpad_left) intakeSubsystem.intake();
        else if (operatorGamepad.dpad_right) intakeSubsystem.outtake();
        else intakeSubsystem.stop();

        // Linear Slide Subsystem
        if (operatorGamepad.right_bumper) linearSlideSubsystem.extendManually();
        else if (operatorGamepad.left_bumper) linearSlideSubsystem.retractManually();
        else linearSlideSubsystem.stop();

        // Claw Subsystem
        if (operatorGamepad.a) clawSubsystem.openClaw();
        if (operatorGamepad.b) clawSubsystem.closeClaw();

    }

    public void runPIDControllers() {
        if (!usingPIDControllers) return;
        elbowSubsystem.runPID();
        linearSlideSubsystem.runPID();
    }

    public void intakePosition() {
        elbowSubsystem.intakePosition();
        linearSlideSubsystem.retract();
        clawSubsystem.openClaw();
    }

    private void alignLeft() {

    }

    private void alignCenter() {

    }

    private void alignRight() {

    }

    private void stepLeft() {
        driveSubsystem.halfStepLeft();
        driveState = DriveState.STEPPING_LEFT;
    }

    private void stepRight() {
        driveSubsystem.halfStepRight();
        driveState = DriveState.STEPPING_RIGHT;
    }

    private boolean isPressed(float controllerInput) {
        return Math.abs(controllerInput) >= Constants.DriveConstants.DEADZONE;
    }

    private void wait(int ms) {
        ElapsedTime timer = new ElapsedTime();
        while (timer.milliseconds() <= ms) {}
    }
}

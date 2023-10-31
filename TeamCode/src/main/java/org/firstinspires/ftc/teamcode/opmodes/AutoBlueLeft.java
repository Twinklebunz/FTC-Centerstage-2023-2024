package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.auto.AutonomousController;

@Autonomous(group = "auto")
public class AutoBlueLeft extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        AutonomousController autonomousController = new AutonomousController(hardwareMap, "blueLeft", telemetry);
        waitForStart();
        if (isStopRequested()) return;
        autonomousController.run();
    }
}

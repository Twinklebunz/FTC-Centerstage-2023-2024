package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.util.PIDController;

//This class holds all constants, the reason we do this is so we can import all the constants 'statically.'
// i.e.: To import the drive constants, you would add
// import static org.firstinspires.ftc.teamcode.Constants.DriveConstants.*;

public class Constants {
    public static class DriveConstants {
        /* Define all driving constants here i.e:
            - configuration names
            - PID values(gain)
            - booleans(USE_FIELD_CENTRIC)
            - limits(MAX_SPEED_M_S)
        */

        public static float DEADZONE = 0;

        public static float INPUT_MULTIPLIER = 0.8f;

        public static String FRONT_LEFT_MOTOR_NAME = " ";
        public static String FRONT_RIGHT_MOTOR_NAME = " ";
        public static String REAR_LEFT_MOTOR_NAME = " ";
        public static String REAR_RIGHT_MOTOR_NAME = " ";

        public static DcMotorSimple.Direction FRONT_LEFT_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
        public static DcMotorSimple.Direction FRONT_RIGHT_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
        public static DcMotorSimple.Direction REAR_LEFT_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
        public static DcMotorSimple.Direction REAR_RIGHT_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;

        public static String IMU_NAME = "imu";

        public static boolean FIELD_CENTRIC = false;
        public static boolean SCALED = false;

        public static float AUTO_DRIVE_SPEED;
        public static float AUTO_STRAFE_SPEED;
        public static float TURN_SPEED;
    }

    public static class ArmConstants {
        public static String CLAW1_SERVO_MOTOR_NAME = " ";
        public static String CLAW2_SERVO_MOTOR_NAME = " ";

        public static double OPEN_POSITION = -1;
        public static double CLOSE_POSITION = -1;
    }

    public static class IntakeConstants {
        public static String INTAKE_SERVO_NAME = " ";
        public static String INTAKE_MOTOR_NAME = " ";
        public static DcMotorSimple.Direction INTAKE_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;

        public static double INTAKE_DOWN_POSITION;
        public static double INTAKE_UP_POSITION;
        public static double INTAKE_SPEED = 0;
        public static double OUTTAKE_SPEED = 0;
    }

    public static class LinearSlideConstants {
        public static String SLIDE_MOTOR_NAME = " ";
        public static DcMotorSimple.Direction SLIDE_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;

        public static double EXTEND_POWER;
        public static double RETRACT_POWER;

        public static final double PULSES_PER_MOTOR_REV = -1;
        public static final double WHEEL_DIAMETER = -1;
        public static final double GEAR_RATIO = -1;
        public static final double PULSES_PER_INCH = (PULSES_PER_MOTOR_REV * GEAR_RATIO) / (WHEEL_DIAMETER * Math.PI);

        public static final PIDController SLIDE_PID_CONTROLLER = new PIDController(-1, 0, 0);
        public static final double TARGET_TOLERANCE = 1.0;


    }
}

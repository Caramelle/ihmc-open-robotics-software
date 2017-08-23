package us.ihmc.atlas.StepAdjustmentVisualizers;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.atlas.parameters.AtlasSimpleICPOptimizationParameters;
import us.ihmc.atlas.parameters.AtlasWalkingControllerParameters;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters;
import us.ihmc.commonWalkingControlModules.instantaneousCapturePoint.icpOptimization.ICPOptimizationParameters;
import us.ihmc.commons.PrintTools;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.robotics.controllers.ControllerFailureException;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.stateMachines.conditionBasedStateMachine.StateTransitionCondition;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner;

public class AtlasSimpleStepAdjustmentDemo
{
   private static StepScriptType stepScriptType = StepScriptType.FORWARD_FAST;
   private static TestType testType = TestType.BIG_ADJUSTMENT;
   private static PushDirection pushDirection = PushDirection.FORWARD_IN_45;

   private static String forwardFastScript = "scripts/stepAdjustment_forwardWalkingFast.xml";
   private static String forwardSlowScript = "scripts/stepAdjustment_forwardWalkingSlow.xml";
   private static String stationaryFastScript = "scripts/stepAdjustment_stationaryWalkingFast.xml";
   private static String stationarySlowScript = "scripts/stepAdjustment_stationaryWalkingSlow.xml";

   private static double simulationTime = 15.0;

   private final StepAdjustmentDemoHelper stepAdjustmentDemo;

   public AtlasSimpleStepAdjustmentDemo(StepScriptType stepScriptType, TestType testType, PushDirection pushDirection)
   {
      TestRobotModel robotModel = new TestRobotModel(testType);

      String script;
      Vector3D forceDirection;
      double percentWeight;

      switch (pushDirection)
      {
      case FORWARD:
         forceDirection = new Vector3D(1.0, 0.0, 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.96;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.49;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.56;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.44;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.92;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.63;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.47;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.47;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.73;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.65;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.48;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.47;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.25;
               break;
            case ADJUSTMENT_WITH_ANGULAR:
               percentWeight = 1.11;
               break;
            case SPEED_UP_ADJUSTMENT_WITH_ANGULAR:
               percentWeight = 1.11;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.83;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.62;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.39;
               break;
            }
            break;
         }
         break;
      case BACKWARD:
         forceDirection = new Vector3D(-1.0, 0.0, 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.9;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.68;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.65;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.65;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.27;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.06;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.48;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.48;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.66;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.55;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.48;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.48;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.85;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.54;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.86;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.86;
               break;
            }
            break;
         }
         break;
      case FORWARD_45:
         double angle = Math.PI / 4.0;
         forceDirection = new Vector3D(Math.cos(angle), -Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.98;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.41;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.69;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.34;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.76;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.33;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.55;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.32;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.17;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.42;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.68;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.33;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.47;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.92;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.46;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.3;
               break;
            }
            break;
         }
         break;
      case FORWARD_60:
         angle = Math.PI / 3.0;
         forceDirection = new Vector3D(Math.cos(angle), -Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.9;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.35;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.54;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.28;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.72;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.96;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.46;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.27;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.15;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.32;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.59;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.28;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.35;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.94;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.39;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.25;
               break;
            }
            break;
         }
         break;
      case BACKWARD_45:
         angle = Math.PI / 4.0;
         forceDirection = new Vector3D(-Math.cos(angle), -Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.82;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.47;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.47;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.38;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.81;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.65;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.6;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.36;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.14;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.54;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.61;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.37;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.46;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.12;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.48;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.33;
               break;
            }
            break;
         }
         break;
      case FORWARD_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(Math.cos(angle), -Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.1;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.49;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.90;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.45;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.94;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.41;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.57;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.46;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.10;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.56;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.55;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.44;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.52;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.9;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.62;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.41;
               break;
            }
            break;
         }
         break;
      case INWARD:
         forceDirection = new Vector3D(0.0, 1.0, 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.24;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.24;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.24;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.24;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.52;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.49;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.57;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.57;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.25;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.26;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.26;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.26;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.43;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.42;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.3;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.31;
               break;
            }
            break;
         }
         break;
      case FORWARD_IN_45:
         angle = Math.PI / 4.0;
         forceDirection = new Vector3D(Math.cos(angle), Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.32;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.32;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.32;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.32;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.62;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.61;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.62;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.62;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.33;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.33;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.33;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.33;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.54;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.54;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.3;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.31;
               break;
            }
            break;
         }
         break;
      case FORWARD_IN_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(Math.cos(angle), Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.36;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.39;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.38;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.39;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.75;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.8;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.51;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.51;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.47;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.47;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.43;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.43;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.70;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.72;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.33;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.34;
               break;
            }
            break;
         }
         break;
      case BACKWARD_60:
         angle = Math.PI / 3.0;
         forceDirection = new Vector3D(-Math.cos(angle), -Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.0;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.39;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.45;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.30;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.68;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.30;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.47;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.29;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.19;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.42;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.62;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.3;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.56;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.95;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.43;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.26;
               break;
            }
            break;
         }
         break;
      case BACKWARD_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(-Math.cos(angle), -Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.83;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.58;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.54;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.53;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.43;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.06;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.52;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.51;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.85;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.62;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.53;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.53;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.95;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.45;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.52;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.46;
               break;
            }
            break;
         }
         break;
      case BACKWARD_IN_45:
         angle = Math.PI / 4.0;
         forceDirection = new Vector3D(-Math.cos(angle), Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.34;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.36;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.38;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.38;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.77;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.75;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.71;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.71;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.37;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.39;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.39;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.39;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.69;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.62;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.5;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.55;
               break;
            }
            break;
         }
         break;
      case BACKWARD_IN_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(-Math.cos(angle), Math.sin(angle), 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.51;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.53;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.56;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.57;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.83;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.86;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.57;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.58;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.51;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.52;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.55;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.55;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.04;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 1.01;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.77;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.83;
               break;
            }
            break;
         }
         break;
      default: // OUTWARD
         forceDirection = new Vector3D(0.0, -1.0, 0.0);

         switch (stepScriptType)
         {
         case FORWARD_SLOW:
            script = forwardSlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.76;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.31;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.47;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.25;
               break;
            }
            break;
         case STATIONARY_FAST:
            script = stationaryFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.72;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.85;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.41;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.26;
               break;
            }
            break;
         case STATIONARY_SLOW:
            script = stationarySlowScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 0.97;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.29; //// TODO: 2/12/17
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.52;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.25;
               break;
            }
            break;
         default: // FORWARD_FAST
            script = forwardFastScript;

            switch (testType)
            {
            case BIG_ADJUSTMENT:
               percentWeight = 1.42;
               break;
            case ADJUSTMENT_ONLY:
               percentWeight = 0.83;
               break;
            case SPEED_UP_ONLY:
               percentWeight = 0.36;
               break;
            default: // doesn't allow speed up or step adjustment
               percentWeight = 0.22;
               break;
            }
            break;
         }
         break;
      }

      stepAdjustmentDemo = new StepAdjustmentDemoHelper(robotModel, script);

      // push parameters:

      double swingTime = stepAdjustmentDemo.getSwingTime();
      double delay = 0.5 * swingTime;
      double weight = stepAdjustmentDemo.getTotalMass() * 9.81;
      double magnitude = percentWeight * weight;
      double duration = 0.1 * swingTime;
      PrintTools.info("Testing step type " + stepScriptType + ", test type " + testType + ", and push direction " + pushDirection + " at weight percent " + percentWeight + ".");

      // push timing:
      StateTransitionCondition pushCondition = stepAdjustmentDemo.getSingleSupportStartCondition(RobotSide.RIGHT);
      stepAdjustmentDemo.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);
   }


   public AtlasSimpleStepAdjustmentDemo(StepScriptType stepScriptType, TestType testType, PushDirection pushDirection, double percentWeight,
                                        boolean showGui)
   {
      AtlasRobotModel robotModel = new TestRobotModel(testType);
      String script;
      switch (stepScriptType)
      {
      case FORWARD_FAST:
         script = forwardFastScript;
         break;
      case FORWARD_SLOW:
         script = forwardSlowScript;
         break;
      case STATIONARY_FAST:
         script = stationaryFastScript;
         break;
      default:
         script = stationarySlowScript;
         break;
      }

      Vector3D forceDirection;
      switch (pushDirection)
      {
      case INWARD:
         forceDirection = new Vector3D(0.0, 1.0, 0.0);
         break;
      case FORWARD_IN_45:
         double angle = Math.PI / 4.0;
         forceDirection = new Vector3D(Math.cos(angle), Math.sin(angle), 0.0);
         break;
      case FORWARD_IN_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(Math.cos(angle), Math.sin(angle), 0.0);
         break;
      case FORWARD:
         forceDirection = new Vector3D(1.0, 0.0, 0.0);
         break;
      case FORWARD_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(Math.cos(angle), -Math.sin(angle), 0.0);
         break;
      case FORWARD_45:
         angle = Math.PI / 4.0;
         forceDirection = new Vector3D(Math.cos(angle), -Math.sin(angle), 0.0);
         break;
      case FORWARD_60:
         angle = Math.PI / 3.0;
         forceDirection = new Vector3D(Math.cos(angle), -Math.sin(angle), 0.0);
         break;
      case OUTWARD:
         forceDirection = new Vector3D(0.0, -1.0, 0.0);
         break;
      case BACKWARD_60:
         angle = Math.PI / 3.0;
         forceDirection = new Vector3D(-Math.cos(angle), -Math.sin(angle), 0.0);
         break;
      case BACKWARD_45:
         angle = Math.PI / 4.0;
         forceDirection = new Vector3D(-Math.cos(angle), -Math.sin(angle), 0.0);
         break;
      case BACKWARD_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(-Math.cos(angle), -Math.sin(angle), 0.0);
         break;
      case BACKWARD:
         forceDirection = new Vector3D(-1.0, 0.0, 0.0);
         break;
      case BACKWARD_IN_30:
         angle = Math.PI / 6.0;
         forceDirection = new Vector3D(-Math.cos(angle), Math.sin(angle), 0.0);
         break;
      default: // BACKWARD_IN_45
         angle = Math.PI / 4.0;
         forceDirection = new Vector3D(-Math.cos(angle), Math.sin(angle), 0.0);
         break;
      }

      stepAdjustmentDemo = new StepAdjustmentDemoHelper(robotModel, script, showGui);

      // push parameters:

      double swingTime = stepAdjustmentDemo.getSwingTime();
      double delay = 0.5 * swingTime;
      double weight = stepAdjustmentDemo.getTotalMass() * 9.81;
      double magnitude = percentWeight * weight;
      double duration = 0.1 * swingTime;
      PrintTools.info("Testing step type " + stepScriptType + ", test type " + testType + ", and push direction " + pushDirection + " at weight percent " + percentWeight + ".");

      // push timing:
      StateTransitionCondition pushCondition = stepAdjustmentDemo.getSingleSupportStartCondition(RobotSide.RIGHT);
      stepAdjustmentDemo.applyForceDelayed(pushCondition, delay, forceDirection, magnitude, duration);
   }

   public void simulateAndBlock(double simulationTime) throws BlockingSimulationRunner.SimulationExceededMaximumTimeException, ControllerFailureException
   {
      stepAdjustmentDemo.simulateAndBlock(simulationTime);
   }

   public void destroySimulation()
   {
      stepAdjustmentDemo.destroySimulation();
   }

   public static void main(String[] args)
   {
      AtlasSimpleStepAdjustmentDemo demo = new AtlasSimpleStepAdjustmentDemo(stepScriptType, testType, pushDirection);

      try
      {
         demo.simulateAndBlock(simulationTime);
      }
      catch (Exception e)
      {
      }
   }

   private class TestRobotModel extends AtlasRobotModel
   {
      private final TestType testType;
      private TestRobotModel(TestType testType)
      {
         super(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, RobotTarget.SCS, false);

         this.testType = testType;
      }

      @Override
      public WalkingControllerParameters getWalkingControllerParameters()
      {
         return new AtlasWalkingControllerParameters(RobotTarget.SCS, getJointMap(), getContactPointParameters())
         {
            @Override
            public boolean allowDisturbanceRecoveryBySpeedingUpSwing()
            {
               switch (testType)
               {
               case FEEDBACK_ONLY:
               case FEEDBACK_WITH_ANGULAR:
               case ADJUSTMENT_ONLY:
               case ADJUSTMENT_WITH_ANGULAR:
               case TIMING:
               case TIMING_WITH_ANGULAR:
                  return false;
               default:
                  return true;
               }
            }

            @Override
            public boolean useOptimizationBasedICPController()
            {
               switch (testType)
               {
               case FEEDBACK_ONLY:
               case SPEED_UP_ONLY:
                  return false;
               default:
                  return true;
               }
            }

            @Override
            public double getMinimumSwingTimeForDisturbanceRecovery()
            {
               return 0.4;
            }

            @Override
            public ICPOptimizationParameters getICPOptimizationParameters()
            {
               return new AtlasSimpleICPOptimizationParameters(false)
               {
                  public boolean useAngularMomentum()
                  {
                     switch (testType)
                     {
                     case FEEDBACK_WITH_ANGULAR:
                     case ADJUSTMENT_WITH_ANGULAR:
                     case SPEED_UP_ADJUSTMENT_WITH_ANGULAR:
                     case TIMING_WITH_ANGULAR:
                        return true;
                     default:
                        return false;
                     }
                  }

                  public boolean useStepAdjustment()
                  {
                     if (testType == TestType.FEEDBACK_WITH_ANGULAR)
                        return false;
                     else
                        return true;
                  }

                  public double getDynamicRelaxationWeight()
                  {
                     switch (testType)
                     {
                     case FEEDBACK_WITH_ANGULAR:
                     case ADJUSTMENT_WITH_ANGULAR:
                     case SPEED_UP_ADJUSTMENT_WITH_ANGULAR:
                     case TIMING_WITH_ANGULAR:
                        return 100000.0;
                     default:
                        return 1000.0;
                     }
                  }

                  /** {@inheritDoc} */
                  @Override
                  public double getDynamicRelaxationDoubleSupportWeightModifier()
                  {
                     switch (testType)
                     {
                     case FEEDBACK_WITH_ANGULAR:
                     case ADJUSTMENT_WITH_ANGULAR:
                     case SPEED_UP_ADJUSTMENT_WITH_ANGULAR:
                     case TIMING_WITH_ANGULAR:
                        return 100.0;
                     default:
                        return 4.0;
                     }
                  }
               };
            }

         };
      }
   }

}
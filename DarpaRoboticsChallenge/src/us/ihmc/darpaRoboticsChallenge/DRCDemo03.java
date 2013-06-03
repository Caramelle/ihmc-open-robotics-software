package us.ihmc.darpaRoboticsChallenge;

import java.util.List;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point2d;

import us.ihmc.SdfLoader.SDFRobot;
import us.ihmc.commonWalkingControlModules.automaticSimulationRunner.AutomaticSimulationRunner;
import us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters;
import us.ihmc.commonWalkingControlModules.controllers.ControllerFactory;
import us.ihmc.commonWalkingControlModules.highLevelHumanoidControl.highLevelStates.HighLevelState;
import us.ihmc.darpaRoboticsChallenge.drcRobot.DRCRobotJointMap;
import us.ihmc.darpaRoboticsChallenge.drcRobot.DRCRobotParameters;
import us.ihmc.darpaRoboticsChallenge.drcRobot.PlainDRCRobot;
import us.ihmc.darpaRoboticsChallenge.initialSetup.DrivingDRCRobotInitialSetup;
import us.ihmc.projectM.R2Sim02.initialSetup.RobotInitialSetup;
import us.ihmc.robotSide.RobotSide;
import us.ihmc.robotSide.SideDependentList;

import com.martiansoftware.jsap.JSAPException;
import com.yobotics.simulationconstructionset.SimulationConstructionSet;
import com.yobotics.simulationconstructionset.util.graphics.DynamicGraphicObjectsListRegistry;
import com.yobotics.simulationconstructionset.util.math.functionGenerator.YoFunctionGeneratorMode;

public class DRCDemo03
{
   private final HumanoidRobotSimulation<SDFRobot> drcSimulation;
   private final DRCDemoEnvironmentWithBoxAndSteeringWheel environment;

   public DRCDemo03(DRCGuiInitialSetup guiInitialSetup, AutomaticSimulationRunner automaticSimulationRunner, double timePerRecordTick,
                    int simulationDataBufferSize)
   {
      DRCSCSInitialSetup scsInitialSetup;
      RobotInitialSetup<SDFRobot> robotInitialSetup = new DrivingDRCRobotInitialSetup();
      DynamicGraphicObjectsListRegistry dynamicGraphicObjectsListRegistry = new DynamicGraphicObjectsListRegistry();

      environment = new DRCDemoEnvironmentWithBoxAndSteeringWheel(dynamicGraphicObjectsListRegistry);
      final PlainDRCRobot robotInterface = new PlainDRCRobot(DRCRobotModel.ATLAS_SANDIA_HANDS, true);
      scsInitialSetup = new DRCSCSInitialSetup(environment, robotInterface.getSimulateDT());
      scsInitialSetup.setSimulationDataBufferSize(simulationDataBufferSize);
      scsInitialSetup.setInitializeEstimatorToActual(true);

      double dt = scsInitialSetup.getDT();
      int recordFrequency = (int) Math.round(timePerRecordTick / dt);
      if (recordFrequency < 1)
         recordFrequency = 1;
      scsInitialSetup.setRecordFrequency(recordFrequency);

      environment.activateDisturbanceControllerOnSteeringWheel(YoFunctionGeneratorMode.SINE);

      WalkingControllerParameters drivingControllerParameters = new DRCRobotDrivingControllerParameters();
      DRCRobotJointMap jointMap = robotInterface.getJointMap();
      HighLevelState initialBehavior = HighLevelState.DRIVING;
      ControllerFactory controllerFactory = DRCObstacleCourseSimulation.createDRCMultiControllerFactory(null, drivingControllerParameters, false, jointMap, initialBehavior);

      drcSimulation = DRCSimulationFactory.createSimulation(controllerFactory, environment, robotInterface, robotInitialSetup, scsInitialSetup,
              guiInitialSetup, null);

      SimulationConstructionSet simulationConstructionSet = drcSimulation.getSimulationConstructionSet();

      // add other registries
      drcSimulation.addAdditionalDynamicGraphicObjectsListRegistries(dynamicGraphicObjectsListRegistry);

      simulationConstructionSet.setCameraPosition(6.0, -2.0, 4.5);
      simulationConstructionSet.setCameraFix(-0.44, -0.17, 0.75);

//    showSeatGraphics(simulationConstructionSet);

      setUpJoyStick(simulationConstructionSet);

      if (automaticSimulationRunner != null)
      {
         drcSimulation.start(automaticSimulationRunner);
      }
      else
      {
         drcSimulation.start(null);
      }
   }

   private void setUpJoyStick(SimulationConstructionSet simulationConstructionSet)
   {
      try
      {
         new DRCRobotSteeringWheelJoystickController(simulationConstructionSet);
      }
      catch (Exception e)
      {
         System.out.println("Could not connect to joystick");
      }
   }

// private void showSeatGraphics(SimulationConstructionSet sim)
// {
//    Graphics3DObject seatGraphics = new Graphics3DObject();
//    seatGraphics.scale(0.25);
//    seatGraphics.translate(-1.25, 0, 3.25);
//    seatGraphics.rotate(Math.toRadians(90), Graphics3DObject.Z);
//    seatGraphics.addModelFile(DRCDemo03.class.getResource("models/seat.3DS"));
//    sim.addStaticLinkGraphics(seatGraphics);
// }

   public static void main(String[] args) throws JSAPException
   {
      AutomaticSimulationRunner automaticSimulationRunner = null;

      DRCGuiInitialSetup guiInitialSetup = new DRCGuiInitialSetup(false, false);

      double timePerRecordTick = 0.005;
      int simulationDataBufferSize = 16000;
      String ipAddress = null;
      int portNumber = -1;
      new DRCDemo03(guiInitialSetup, automaticSimulationRunner, timePerRecordTick, simulationDataBufferSize);
   }

   public SimulationConstructionSet getSimulationConstructionSet()
   {
      return drcSimulation.getSimulationConstructionSet();
   }
}

package us.ihmc.avatar.networkProcessor.kinematicsToolboxModule;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;

import com.badlogic.gdx.math.collision.BoundingBox;

import us.ihmc.avatar.collisionAvoidance.FrameConvexPolytopeVisualizer;
import us.ihmc.avatar.collisionAvoidance.RobotCollisionMeshProvider;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.jointAnglesWriter.JointAnglesWriter;
import us.ihmc.commons.RandomNumbers;
import us.ihmc.communication.controllerAPI.CommandInputManager;
import us.ihmc.communication.controllerAPI.StatusMessageOutputManager;
import us.ihmc.communication.packets.HumanoidKinematicsToolboxConfigurationMessage;
import us.ihmc.communication.packets.KinematicsToolboxRigidBodyMessage;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.geometry.polytope.DCELPolytope.Frame.FrameConvexPolytope;
import us.ihmc.graphicsDescription.Graphics3DObject;
import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.appearance.YoAppearanceRGBColor;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddModelFileInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DPrimitiveInstruction;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsListRegistry;
import us.ihmc.humanoidRobotics.communication.kinematicsToolboxAPI.HumanoidKinematicsToolboxConfigurationCommand;
import us.ihmc.humanoidRobotics.communication.packets.walking.CapturabilityBasedStatus;
import us.ihmc.robotModels.FullHumanoidRobotModel;
import us.ihmc.robotModels.FullRobotModel;
import us.ihmc.robotModels.FullRobotModelUtils;
import us.ihmc.robotics.MathTools;
import us.ihmc.robotics.robotController.RobotController;
import us.ihmc.robotics.robotDescription.ConvexShapeDescription;
import us.ihmc.robotics.robotDescription.JointDescription;
import us.ihmc.robotics.robotDescription.LinkDescription;
import us.ihmc.robotics.robotDescription.LinkGraphicsDescription;
import us.ihmc.robotics.robotDescription.RobotDescription;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.robotSide.RobotSideTest;
import us.ihmc.robotics.screwTheory.OneDoFJoint;
import us.ihmc.robotics.screwTheory.RigidBody;
import us.ihmc.robotics.screwTheory.ScrewTools;
import us.ihmc.robotics.sensors.ForceSensorDefinition;
import us.ihmc.robotics.sensors.IMUDefinition;
import us.ihmc.sensorProcessing.communication.packets.dataobjects.RobotConfigurationData;
import us.ihmc.sensorProcessing.simulatedSensors.DRCPerfectSensorReaderFactory;
import us.ihmc.simulationconstructionset.FloatingRootJointRobot;
import us.ihmc.simulationconstructionset.Robot;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner;
import us.ihmc.simulationconstructionset.util.simulationTesting.SimulationTestingParameters;
import us.ihmc.tools.MemoryTools;
import us.ihmc.tools.thread.ThreadTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;

public abstract class AvatarInverseKinematicsObstacleAvoidanceTest
{
   private static final boolean visualize = true;

   private static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();

   private YoVariableRegistry testRegistry = new YoVariableRegistry("InverseKinematicsTestRegistry");
   private YoGraphicsListRegistry graphicsRegistry = new YoGraphicsListRegistry();
   private final AppearanceDefinition ghostAppearance = new YoAppearanceRGBColor(Color.YELLOW, 0.8);
   private final double robotTransparency = 0.75;
   
   private SimulationTestingParameters simulationTestParameters;
   private SimulationConstructionSet scs;
   private BlockingSimulationRunner blockingSimulationRunner;

   public abstract boolean keepSCSUp();

   public abstract int getNumberOfObstacles();

   public abstract boolean specifyObstacles();

   public abstract List<? extends ConvexShapeDescription> getObstacleDescription();

   public abstract BoundingBox getWorkspaceBoundsForCreatingObstacles();

   public abstract Point3D getDesiredHandLocation();

   public abstract DRCRobotModel getRobotModel();

   private final List<FrameConvexPolytope> obstacleList = new ArrayList<>();
   private FrameConvexPolytopeVisualizer visualizer = new FrameConvexPolytopeVisualizer(40, testRegistry, graphicsRegistry);

   YoBoolean initializationSucceeded = new YoBoolean("ControllerInitializationSucceeded", testRegistry);
   
   private FloatingRootJointRobot scsRobot;
   private FloatingRootJointRobot scsGhostRobot;
   private FullHumanoidRobotModel controllerRobotModel;
   private FullHumanoidRobotModel ghostRobotModel;
   private JointAnglesWriter controllerRobotWriter;
   private JointAnglesWriter ghostRobotWriter;
   private HumanoidKinematicsToolboxController toolbox;
   private CommandInputManager commandInputManager;
   private RobotController controllerWrapper;

   @Before
   public void setupTest()
   {
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " before test.");
      simulationTestParameters = new SimulationTestingParameters();
      simulationTestParameters.setKeepSCSUp(keepSCSUp());

      DRCRobotModel controllerRobot = getRobotModel();
      RobotDescription robotDescription = controllerRobot.getRobotDescription();
      //recursivelyModifyRobotGraphics(robotDescription.getChildrenJoints().get(0));
      controllerRobotModel = controllerRobot.createFullRobotModel();
      scsRobot = controllerRobot.createHumanoidFloatingRootJointRobot(false);
      DRCPerfectSensorReaderFactory drcPerfectSensorReaderFactory = new DRCPerfectSensorReaderFactory(scsRobot, null, 0);
      drcPerfectSensorReaderFactory.build(controllerRobotModel.getRootJoint(), null, null, null, null, null, null);
      drcPerfectSensorReaderFactory.getSensorReader().read();

      commandInputManager = new CommandInputManager(KinematicsToolboxModule.supportedCommands());
      commandInputManager.registerConversionHelper(new KinematicsToolboxCommandConverter(controllerRobotModel.getElevator()));
      StatusMessageOutputManager statusOutputManager = new StatusMessageOutputManager(KinematicsToolboxModule.supportedStatus());
      toolbox = new HumanoidKinematicsToolboxController(commandInputManager, statusOutputManager, controllerRobotModel, graphicsRegistry, testRegistry,
                                                        visualizer);
      scsRobot.setGravity(0.0);
      scsRobot.setDynamic(false);
      controllerRobotWriter = new JointAnglesWriter(scsRobot, controllerRobotModel.getRootJoint(), controllerRobotModel.getOneDoFJoints());

      DRCRobotModel ghostRobot = getRobotModel();
      RobotDescription ghostDescription = ghostRobot.getRobotDescription();
      ghostDescription.setName("GhostHumanoid");
      recursivelyModifyGhostGraphics(ghostDescription.getChildrenJoints().get(0));
      scsGhostRobot = ghostRobot.createHumanoidFloatingRootJointRobot(false);
      scsGhostRobot.setGravity(0.0);
      scsGhostRobot.setDynamic(false);
      ghostRobotModel = ghostRobot.createFullRobotModel();
      ghostRobotWriter = new JointAnglesWriter(scsGhostRobot, ghostRobotModel.getRootJoint(), ghostRobotModel.getOneDoFJoints());
      toolbox.setCollisionMeshes(new RobotCollisionMeshProvider(4).createCollisionMeshesFromRobotDescription(controllerRobotModel,
                                                                                                             controllerRobot.getRobotDescription()));
      toolbox.submitObstacleCollisionMesh(createObstacles());
      controllerWrapper = createControllerWrapperAroundToolbox();
      scsRobot.setController(controllerWrapper);
      if (visualize)
      {
         scs = new SimulationConstructionSet(new Robot[] {scsRobot}, simulationTestParameters);
         Graphics3DObject coordinateSystem = new Graphics3DObject();
         coordinateSystem.addCoordinateSystem(.5);
         scs.setGroundVisible(false);
         scs.addStaticLinkGraphics(coordinateSystem);
         scs.addYoGraphicsListRegistry(graphicsRegistry, true);
         scs.setCameraFix(0.0, 0.0, 1.0);
         scs.setCameraPosition(8.0, 0.0, 3.0);
         scs.setDT(0.001, 1);
         blockingSimulationRunner = new BlockingSimulationRunner(scs, 6000.0);
         scs.startOnAThread();
      }
   }

   private void recursivelyModifyGhostGraphics(JointDescription joint)
   {
      if (joint == null)
         return;
      LinkDescription link = joint.getLink();
      if (link == null)
         return;
      LinkGraphicsDescription linkGraphics = link.getLinkGraphics();
      if (linkGraphics == null)
         return;

      ArrayList<Graphics3DPrimitiveInstruction> graphics3dInstructions = linkGraphics.getGraphics3DInstructions();

      if (graphics3dInstructions == null)
         return;

      for (Graphics3DPrimitiveInstruction primitive : graphics3dInstructions)
      {
         if (primitive instanceof Graphics3DAddModelFileInstruction)
         {
            Graphics3DAddModelFileInstruction modelInstruction = (Graphics3DAddModelFileInstruction) primitive;
            modelInstruction.setAppearance(ghostAppearance);
         }
      }

      if (joint.getChildrenJoints() == null)
         return;

      for (JointDescription child : joint.getChildrenJoints())
      {
         recursivelyModifyGhostGraphics(child);
      }

   }

   private void recursivelyModifyRobotGraphics(JointDescription joint)
   {
      if (joint == null)
         return;
      LinkDescription link = joint.getLink();
      if (link == null)
         return;
      LinkGraphicsDescription linkGraphics = link.getLinkGraphics();
      if (linkGraphics == null)
         return;

      ArrayList<Graphics3DPrimitiveInstruction> graphics3dInstructions = linkGraphics.getGraphics3DInstructions();

      if (graphics3dInstructions == null)
         return;

      for (Graphics3DPrimitiveInstruction primitive : graphics3dInstructions)
      {
         if (primitive instanceof Graphics3DAddModelFileInstruction)
         {
            Graphics3DAddModelFileInstruction modelInstruction = (Graphics3DAddModelFileInstruction) primitive;
            AppearanceDefinition modelApprearance = modelInstruction.getAppearance();
            if(modelApprearance == null)
               modelInstruction.setAppearance(new YoAppearanceRGBColor(Color.WHITE, robotTransparency));
            else
               modelApprearance.setTransparency(robotTransparency);
         }
      }

      if (joint.getChildrenJoints() == null)
         return;

      for (JointDescription child : joint.getChildrenJoints())
      {
         recursivelyModifyGhostGraphics(child);
      }

   }
   
   
   private RobotController createControllerWrapperAroundToolbox()
   {
      return new RobotController()
      {
         @Override
         public void initialize()
         {
         }

         @Override
         public YoVariableRegistry getYoVariableRegistry()
         {
            return testRegistry;
         }

         @Override
         public String getName()
         {
            return scsRobot.getName() + "Controller";
         }

         @Override
         public String getDescription()
         {
            return "To take over the world one obstacle at a time";
         }

         @Override
         public void doControl()
         {
            if (!initializationSucceeded.getBooleanValue())
               initializationSucceeded.set(toolbox.initialize());

            if (initializationSucceeded.getBooleanValue())
            {
               toolbox.updateInternal();
               controllerRobotWriter.updateRobotConfigurationBasedOnFullRobotModel();
               visualizer.update();
            }
         }
      };
   }

   @After
   public void cleanTest()
   {
      cleanTestVariables();
      if (simulationTestParameters.getKeepSCSUp())
      {
         ThreadTools.sleepForever();
      }

      simulationTestParameters = null;
      MemoryTools.printCurrentMemoryUsageAndReturnUsedMemoryInMB(getClass().getSimpleName() + " after test.");
   }

   private void cleanTestVariables()
   {
      obstacleList.clear();
   }

   private List<FrameConvexPolytope> createObstacles()
   {
      int numberOfObstacles = getNumberOfObstacles();
      if (specifyObstacles())
      {
         RobotCollisionMeshProvider meshProvider = new RobotCollisionMeshProvider(4);
         List<? extends ConvexShapeDescription> obstacleDescriptions = getObstacleDescription();
         for (int i = 0; i < numberOfObstacles; i++)
         {
            ConvexShapeDescription obstacleDescription = obstacleDescriptions.get(i);
            FrameConvexPolytope obstacleMesh = meshProvider.createCollisionMeshesFromDescription(worldFrame, obstacleDescription);
            obstacleList.add(obstacleMesh);
         }
      }
      else
      {
         throw new RuntimeException("Unimplemented case");
      }
      return obstacleList;
   }

   public void testInverseKinematics()
   {
      Random random = new Random(2145);
      toolbox.enableCollisionAvoidance(true);
      toolbox.updateCapturabilityBasedStatus(createCapturabilityBasedStatus(true, true));
      HumanoidKinematicsToolboxConfigurationMessage command = new HumanoidKinematicsToolboxConfigurationMessage();
      command.setHoldCurrentCenterOfMassXYPosition(true);
      commandInputManager.submitMessage(command);
      for (RobotSide side : new RobotSide[]{RobotSide.RIGHT, RobotSide.LEFT})
      {
         //randomizeArmJointPositions(random, side, ghostRobotModel);
         ghostRobotWriter.updateRobotConfigurationBasedOnFullRobotModel();
         RigidBody hand = ghostRobotModel.getHand(side);
         FramePoint3D desiredPosition = new FramePoint3D(hand.getBodyFixedFrame());
         desiredPosition.add(0.4, side.negateIfLeftSide(0.65), 0.0);
         desiredPosition.changeFrame(worldFrame);
         KinematicsToolboxRigidBodyMessage message = new KinematicsToolboxRigidBodyMessage(hand, desiredPosition);
         message.setWeight(20.0);
         commandInputManager.submitMessage(message);
      }
      runControllerToolbox(5000);
   }

   private void randomizeRobotConfiguration(FullRobotModel robotModel, double percentOfMotionRangeAllowed, Random random)
   {
      randomizeJointPositions(random, robotModel.getOneDoFJoints(), percentOfMotionRangeAllowed);
   }

   private void randomizeArmJointPositions(Random random, RobotSide robotSide, FullHumanoidRobotModel robotModelToModify)
   {
      randomizeArmJointPositions(random, robotSide, robotModelToModify, 1.0);
   }

   private void randomizeArmJointPositions(Random random, RobotSide robotSide, FullHumanoidRobotModel robotModelToModify, double percentOfMotionRangeAllowed)
   {
      RigidBody chest = robotModelToModify.getChest();
      RigidBody hand = robotModelToModify.getHand(robotSide);
      randomizeKinematicsChainPositions(random, chest, hand, percentOfMotionRangeAllowed);
   }

   private void randomizeKinematicsChainPositions(Random random, RigidBody base, RigidBody body)
   {
      randomizeKinematicsChainPositions(random, base, body, 1.0);
   }

   private void randomizeKinematicsChainPositions(Random random, RigidBody base, RigidBody body, double percentOfMotionRangeAllowed)
   {
      percentOfMotionRangeAllowed = MathTools.clamp(percentOfMotionRangeAllowed, 0.0, 1.0);

      OneDoFJoint[] joints = ScrewTools.createOneDoFJointPath(base, body);

      randomizeJointPositions(random, joints, percentOfMotionRangeAllowed);
   }

   private void randomizeJointPositions(Random random, OneDoFJoint[] joints, double percentOfMotionRangeAllowed)
   {
      for (OneDoFJoint joint : joints)
      {
         double jointLimitLower = joint.getJointLimitLower();
         double jointLimitUpper = joint.getJointLimitUpper();
         double rangeReduction = (1.0 - percentOfMotionRangeAllowed) * (jointLimitUpper - jointLimitLower);
         jointLimitLower += 0.5 * rangeReduction;
         jointLimitUpper -= 0.5 * rangeReduction;
         joint.setQ(RandomNumbers.nextDouble(random, jointLimitLower, jointLimitUpper));
      }
   }

   private void runControllerToolbox(int numberOfTicks)
   {
      RobotConfigurationData robotConfigurationData = extractRobotConfigurationData(controllerRobotModel);
      toolbox.updateRobotConfigurationData(robotConfigurationData);

      initializationSucceeded.set(false);
      try
      {
         System.gc();
         if (visualize)
         {
            blockingSimulationRunner.simulateNTicksAndBlockAndCatchExceptions(numberOfTicks);
         }
         else
         {
            for (int i = 0; i < numberOfTicks; i++)
               controllerWrapper.doControl();
         }
      }
      catch (Exception e)
      {
         assertTrue(false);
      }
   }

   private RobotConfigurationData extractRobotConfigurationData(FullHumanoidRobotModel fullRobotModel)
   {
      OneDoFJoint[] joints = FullRobotModelUtils.getAllJointsExcludingHands(fullRobotModel);
      RobotConfigurationData robotConfigurationData = new RobotConfigurationData(joints, new ForceSensorDefinition[0], null, new IMUDefinition[0]);
      robotConfigurationData.setJointState(Arrays.stream(joints).collect(Collectors.toList()));
      robotConfigurationData.setRootTranslation(fullRobotModel.getRootJoint().getTranslationForReading());
      robotConfigurationData.setRootOrientation(fullRobotModel.getRootJoint().getRotationForReading());
      return robotConfigurationData;
   }
   
   private CapturabilityBasedStatus createCapturabilityBasedStatus(boolean isLeftFootInSupport, boolean isRightFootInSupport)
   {
      CapturabilityBasedStatus capturabilityBasedStatus = new CapturabilityBasedStatus();
      capturabilityBasedStatus.leftFootSupportPolygonLength = isLeftFootInSupport ? 1 : 0;
      capturabilityBasedStatus.rightFootSupportPolygonLength = isRightFootInSupport ? 1 : 0;
      return capturabilityBasedStatus;
   }
}

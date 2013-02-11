package us.ihmc.darpaRoboticsChallenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math3.util.FastMath;

import us.ihmc.commonAvatarInterfaces.CommonAvatarEnvironmentInterface;
import us.ihmc.graphics3DAdapter.graphics.appearances.YoAppearance;

import com.yobotics.simulationconstructionset.ExternalForcePoint;
import com.yobotics.simulationconstructionset.GroundContactModel;
import com.yobotics.simulationconstructionset.GroundContactPoint;
import com.yobotics.simulationconstructionset.GroundProfile;
import com.yobotics.simulationconstructionset.Robot;
import com.yobotics.simulationconstructionset.SimulationConstructionSet;
import com.yobotics.simulationconstructionset.util.LinearStickSlipGroundContactModel;
import com.yobotics.simulationconstructionset.util.environments.ContactableSelectableBoxRobot;
import com.yobotics.simulationconstructionset.util.environments.ContactableToroidRobot;
import com.yobotics.simulationconstructionset.util.environments.SelectableObject;
import com.yobotics.simulationconstructionset.util.environments.SelectableObjectListener;
import com.yobotics.simulationconstructionset.util.graphics.DynamicGraphicObjectsListRegistry;
import com.yobotics.simulationconstructionset.util.ground.CombinedTerrainObject;
import com.yobotics.simulationconstructionset.util.ground.Contactable;
import com.yobotics.simulationconstructionset.util.ground.TerrainObject;

public class DRCDemoEnvironmentSimpleSteeringWheelContact implements CommonAvatarEnvironmentInterface
{
   private final CombinedTerrainObject combinedTerrainObject;
   private final ArrayList<Robot> envRobots = new ArrayList<Robot>();
   private final ArrayList<ExternalForcePoint> contactPoints = new ArrayList<ExternalForcePoint>();
   private final ArrayList<Contactable> contactables = new ArrayList<Contactable>();

   public DRCDemoEnvironmentSimpleSteeringWheelContact(DynamicGraphicObjectsListRegistry dynamicGraphicObjectsListRegistry)
   {
      combinedTerrainObject = createCombinedTerrainObject();

      Matrix3d pinJointZRotation = new Matrix3d();
      pinJointZRotation.rotZ(-FastMath.PI / 2.0);
      Matrix3d pinJointRotation = new Matrix3d();
      pinJointRotation.rotY(-FastMath.PI / 4.0);
      pinJointRotation.mul(pinJointZRotation);

      Vector3d pinJointLocation = new Vector3d(0.6, 0.0, 0.9);
      Transform3D pinJointTransformFromWorld = new Transform3D(pinJointRotation, pinJointLocation, 1.0);
      Vector3d pinJointLinkCoMOffset = new Vector3d(0.0, 0.0, 0.05);

      ContactableToroidRobot bot = new ContactableToroidRobot("steeringWheel", pinJointTransformFromWorld, pinJointLinkCoMOffset);
      bot.createAvailableContactPoints(0, 30, 1.0 / 15.0);
      contactables.add(bot);
      envRobots.add(bot);

      double length = 0.6;
      double width = 0.6;
      double height = 0.6;
      double mass = 1.0;
      ContactableSelectableBoxRobot contactableBoxRobot = ContactableSelectableBoxRobot.createContactableWoodBoxRobot("BoxRobot1", length, width, height, mass);
      contactableBoxRobot.setPosition(0.6, 0.0, 3.0);
      contactableBoxRobot.createAvailableContactPoints(0, 10, 1.0 / 15.0);
      
      GroundContactModel groundContactModel = createGroundContactModel(contactableBoxRobot, combinedTerrainObject);
      contactableBoxRobot.setGroundContactModel(groundContactModel);
      
      contactables.add(contactableBoxRobot);
      envRobots.add(contactableBoxRobot);      
      
      ArrayList<GroundContactPoint> wheelPts = new ArrayList<GroundContactPoint>();
      ArrayList<GroundContactPoint> boxPts = new ArrayList<GroundContactPoint>();
      
      ExternalForcePoint[] pts = new ExternalForcePoint[wheelPts.size() + boxPts.size()];
      
      Set<GroundContactPoint> set = new HashSet<GroundContactPoint>();
      
      set.addAll(wheelPts);
      set.addAll(boxPts);
      
      set.toArray(pts);
      
      addContactPoints(pts);
      
   }

   public TerrainObject getTerrainObject()
   {
      return combinedTerrainObject;
   }

   public List<Robot> getEnvironmentRobots()
   {
      return envRobots;
   }

   public void createAndSetContactControllerToARobot()
   {
      ContactController contactController = new ContactController();
      contactController.addContactPoints(contactPoints);
      contactController.addContactables(contactables);
      envRobots.get(0).setController(contactController);
   }

   public void addContactPoints(ExternalForcePoint[] externalForcePoints)
   {
      for (ExternalForcePoint pt : externalForcePoints)
      {
         this.contactPoints.add(pt);
      }
   }

   public void addSelectableListenerToSelectables(SelectableObjectListener selectedListener)
   {
      for (Contactable contactable : contactables)
      {
         if (contactable instanceof SelectableObject)
         {
            ((SelectableObject) contactable).addSelectedListeners(selectedListener);
         }
      }

   }

   private CombinedTerrainObject createCombinedTerrainObject()
   {
      CombinedTerrainObject ret = new CombinedTerrainObject("simpleWheelTest");
      ret.addBox(-10.0, -10.0, 10.0, 10.0, -0.05, 0.0, YoAppearance.RGBColor(0.35, 0.35, 0.35));
      return ret;
   }

   public static void main(String[] args)
   {
      DynamicGraphicObjectsListRegistry dynamicGraphicObjectsListRegistry = new DynamicGraphicObjectsListRegistry();
      DRCDemoEnvironmentSimpleSteeringWheelContact env = new DRCDemoEnvironmentSimpleSteeringWheelContact(dynamicGraphicObjectsListRegistry);

      List<Robot> robots = env.getEnvironmentRobots();
      Robot[] robotArray = new Robot[robots.size()];
      robots.toArray(robotArray);

      SimulationConstructionSet scs = new SimulationConstructionSet(robotArray, 36000);
      scs.setDT(1e-4, 20);

      TerrainObject terrainObject = env.getTerrainObject();
      scs.addStaticLinkGraphics(terrainObject.getLinkGraphics());

      scs.addDynamicGraphicObjectListRegistries(dynamicGraphicObjectsListRegistry);
      scs.setGroundVisible(false);

      scs.startOnAThread();
   }
   
   private GroundContactModel createGroundContactModel(Robot robot, GroundProfile groundProfile)
   {
      double kXY = 40000.0;
      double bXY = 10.0;
      double kZ = 80.0;
      double bZ = 500.0;
      double alphaStick = 0.7;
      double alphaSlip = 0.5;

      GroundContactModel groundContactModel = new LinearStickSlipGroundContactModel(robot, kXY, bXY, kZ, bZ, alphaSlip, alphaStick,
                                                 robot.getRobotsYoVariableRegistry());
      groundContactModel.setGroundProfile(groundProfile);

      return groundContactModel;
   }
}

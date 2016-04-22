package us.ihmc.aware.controller.position;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.SdfLoader.SDFFullQuadrupedRobotModel;
import us.ihmc.SdfLoader.models.FullRobotModel;
import us.ihmc.aware.params.DoubleParameter;
import us.ihmc.aware.params.ParameterFactory;
import us.ihmc.aware.model.QuadrupedRuntimeEnvironment;
import us.ihmc.SdfLoader.partNames.QuadrupedJointName;
import us.ihmc.aware.model.QuadrupedRobotParameters;
import us.ihmc.quadrupedRobotics.parameters.QuadrupedInitialPositionParameters;
import us.ihmc.robotics.screwTheory.OneDoFJoint;
import us.ihmc.robotics.trajectories.MinimumJerkTrajectory;

/**
 * A controller that will track the minimum jerk trajectory to bring joints to a preparatory pose.
 */
public class QuadrupedPositionStandPrepController implements QuadrupedPositionController
{
   private final ParameterFactory parameterFactory = new ParameterFactory(getClass());
   private final DoubleParameter trajectoryTimeParameter = parameterFactory.createDouble("trajectoryTime", 1.0);
   private final QuadrupedInitialPositionParameters initialPositionParameters;

   private final SDFFullQuadrupedRobotModel fullRobotModel;
   private final double dt;

   private final List<MinimumJerkTrajectory> trajectories;

   /**
    * The time from the beginning of the current preparation trajectory in seconds.
    */
   private double timeInTrajectory = 0.0;

   public QuadrupedPositionStandPrepController(QuadrupedRuntimeEnvironment environment, QuadrupedInitialPositionParameters initialPositionParameters)
   {
      this.initialPositionParameters = initialPositionParameters;
      this.fullRobotModel = environment.getFullRobotModel();
      this.dt = environment.getControlDT();

      this.trajectories = new ArrayList<>(fullRobotModel.getOneDoFJoints().length);
      for (int i = 0; i < fullRobotModel.getOneDoFJoints().length; i++)
      {
         trajectories.add(new MinimumJerkTrajectory());
      }
   }

   @Override
   public void onEntry()
   {
      for (int i = 0; i < fullRobotModel.getOneDoFJoints().length; i++)
      {
         OneDoFJoint joint = fullRobotModel.getOneDoFJoints()[i];
         joint.setUnderPositionControl(true);

         QuadrupedJointName jointId = fullRobotModel.getNameForOneDoFJoint(joint);
         double desiredPosition = initialPositionParameters.getInitialPosition(jointId);

         // Start the trajectory from the current pos/vel/acc.
         MinimumJerkTrajectory trajectory = trajectories.get(i);
         trajectory.setMoveParameters(joint.getQ(), joint.getQd(), joint.getQdd(), desiredPosition, 0.0, 0.0,
               trajectoryTimeParameter.get());
      }

      // This is a new trajectory. We start at time 0.
      timeInTrajectory = 0.0;
   }

   @Override
   public QuadrupedPositionControllerEvent process()
   {
      fullRobotModel.updateFrames();

      for (int i = 0; i < fullRobotModel.getOneDoFJoints().length; i++)
      {
         OneDoFJoint joint = fullRobotModel.getOneDoFJoints()[i];
         MinimumJerkTrajectory trajectory = trajectories.get(i);

         trajectory.computeTrajectory(timeInTrajectory);
         joint.setqDesired(trajectory.getPosition());
      }

      timeInTrajectory += dt;

      return isMotionExpired() ? QuadrupedPositionControllerEvent.STARTING_POSE_REACHED : null;
   }

   @Override
   public void onExit()
   {
   }

   private boolean isMotionExpired()
   {
      return timeInTrajectory > trajectoryTimeParameter.get();
   }
}


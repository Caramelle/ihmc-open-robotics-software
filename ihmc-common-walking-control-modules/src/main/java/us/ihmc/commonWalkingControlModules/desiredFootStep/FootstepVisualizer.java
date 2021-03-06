package us.ihmc.commonWalkingControlModules.desiredFootStep;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.euclid.geometry.ConvexPolygon2D;
import us.ihmc.euclid.referenceFrame.FramePoint2D;
import us.ihmc.euclid.referenceFrame.FramePose3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicCoordinateSystem;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPolygon;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsListRegistry;
import us.ihmc.humanoidRobotics.bipedSupportPolygons.ContactablePlaneBody;
import us.ihmc.humanoidRobotics.footstep.Footstep;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.robotics.math.frames.YoFrameConvexPolygon2d;
import us.ihmc.robotics.math.frames.YoFramePose;
import us.ihmc.robotics.robotSide.RobotSide;

public class FootstepVisualizer
{
   private static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();

   private static final int maxNumberOfContactPoints = 6;

   private final YoFramePose yoFootstepPose;
   private final YoFrameConvexPolygon2d yoFoothold;

   private final FramePose3D footstepPose = new FramePose3D();
   private final ConvexPolygon2D foothold = new ConvexPolygon2D();

   private final RobotSide robotSide;
   private final List<Point2D> defaultContactPointsInSoleFrame = new ArrayList<>();

   private final YoGraphicCoordinateSystem poseViz;
   private final YoGraphicPolygon footholdViz;

   public FootstepVisualizer(String name, String graphicListName, RobotSide robotSide, ContactablePlaneBody contactableFoot, AppearanceDefinition footstepColor,
         YoGraphicsListRegistry yoGraphicsListRegistry, YoVariableRegistry registry)
   {
      this.robotSide = robotSide;
      yoFootstepPose = new YoFramePose(name + "Pose", worldFrame, registry);
      yoFoothold = new YoFrameConvexPolygon2d(name + "Foothold", "", worldFrame, maxNumberOfContactPoints, registry);

      double coordinateSystemSize = 0.2;
      double footholdScale = 1.0;
      poseViz = new YoGraphicCoordinateSystem(name + "Pose", yoFootstepPose, coordinateSystemSize, footstepColor);
      footholdViz = new YoGraphicPolygon(name + "Foothold", yoFoothold, yoFootstepPose, footholdScale, footstepColor);
      yoGraphicsListRegistry.registerYoGraphic(graphicListName, poseViz);
      yoGraphicsListRegistry.registerYoGraphic(graphicListName, footholdViz);

      List<FramePoint2D> contactPoints2d = contactableFoot.getContactPoints2d();
      for (int i = 0; i < contactPoints2d.size(); i++)
         defaultContactPointsInSoleFrame.add(new Point2D(contactPoints2d.get(i)));
   }

   public void update(Footstep footstep)
   {
      footstep.getPose(footstepPose);
      yoFootstepPose.setAndMatchFrame(footstepPose);

      List<Point2D> predictedContactPoints = footstep.getPredictedContactPoints();
      List<Point2D> contactPointsToVisualize;
      if (predictedContactPoints == null || predictedContactPoints.isEmpty())
         contactPointsToVisualize = defaultContactPointsInSoleFrame;
      else
         contactPointsToVisualize = predictedContactPoints;

      foothold.setAndUpdate(contactPointsToVisualize, contactPointsToVisualize.size());

      yoFoothold.setConvexPolygon2d(foothold);

      poseViz.update();
      footholdViz.update();
   }

   public void hide()
   {
      yoFootstepPose.setToNaN();
   }

   public RobotSide getRobotSide()
   {
      return robotSide;
   }
}

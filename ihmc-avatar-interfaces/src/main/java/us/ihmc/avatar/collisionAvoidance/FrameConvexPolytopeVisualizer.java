package us.ihmc.avatar.collisionAvoidance;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.ihmc.commons.PrintTools;
import us.ihmc.euclid.Axis;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.geometry.polytope.ConvexPolytopeConstructor;
import us.ihmc.geometry.polytope.DCELPolytope.Basics.ConvexPolytopeReadOnly;
import us.ihmc.geometry.polytope.DCELPolytope.Basics.PolytopeHalfEdgeReadOnly;
import us.ihmc.geometry.polytope.DCELPolytope.Basics.PolytopeVertexReadOnly;
import us.ihmc.geometry.polytope.DCELPolytope.Frame.FrameConvexPolytope;
import us.ihmc.graphicsDescription.Graphics3DObject;
import us.ihmc.graphicsDescription.appearance.YoAppearanceRGBColor;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicLineSegment;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPosition;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsListRegistry;
import us.ihmc.robotics.MathTools;
import us.ihmc.robotics.robotDescription.CollisionMeshDescription;
import us.ihmc.robotics.screwTheory.RigidBody;
import us.ihmc.simulationconstructionset.Robot;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;
import us.ihmc.simulationconstructionset.SimulationConstructionSetParameters;
import us.ihmc.tools.thread.ThreadTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

public class FrameConvexPolytopeVisualizer
{
   private static final int numberOfVizEdges = 1000;
   private static final int numberOfVizVertices = 500;
   private final YoVariableRegistry registry;
   private final YoGraphicsListRegistry graphicsListRegistry;
   private SimulationConstructionSet scs;
   private ArrayList<YoGraphicPosition> polytopeVerticesViz;
   private ArrayList<YoGraphicLineSegment> polytopeEdgesViz;
   private YoGraphicPosition position;
   private YoDouble yoTime;
   private final ConvexPolytopeReadOnly[] polytopes;
   private final Color[] polytopeColors;
   private int numberOfPolytopes = 0;
   private YoGraphicLineSegment collisionVector;
   private YoGraphicLineSegment collisionPoints;
   private static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();
   private boolean keepSCSUp = false;

   public FrameConvexPolytopeVisualizer(int maxNumberOfPolytopes, YoVariableRegistry registry, YoGraphicsListRegistry graphicsListRegistry)
   {
      this.scs = null;
      this.registry = registry;
      this.graphicsListRegistry = graphicsListRegistry;
      this.polytopes = new ConvexPolytopeReadOnly[maxNumberOfPolytopes];
      this.polytopeColors = new Color[maxNumberOfPolytopes];
      createVizArrays();
      createPolytopeVisualizationElements();
   }
   
   public FrameConvexPolytopeVisualizer(int maxNumberOfPolytopes)
   {
      this(maxNumberOfPolytopes, false);
   }

   public FrameConvexPolytopeVisualizer(int maxNumberOfPolytopes, boolean keepSCSUp, Robot... robots)
   {
      this.registry = new YoVariableRegistry("PolytopeVisualizer");
      this.graphicsListRegistry = new YoGraphicsListRegistry();
      this.keepSCSUp = keepSCSUp;
      createVizArrays();
      polytopes = new ConvexPolytopeReadOnly[maxNumberOfPolytopes];
      polytopeColors = new Color[maxNumberOfPolytopes];
      createPolytopeVisualizationElements();
      setupSCS(robots);
   }

   private void createVizArrays()
   {
      polytopeVerticesViz = new ArrayList<>(numberOfVizVertices);
      polytopeEdgesViz = new ArrayList<>(numberOfVizEdges);
   }

   public void addPolytope(ConvexPolytopeReadOnly polytopeToAdd)
   {
      polytopes[numberOfPolytopes] = polytopeToAdd;
      polytopeColors[numberOfPolytopes] = getNextColor();
      numberOfPolytopes++;
      update();
   }

   private Color getNextColor()
   {
      double numberOfDivisionsPerColor = Math.pow(polytopes.length, 1.0/ 3.0);
      double r = MathTools.clamp((numberOfPolytopes % numberOfDivisionsPerColor) / (numberOfDivisionsPerColor - 1), 0.0, 1.0);
      double g = MathTools.clamp(((numberOfPolytopes / numberOfDivisionsPerColor) % numberOfDivisionsPerColor) / (numberOfDivisionsPerColor - 1), 0.0, 1.0);
      double b = MathTools.clamp(((numberOfPolytopes / numberOfDivisionsPerColor / numberOfDivisionsPerColor) % numberOfDivisionsPerColor) / (numberOfDivisionsPerColor - 1), 0.0, 1.0);
      return new Color((float)r, (float)g, (float)b);
   }

   public void addPolytope(ConvexPolytopeReadOnly polytopeToAdd, Color color)
   {
      polytopes[numberOfPolytopes] = polytopeToAdd;
      polytopeColors[numberOfPolytopes] = color;
      numberOfPolytopes++;
   }

   public void update()
   {
      updatePolytopeVisualization(polytopes);
      if (scs!= null && keepSCSUp)
      {
         PrintTools.debug("Sleeping forever");
         ThreadTools.sleepForever();
      }
   }

   public void updateNonBlocking()
   {
      updatePolytopeVisualization(polytopes);
   }

   private void setupSCS(Robot... robots)
   {
      Robot robot = new Robot(getClass().getSimpleName() + "Robot");
      yoTime = robot.getYoTime();
      robot.addYoVariableRegistry(registry);
      robot.addYoGraphicsListRegistry(graphicsListRegistry);
      SimulationConstructionSetParameters parameters = new SimulationConstructionSetParameters();
      Robot[] robotList;
      if(robots == null)
      {
         robotList = new Robot[1];
         robotList[0] = robot;
      }
      else
      {
         robotList = Arrays.copyOf(robots, robots.length + 1);
         robotList[robots.length] = robot;
      }
      scs = new SimulationConstructionSet(robotList, parameters);
      Graphics3DObject coordinateSystem = new Graphics3DObject();
      coordinateSystem.addCoordinateSystem(.5);
      //scs.addStaticLinkGraphics(coordinateSystem);
      scs.setGroundVisible(false);
      scs.setDT(1.0, 1);
      scs.startOnAThread();
   }

   public void tickSCS()
   {
      if(scs!= null)
      {
         yoTime.add(1.0);
         scs.tickAndUpdate();
      }
   }

   private Point3D tempPoint1 = new Point3D();
   private Point3D tempPoint2 = new Point3D();

   public void showCollisionVector(Point3D startPoint, Point3D endPoint)
   {
      this.collisionPoints.setStartAndEnd(startPoint, endPoint);
   }

   
   public void showCollisionVector(Vector3D collisionVector)
   {
      tempPoint2.set(tempPoint1);
      tempPoint2.add(collisionVector);
      this.collisionVector.setStartAndEnd(tempPoint1, tempPoint2);
   }

   public void createPolytopeVisualizationElements()
   {
      collisionVector = new YoGraphicLineSegment("CollisionVector", "Viz", worldFrame, new YoAppearanceRGBColor(Color.PINK, 0.0), registry);
      collisionVector.setDrawArrowhead(true);
      collisionVector.setToNaN();
      graphicsListRegistry.registerYoGraphic("CollisionVector", collisionVector);

      collisionPoints = new YoGraphicLineSegment("CollisionPoints", "Viz", worldFrame, new YoAppearanceRGBColor(Color.GREEN, 0.0), registry);
      collisionPoints.setDrawArrowhead(true);
      collisionPoints.setToNaN();
      graphicsListRegistry.registerYoGraphic("CollisionPoints", collisionPoints);

      polytopeEdgesViz.clear();
      for (int i = 0; i < numberOfVizEdges; i++)
      {
         YoGraphicLineSegment edge = new YoGraphicLineSegment("PolytopeEdge" + i, "Viz", worldFrame, new YoAppearanceRGBColor(Color.BLACK, 0.5), registry);
         edge.setDrawArrowhead(false);
         edge.setToNaN();
         polytopeEdgesViz.add(edge);
      }
      graphicsListRegistry.registerYoGraphics("PolytopeEdges", polytopeEdgesViz);

      for (int i = 0; i < numberOfVizVertices; i++)
      {
         YoGraphicPosition point = new YoGraphicPosition("PolytopeVertex" + i, "Viz", registry, 0.001, new YoAppearanceRGBColor(Color.BLACK, 0.0));
         point.setPositionToNaN();
         polytopeVerticesViz.add(point);
      }
      graphicsListRegistry.registerYoGraphics("PolytopeVertices", polytopeVerticesViz);

      position = new YoGraphicPosition("PositionForVisibleEdges", "Viz", registry, 0.1, new YoAppearanceRGBColor(Color.RED, 0.5));
      position.setPositionToNaN();
      graphicsListRegistry.registerYoGraphic("VisualPoint", position);
   }

   private FramePoint3D tempFramePoint1 = new FramePoint3D();
   private FramePoint3D tempFramePoint2 = new FramePoint3D();

   public void updatePolytopeVisualization(ConvexPolytopeReadOnly... polytopes)
   {
      int edgeIndex = 0;
      int vertexIndex = 0;
      for (int j = 0; j < numberOfPolytopes; j++)
      {
         Color color = polytopeColors[j];
         List<? extends PolytopeHalfEdgeReadOnly> edges = polytopes[j].getEdges();
         List<? extends PolytopeVertexReadOnly> vertices = polytopes[j].getVertices();
         int i = 0;
         for (i = 0; i < edges.size(); i++)
         {
            polytopeEdgesViz.get(edgeIndex).setStartAndEnd(edges.get(i).getOriginVertex().getPosition(), edges.get(i).getDestinationVertex().getPosition());
            polytopeEdgesViz.get(edgeIndex).getAppearance().getColor().set(color);
            edgeIndex++;
         }
         for (i = 0; i < vertices.size(); i++)
         {
            polytopeVerticesViz.get(vertexIndex).setPosition(vertices.get(i).getPosition());
            vertexIndex++;
         }
      }
      for (; edgeIndex < polytopeEdgesViz.size(); edgeIndex++)
         polytopeEdgesViz.get(edgeIndex).setToNaN();
      for (; vertexIndex < polytopeVerticesViz.size(); vertexIndex++)
         polytopeVerticesViz.get(vertexIndex).setPositionToNaN();
      tickSCS();
   }

   public void addVerticesForViz(ArrayList<Point3D> pointList)
   {
      int index;
      for (index = 0; index < pointList.size(); index++)
      {
         polytopeVerticesViz.get(index).setPosition(pointList.get(index));
      }
      for (; index < polytopeVerticesViz.size(); index++)
      {
         polytopeVerticesViz.get(index).setPositionToNaN();
      }

      tickSCS();
   }

   public static void main(String args[])
   {
      FrameConvexPolytopeVisualizer viz = new FrameConvexPolytopeVisualizer(1);
      //ArrayList<Point3D> pointList = ConvexPolytopeConstructor.getCollisionMeshPointsForCapsule(0.0, 0.0, 0.0, Axis.Z, 1, 0.2, 8);
      //viz.addVerticesForViz(pointList);
      FrameConvexPolytope capsule = ConvexPolytopeConstructor.getFrameCapsuleCollisionMesh(new FramePoint3D(), Axis.Z, 1, 0.5, 4);
      viz.addPolytope(capsule, Color.BLUE);
      viz.update();
   }

   private static void testRobotMeshProvider()
   {
      FrameConvexPolytopeVisualizer viz = new FrameConvexPolytopeVisualizer(1);
      RobotCollisionMeshProvider meshProvider = new RobotCollisionMeshProvider(10);
      RigidBodyTransform transform = new RigidBodyTransform();
      RigidBody rigidBody = new RigidBody("RigidBody", transform, worldFrame);
      ArrayList<CollisionMeshDescription> collisionMeshDescriptionList = new ArrayList<>();
      CollisionMeshDescription collisionMesh = new CollisionMeshDescription();
      collisionMesh.addCylinderReferencedAtCenter(0.5, 2);
      collisionMesh.addCubeReferencedAtCenter(1, 1, 1);
      collisionMeshDescriptionList.add(collisionMesh);
      FrameConvexPolytope frameConvexPolytope = meshProvider.createCollisionMesh(rigidBody, collisionMeshDescriptionList);
      viz.addPolytope(frameConvexPolytope, Color.CYAN);
      viz.update();
   }

   public void updateColor(FrameConvexPolytope polytopeToChange, Color newColor)
   {
      for (int i = 0; i < numberOfPolytopes; i++)
         if(polytopes[i] == polytopeToChange)
            polytopeColors[i] = newColor;
      updateNonBlocking();
   }

   public void removePolytope(ConvexPolytopeReadOnly polytope)
   {
      for(int i = 0; i < numberOfPolytopes; i++)
      {
         if(polytopes[i] == polytope)
         {
            polytopes[i] = null;
            numberOfPolytopes--;
            break;
         }
      }
      update();
   }
}
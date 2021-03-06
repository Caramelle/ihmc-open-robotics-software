package us.ihmc.atlas;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

import us.ihmc.avatar.visualization.GainControllerSliderBoard;
import us.ihmc.avatar.visualization.WalkControllerSliderBoard;
import us.ihmc.robotDataLogger.Announcement;
import us.ihmc.robotDataLogger.YoVariableClient;
import us.ihmc.robotDataLogger.rtps.LogProducerDisplay;
import us.ihmc.robotDataVisualizer.visualizer.SCSVisualizer;
import us.ihmc.robotDataVisualizer.visualizer.SCSVisualizerStateListener;
import us.ihmc.simulationconstructionset.Robot;
import us.ihmc.simulationconstructionset.SimulationConstructionSet;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class RemoteAtlasVisualizer implements SCSVisualizerStateListener
{
   private static final int DEFAULT_ONE_IN_N_PACKETS_FOR_VIZ = 6;
   private static final AtlasSliderBoardType defaultSliderBoardType = AtlasSliderBoardType.WALK_CONTROLLER;

   public enum AtlasSliderBoardType {GAIN_CONTROLLER, JOINT_ANGLE_OFFSET, WALK_CONTROLLER}

   public RemoteAtlasVisualizer(int bufferSize, int displayOneInNPacketsFactor)
   {
      SCSVisualizer scsVisualizer = new SCSVisualizer(bufferSize);
      scsVisualizer.setDisplayOneInNPackets(displayOneInNPacketsFactor);
      scsVisualizer.addSCSVisualizerStateListener(this);
      scsVisualizer.addButton("requestStop", 1.0);
      scsVisualizer.addButton("calibrateWristForceSensors", 1.0);
      scsVisualizer.setShowOverheadView(true);

      YoVariableClient client = new YoVariableClient(scsVisualizer, new RemoteAtlasVisualizerLogFilter());
      client.start();
   }

   @Override
   public void starting(SimulationConstructionSet scs, Robot robot, YoVariableRegistry registry)
   {
      switch (defaultSliderBoardType)
      {
         case WALK_CONTROLLER :
            new WalkControllerSliderBoard(scs, registry, null);

            break;

         case GAIN_CONTROLLER :
            new GainControllerSliderBoard(scs, registry);

            break;

         case JOINT_ANGLE_OFFSET :
            new JointAngleOffsetSliderBoard(scs, registry);

            break;
      }
   }

   public static void main(String[] args) throws JSAPException
   {
      int bufferSize = 16384;
      JSAP jsap = new JSAP();

      FlaggedOption robotModel = new FlaggedOption("robotModel").setLongFlag("model").setShortFlag('m').setRequired(true).setStringParser(JSAP.STRING_PARSER);
      robotModel.setHelp("Robot models: " + AtlasRobotModelFactory.robotModelsToString());

      FlaggedOption oneInNPacketsFactor = new FlaggedOption("displayOneInNPackets").setLongFlag("display-one-in-n-packets").setShortFlag('p').setStringParser(JSAP.INTEGER_PARSER).setRequired(false);
      oneInNPacketsFactor.setHelp("Visualize one in ever N packets, where N is the argument passed in. Default value is 6 (displays one in every 6 packets)");
      oneInNPacketsFactor.setDefault("" + DEFAULT_ONE_IN_N_PACKETS_FOR_VIZ);

      Switch runningOnRealRobot = new Switch("runningOnRealRobot").setLongFlag("realRobot");


      jsap.registerParameter(robotModel);
      jsap.registerParameter(runningOnRealRobot);
      jsap.registerParameter(oneInNPacketsFactor);

      JSAPResult config = jsap.parse(args);

      try
      {
        int oneInNPacketsValue = DEFAULT_ONE_IN_N_PACKETS_FOR_VIZ;

         if (config.contains("displayOneInNPackets"))
         {
            oneInNPacketsValue = config.getInt("displayOneInNPackets");
         }

         new RemoteAtlasVisualizer(bufferSize, oneInNPacketsValue);
      }
      catch(IllegalArgumentException e)
      {
         System.err.println();
         System.err.println("Usage: java " + RemoteAtlasVisualizer.class.getName());
         System.err.println("                " + jsap.getUsage());
         System.err.println();
         System.exit(1);
      }
   }

   private class RemoteAtlasVisualizerLogFilter implements LogProducerDisplay.LogSessionFilter
   {
      @Override
      public boolean shouldAddToDisplay(Announcement description)
      {
         return description.getHostNameAsString().startsWith("cpu") || description.getHostNameAsString().equals("kiwi-test-server");
      }
   }
}

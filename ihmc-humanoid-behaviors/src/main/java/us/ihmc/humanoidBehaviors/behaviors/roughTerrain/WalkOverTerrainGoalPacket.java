package us.ihmc.humanoidBehaviors.behaviors.roughTerrain;

import us.ihmc.communication.packets.Packet;
import us.ihmc.robotics.geometry.FramePose;
import us.ihmc.robotics.geometry.FramePose2d;

public class WalkOverTerrainGoalPacket extends Packet<WalkOverTerrainGoalPacket>
{
   public FramePose goalPose;

   public WalkOverTerrainGoalPacket()
   {
   }

   public WalkOverTerrainGoalPacket(FramePose goalPose)
   {
      this.goalPose = goalPose;
   }

   @Override
   public boolean epsilonEquals(WalkOverTerrainGoalPacket other, double epsilon)
   {
      if(other == null)
         return false;

      return goalPose.epsilonEquals(other.goalPose, epsilon);
   }
}

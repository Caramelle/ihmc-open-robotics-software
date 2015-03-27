package us.ihmc.acsell.network;

import java.net.URISyntaxException;

import us.ihmc.acsell.parameters.BonoRobotModel;
import us.ihmc.communication.configuration.NetworkParameterKeys;
import us.ihmc.communication.configuration.NetworkParameters;
import us.ihmc.communication.kryo.IHMCCommunicationKryoNetClassList;
import us.ihmc.communication.packetCommunicator.KryoPacketClientEndPointCommunicator;
import us.ihmc.communication.packetCommunicator.KryoPacketCommunicator;
import us.ihmc.communication.packets.PacketDestination;
import us.ihmc.communication.util.NetworkPorts;
import us.ihmc.darpaRoboticsChallenge.drcRobot.DRCRobotModel;
import us.ihmc.darpaRoboticsChallenge.networkProcessor.DRCNetworkModuleParameters;
import us.ihmc.darpaRoboticsChallenge.networkProcessor.DRCNetworkProcessor;

import com.martiansoftware.jsap.JSAPException;

public class StepprNetworkProcessor
{
   private static final DRCRobotModel model = new BonoRobotModel(true, true);

   public static void main(String[] args) throws URISyntaxException, JSAPException
   {
      IHMCCommunicationKryoNetClassList netClassList = new IHMCCommunicationKryoNetClassList();
      String controllerKryoServerIp = NetworkParameters.getHost(NetworkParameterKeys.robotController);
            
//            model.getNetworkParameters().getRobotControlComputerIP();
      int tcpPort = NetworkPorts.NETWORK_PROCESSOR_TO_CONTROLLER_TCP_PORT.getPort();
      int communicatorId = PacketDestination.CONTROLLER.ordinal();

      KryoPacketCommunicator realRobotControllerConnection = new KryoPacketClientEndPointCommunicator(controllerKryoServerIp, tcpPort, netClassList,
            communicatorId, "Steppr_Controller_Endpoint");

      DRCNetworkModuleParameters networkModuleParams = new DRCNetworkModuleParameters();

      networkModuleParams.setControllerCommunicator(realRobotControllerConnection);
      networkModuleParams.setUseUiModule(true);
      networkModuleParams.setUseBehaviorModule(true);

      new DRCNetworkProcessor(model, networkModuleParams);
   }
}


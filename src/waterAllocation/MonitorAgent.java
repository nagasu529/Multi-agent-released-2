/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterAllocation;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import waterAllocation.Crops.cropType;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;
/**
 *
 * @author chiewchk
 */
public class MonitorAgent extends Agent{
    
    private MonitorUI monitorUI;

    protected void setup(){
        System.out.println(getAID()+" is ready");
        
        //Creating catalogue and running GUI
        //monitorUI = new monitorUI(this);
        monitorUI.show();
     
        //Start agent and register all service.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        dfd.addServices(sd);
        try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		} 
    }
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } 
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Seller-agent "+getAID().getName()+" terminating.");
    }
    
    public class agentInfo{
        String farmerName;
        String agentType;
        double waterVolumn;
        double pricePerMM;
        String sellingStatus;
        
        agentInfo(String farmerName, String agentType, double waterVolumn, double pricePerMM, String sellingStatus){
            this.farmerName = farmerName;
            this.agentType = agentType;
            this.waterVolumn = waterVolumn;
            this.pricePerMM = pricePerMM;
            this.sellingStatus = sellingStatus;
        }
    }
}
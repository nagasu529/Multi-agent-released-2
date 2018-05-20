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
public class Farmer extends Agent{
    
    private FarmerGUI myGui;
    Crops calCrops = new Crops();
    
    //set agent status after calculating water reduction on farm.
    String agentStatus;
    double volumeToSell;
    double volumeToBuy;
    double sellingPrice;
    double buyingPrice;
    String log = "";
    
    //The list of known water selling agent
    private AID[] sellerAgent;
    
    //Farmer information on each agent.
    agentInfo farmerInfo = new agentInfo("", "", 0.0, 0.0, "avalable");
    
    //The list of information (buying or selling) from agent which include price and mm^3
    private HashMap catalogue = new HashMap();
    
    protected void setup(){
        System.out.println(getAID()+" is ready");
        
        //Creating catalogue and running GUI
        myGui = new FarmerGUI(this);
        myGui.show();
     
        //Start agent and register all service.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        farmerInfo.agentType = "Farmer";
        sd.setType(farmerInfo.agentType);
        sd.setName(getAID().getName());
        farmerInfo.farmerName = getAID().getName();
        dfd.addServices(sd);
        try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
        //log.concat("Hello "+ getAID().getName() + "Stage is " + sd.getType()+"\n");
        
        System.out.println("Hello "+ getAID().getName() + "Stage is " + sd.getType());
        
        //Add a TickerBehaviour that chooses agent status to buyer or seller.
        addBehaviour(new TickerBehaviour(this, 10000) {
	    protected void onTick() {
	    	System.out.println("Agent status is " + farmerInfo.agentType);
	    	
                    if (farmerInfo.agentType=="seller"||farmerInfo.agentType=="Farmer-seller") {
                    	//Register the seller description service on yellow pages.
                        farmerInfo.agentType = "Farmer-seller";
                        sd.setType(farmerInfo.agentType);
                        farmerInfo.pricePerMM = 300;
                        //farmerInfo.sellingStatus = "available";
                        dfd.addServices(sd);
                        System.out.println("");
                        System.out.println("Name: " + farmerInfo.farmerName);
                        System.out.println("Status: " + farmerInfo.agentType);
                        System.out.println("Volumn to sell: " + farmerInfo.waterVolumn);
                        System.out.println("Selling price: " + farmerInfo.pricePerMM);
                        System.out.println("Selling status: " + farmerInfo.sellingStatus);
                        System.out.println("");
                        System.out.println("preparing to sell");
                        System.out.println("");
                        
                        /*
                        ** Selling water process
                        */
                        //Catalogue updating
                        
                        //sellingPrice = 0.2;
                        //updateCatalogue(sd.getName(), sd.getType(), volumeToSell,sellingPrice);
                        // Add the behaviour queries from buyer agents
                        
                        addBehaviour(new OfferRequestsServer());
                        
                        // Add the behaviour serving purchase orders from buyer agents
                        addBehaviour(new PurchaseOrdersServer());
                        
                    } else if(farmerInfo.agentType=="buyer"||farmerInfo.agentType=="Farmer-buyer"){
                        farmerInfo.agentType = "Farmer-buyer";
                        sd.setType(farmerInfo.agentType);
                        farmerInfo.pricePerMM = 300;
                        farmerInfo.waterVolumn = 3935.868;
                        farmerInfo.sellingStatus = "unknown";
                        System.out.println("Name: " + farmerInfo.farmerName);
                        System.out.println("Status: " + farmerInfo.agentType);
                        System.out.println("Volumn to buy: " + farmerInfo.waterVolumn);
                        System.out.println("Buying price:" + farmerInfo.pricePerMM);
                        System.out.println("Selling status: " + farmerInfo.sellingStatus);
                        System.out.println("");
                        System.out.println("Lookign to buy water");
                        
                        /*
                        ** Buying water process
                        */
                        
                        //update seller list
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("Farmer");
                        template.addServices(sd);
                        try {
                        	DFAgentDescription[] result = DFService.search(myAgent, template); 
                        	System.out.println("Found the following seller agents:");
                          sellerAgent = new AID[result.length];
                          for (int i = 0; i < result.length; ++i) {
                            sellerAgent[i] = result[i].getName();
              	          	System.out.println(sellerAgent[i].getName());
                          }
                        }
                        catch (FIPAException fe) {
                          fe.printStackTrace();
                        }
                        addBehaviour(new RequestPerformer());
                    }
	    	}
        } );
    }
    
    //Update input data from GUI which include water allocation on single farm.
    public void farmerInput(final String filenameGlob, final Double actualRate, final int etSeason) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                //Input parameters from GUI
                calCrops.readText(filenameGlob);
                double totalWaterReductionPctg = actualRate/100;
                //Choosing ET0 from database.
                switch(etSeason){
                    case 0:
                        calCrops.ET0Spring();
                        
                        break;
                    case 1:
                        calCrops.ET0Summer();
                        //System.out.println("ET0 summer choosed");
                        break;
                    case 2:
                        calCrops.ET0Autumn();
                        //System.out.println("ET0 autumn choosed");
                        break;
                    default:
                        calCrops.ET0Winter();
                        //System.out.println("ET0 winter choosed");
                }
                calCrops.ET = calCrops.avgET0;
                calCrops.farmFactorValues();
                double actualReduction = calCrops.calcWaterReduction(totalWaterReductionPctg);
                System.out.println("");
                System.out.println("Water reduction result:");
            
                //Result calculation
                System.out.println("");
                Iterator itrR=calCrops.resultList.iterator();
                while (itrR.hasNext()) {
                    cropType st = (cropType)itrR.next();
                    System.out.println(st.cropName + " " + st.cropStage +
                        " " + st.droubhtSensitivity + " " + st.dsValue + " " + st.stValue + " " + st.cvValue +
                        " " + st.literPerSecHec + " " + st.waterReq + " " + st.cropCoefficient + " " + st.waterReduction);
                }   
                System.out.println("Actual reduction is: " + actualReduction);
            
                //Clean parameter
                calCrops.resultList.clear();
                calCrops.calList.clear();
                calCrops.cropT.clear();
                calCrops.cv.clear();
                calCrops.ds.clear();
                calCrops.order.clear();
                calCrops.st.clear();
                if (actualReduction >= (calCrops.totalWaterReq*totalWaterReductionPctg)) {
                    farmerInfo.agentType = "seller";
                    farmerInfo.waterVolumn = actualReduction;
                } //else {
                    //agentStatus = "buyer";
                //}
            }
        } );
    }
    
    /*
    *	OfferRequestsSerer 
    *	This behaviour is used b Seller mechanism for water buying request form other agent.
    *	If the requested water capacity and price match with buyer, the seller replies with a PROPOSE message specifying the price.
    *	Otherwise a REFUSE message is send back.
    * and PurchaseOrderServer is required by agent when the agent status is "Seller"
    */
    private class OfferRequestsServer extends CyclicBehaviour {
	public void action() {
		
			//Register service to DFDAgent

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            String log = new String();
            if (msg != null) {
                // CFP Message received. Process it
                //String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                
                //String sellingStatus = (String) farmerInfo.sellingStatus;
                
                if (farmerInfo.sellingStatus== "avalable") {
                    // The requested water is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(farmerInfo.waterVolumn));
                } else {
                    // The requested water is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("do not water for sale");
                }
                myAgent.send(reply);
                System.out.println(log);
            }else {
                block();
            }
        }
    }
    /*
     * 	PurchaseOrderServer
     * 	This behaviour is used by Seller agent to serve incoming offer acceptances (purchase orders) from buyer.
     * 	The seller agent will remove selling list and replies with an INFORM message to notify the buyer that purchase has been
     * 	successfully complete.
     */
    
    private class PurchaseOrdersServer extends CyclicBehaviour {
	public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                if (farmerInfo.sellingStatus=="avalable") {
                	farmerInfo.sellingStatus = "sold";
                	reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(getAID().getName()+" sold water to agent "+msg.getSender().getName());
                    System.out.println(farmerInfo.sellingStatus);
				} else {
					// The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available for sale");
				}
                
            }else {
                block();
            }
		}
    }
    /*
     * 	Request performer
     * 	
     * 	This behaviour is used by buyer mechanism to request seller agents for water pricing ana selling capacity.
     */
    private class RequestPerformer extends Behaviour {
	private AID bestSeller; // The agent who provides the best offer 
	private double bestPrice;  // The best offered price
	private int repliesCnt = 0; // The counter of replies from seller agents
	private MessageTemplate mt; // The template to receive replies
	private int step = 0;
        
	public void action() {
		//System.out.println(farmerInfo.farmerName + "is trying to buy: " + farmerInfo.waterVolumn + " mm^3 of water" );
		switch (step) {
	    case 0:
	    	// Send the cfp to all sellers
	      	ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
	      	for (int i = 0; i < sellerAgent.length; ++i) {
	        	cfp.addReceiver(sellerAgent[i]);
	      	} 
	      	cfp.setContent(String.valueOf(farmerInfo.waterVolumn));
	      	cfp.setConversationId("water-trade");
	      	cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
	      	myAgent.send(cfp);
	      	// Prepare the template to get proposals
	      	mt = MessageTemplate.and(MessageTemplate.MatchConversationId("water-trade"), 
	      		MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
	      	step = 1;
	    	break;
	    case 1:
	    	// Receive all proposals/refusals from seller agents
	      	ACLMessage reply = myAgent.receive(mt);
	      	if (reply != null) {
	        	// Reply received
	        	if (reply.getPerformative() == ACLMessage.PROPOSE) {
	          		// This is an offer 
	          		double volumn = Double.parseDouble(reply.getContent());
	          		if (bestSeller == null || volumn < bestPrice) {
	          			
	            		// This is the best offer at present
	            		bestPrice = volumn;
	            		System.out.println(volumn);
	            		bestSeller = reply.getSender();
	          		}
	        	}
	        	repliesCnt++;
	        	System.out.println("Best seller is " + bestSeller);
	        	System.out.println("Volumn to sell is :" + bestPrice);
	        	if (repliesCnt >= sellerAgent.length-1) {
	          		// We received all replies

	          		step = 2; 
	        	}
	      	}else {
	        	block();
	      	}
	      	break;
	    case 2:
	    	// Send the purchase order to the seller that provided the best offer
	      	ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        	order.addReceiver(bestSeller);
	      	order.setContent(String.valueOf(farmerInfo.pricePerMM));
	      	order.setConversationId("water-trade");
	      	order.setReplyWith("order"+System.currentTimeMillis());
	      	myAgent.send(order);
	      	// Prepare the template to get the purchase order reply
	      	mt = MessageTemplate.and(MessageTemplate.MatchConversationId("water-trade"), 
	      		MessageTemplate.MatchInReplyTo(order.getReplyWith()));
	    	
	    	step = 3;
	    	break;
	    case 3:      
	      	// Receive the purchase order reply
	      	reply = myAgent.receive(mt);
	      	if (reply != null) {
	        	// Purchase order reply received
	        	if (reply.getPerformative() == ACLMessage.INFORM) {
	          		// Purchase successful. We can terminate
	          		System.out.println(farmerInfo.farmerName +" successfully purchased from agent "+reply.getSender().getName());
	          		System.out.println("Price = "+bestPrice);
	          		myAgent.doDelete();
	        	}
	        	else {
	          		System.out.println("Attempt failed: requested water volumn already sold.");
	        	}
	        	
	        	step = 4;
	      	}
	      	else {
	        	block();
	      	}
	      	break;
	    }        
	}
	
	public boolean done() {
	  	if (step == 2 && bestSeller == null) {
	  		System.out.println("Attempt failed: "+volumeToBuy+" not available for sale");
	  	}
	    return ((step == 2 && bestSeller == null) || step == 4);
	}
    }
    
    public void updateCatalogue(final String agentName, final String agentType, final double waterVolumn, final double priceForSell){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                //farmerInfo.
            	//agentInfo agentInfo = new agentInfo(agentName, agentType, waterVolumn, priceForSell);
                //System.out.println(agentName+" need to sell water to others. The water volumn is = "+ volumeToSell);
                //System.out.println(agentInfo.agentType);
                //System.out.println(agentInfo.farmerName);
            }
        });
    }
    /*
    public void updateCatalogue(final String agentName, final double waterVolumn, final double priceForSell) {
        addBehaviour(new OneShotBehaviour() {
        public void action() {
            double array[] = {waterVolumn, priceForSell};
            System.out.println(array[1]);
            catalogue.put(agentName, array);
            
            //Get a set of the entries
            Set names = catalogue.entrySet();
            
            //Get a iterator
            Iterator i = names.iterator();
            
            //Display elements
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                System.out.print(me.getKey() +  ": ");
                System.out.println(me.getValue());
            }
            System.out.println();
            //System.out.println(agentName+" need to sell water to others. The water volumn is = "+ volumeToSell);
            }
        } );
    }
    */
    
   
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
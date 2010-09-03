package gsi.investalia.server.jade;

import jade.core.AID;


import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.tilab.wade.performer.WorkflowEngineAgent;
import com.tilab.wade.performer.WorkflowException;
import com.tilab.wade.dispatcher.DispatchingCapabilities;
import com.tilab.wade.dispatcher.WorkflowResultListener;
import com.tilab.wade.commons.AgentInitializationException;
import com.tilab.wade.performer.descriptors.WorkflowDescriptor;
import com.tilab.wade.performer.ontology.ExecutionError;


public class UpdateUserAgent extends WorkflowEngineAgent {
	//Object used to start the execution of workflows
	private DispatchingCapabilities dc = new DispatchingCapabilities();

	//Put agent initializations here
	protected void agentSpecificSetup() throws AgentInitializationException {
		super.agentSpecificSetup();

		//Register the register service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("updateuser");
		sd.setName("JADE-investalia-updateuser");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		//Initialize the DispatchingCapabilities instance used to launch workflows
		dc.init(this);
		
		//Launches the execution of the workflow 
		WorkflowDescriptor wd = new WorkflowDescriptor("gsi.investalia.server.wade.UpdateUserWorkflow");
		try {
			//Dispatch the workflow to myself 
			dc.launchWorkflow(getAID(), wd, new updateUserListener(), null);	
		} catch (WorkflowException e) {
			e.printStackTrace();
		}	
	}
	
	//This class indicates when and why the workflow execution has stopped.
	//If we find an error, we restart the workflow
	private class updateUserListener implements WorkflowResultListener {
		public void handleAssignedId(AID executor, String executionId) {
			//The workflow was properly loaded and a unique ID was assigned to it
			System.out.println("Workflow correctly loaded by performer "+executor.getLocalName());	
		}

		public void handleLoadError(String reason) {
			//The workflow could not be loaded
			System.out.println("Error loading the workflow");
		}

		public void handleNotificationError(AID executor, String executionId) {
			//There was a communication error receiving the notification from the executor
			System.out.println("Notification error ("+executionId+")");
		}

		public void handleExecutionError(ExecutionError er, AID executor, String executionId) {
			//The execution of the workflow failed
			System.out.println("Execution error ("+executionId+")");
			
			//We start a new one 
			WorkflowDescriptor wd = new WorkflowDescriptor("gsi.investalia.server.wade.UpdateUserWorkflow");
			try {
				//Dispatch the workflow to myself 
				dc.launchWorkflow(getAID(), wd, new updateUserListener(), null);	
			} catch (WorkflowException e) {
				e.printStackTrace();
			}	
		}
		
		public void handleExecutionCompleted(jade.util.leap.List results, AID executor, String executionId) {
			//The workflow was successfully executed
			System.out.println("Execution OK ("+executionId+")");
		}
	}

	//Agent clean-up operations 
	protected void takeDown() {
		//Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		//Printout a dismissal message
		System.out.println("UpdateUser-agent " + getAID().getName() + " terminating.");
	}
	
	public DispatchingCapabilities getDC() {
		return dc;
	}
}


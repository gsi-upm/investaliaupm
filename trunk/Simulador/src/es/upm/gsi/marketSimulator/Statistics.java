package es.upm.gsi.marketSimulator;

public class Statistics {
	
	static public void generateStatistics (Investors investor) {
		System.out.println("Estadistica en iteracion "+investor.getIteration()+":");
		friendStatistics(investor);
		messageStatisticsByAgentType(investor);
		
		//investorsStatistics(investor);
		
		//printActivityStatistics(investor);
		
		//printIbex35Statistics(((SimulateSocialExchange)investor.getRoot()).getStock());
	}
	
	static public void friendStatistics(Investors investor) {
		double reputationByAgentType[][][][] = new double[2][2][2]
		    [12 + Properties.NO_COMMON_FRIEND_WEIGHT.length * Properties.FRIEND_DEGRADATION_FACTOR.length];
		for(int i = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByFinance().size()-1; i >= 0; i--) {
			Investors cell = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByFinance().get(i);
			int commonFriends = 0;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			   [cell.getAgentType()[3]][3] += cell.getFriends().size();
			for(Investors friend: cell.getFriends()) {
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				      [cell.getAgentType()[3]][4 + friend.getAgentType()[1] * 4 + 
				       friend.getAgentType()[2] * 2 + friend.getAgentType()[3]]++;
				if(cell.getFollower().contains(friend))
					commonFriends++;
			}
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			                     [cell.getAgentType()[3]][0]++;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			      [cell.getAgentType()[3]][1] += commonFriends;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			      [cell.getAgentType()[3]][2] += cell.getFollower().size() - commonFriends;
			for(int k = 0; k < Properties.NO_COMMON_FRIEND_WEIGHT.length; k++) {
				for(int j = 0; j < Properties.FRIEND_DEGRADATION_FACTOR.length; j++) {
					reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
					   [cell.getAgentType()[3]][12 + k * Properties.FRIEND_DEGRADATION_FACTOR.length + j]
					   += cell.getFriendReputation()[k * Properties.FRIEND_DEGRADATION_FACTOR.length + j];
				}
			}			
		}
		for(int i = 0; i < reputationByAgentType.length; i++) {
			for(int j = 0; j < reputationByAgentType.length; j++) {
				for(int k = 0; k < reputationByAgentType.length; k++) {
					String agentType = "[";		
					if(i == Investors.FREQUENT_USER)
						agentType += "FRE_US,";
					else
						agentType += "OCA_US,";
					if(j == Investors.GOOD_WRITER)
						agentType += "GOOD_WR,";
					else
						agentType += "BAD_WRI,";
					if(k == Investors.FRIENDLY_USER)
						agentType += "FRI]";
					else
						agentType += "NOF]";
					agentType += ":"+String.format("commonF: %.2f", 
						reputationByAgentType[i][j][k][1]/reputationByAgentType[i][j][k][0]);
					agentType += ":"+String.format(" NoComF: %.2f", 
							reputationByAgentType[i][j][k][2]/reputationByAgentType[i][j][k][0]);
					agentType += ":"+String.format(" Frien: %.2f [", 
							reputationByAgentType[i][j][k][3]/reputationByAgentType[i][j][k][0]);
					for(int l = 0; l < 8; l++) {
						agentType += String.format("%.2f ",reputationByAgentType[i][j][k]
						[4 + l]/reputationByAgentType[i][j][k][3]);
					}
					agentType += "] - [";
					for(int l = 0; l < Properties.NO_COMMON_FRIEND_WEIGHT.length; l++) {
						for(int m = 0; m < Properties.FRIEND_DEGRADATION_FACTOR.length; m++) {
							agentType += String.format("%.2f ",reputationByAgentType[i][j][k]
							[12 + l * Properties.FRIEND_DEGRADATION_FACTOR.length + m]/
							reputationByAgentType[i][j][k][0]);
						}
					}
					System.out.println(agentType+"]");					
				}	
			}
		}		
	}
	
	static public void messageStatisticsByAgentType(Investors investor) {
		double reputationByAgentType[][][][] = new double[2][2][2][16 + 8];
		//int timeLimit = Properties.MAX_DIFFERENCE_CLUSTERS * Properties.TIME_CLUSTER;		
		for(int i = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByFinance().size()-1; i >= 0; i--) {
			Investors cell = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByFinance().get(i);
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			        [cell.getAgentType()[3]][0]++;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			        [cell.getAgentType()[3]][1] += cell.plays;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			        [cell.getAgentType()[3]][12] += cell.reads;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			        [cell.getAgentType()[3]][13] += cell.isPlayed;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			        [cell.getAgentType()[3]][14] += cell.isRead;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
			        [cell.getAgentType()[3]][23] += cell.isReadProbability / cell.probabilitiesNum;
			for(int j = cell.getMessages().size()-1; j >= 0; j--) {
				Message message = cell.getMessages().get(j);
				int timeDifference = investor.getIteration() - message.getDate();
				if(timeDifference >= Properties.TIME_LIMIT) {
					break;
				}
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				    [cell.getAgentType()[3]][2]++;
				if(message.isGood()) {
					reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
					       [cell.getAgentType()[3]][3]++;
				}
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				    [cell.getAgentType()[3]][4] += message.getUniqueReaders().size() +
				    message.getUniqueFollowers().size() * 3 + 
				    message.getScore() - message.getScores().size() * Properties.MAXIMUM_SCORE/2;
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				    [cell.getAgentType()[3]][5] += message.getUniqueReaders().size();
				for(Investors investo : message.getUniqueReaders()) {
					reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
					    [cell.getAgentType()[3]][15 + investo.getAgentType()[1] * 4 + 
					    investo.getAgentType()[2] * 2 + investo.getAgentType()[3]]++;
				}				
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				    [cell.getAgentType()[3]][6] += message.getUniqueFollowers().size() * 3;
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				    [cell.getAgentType()[3]][7] += message.getScore() - 
				    message.getScores().size() * Properties.MAXIMUM_SCORE/2;
				timeDifference /= Properties.TIME_CLUSTER;
				if(timeDifference > 2) {
					reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
					    [cell.getAgentType()[3]][7+4]++;
				} else{
					reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]]
				        [cell.getAgentType()[3]][7+timeDifference+1]++;
				}				
			}
			cell.plays = 0; cell.reads = 0; cell.isPlayed = 0; cell.isRead = 0;
			cell.isReadProbability = 0; cell.probabilitiesNum = 0;
		}
		//Print message statistics
		for(int i = 0; i < reputationByAgentType.length; i++) {
			for(int j = 0; j < reputationByAgentType[i].length; j++) {
				for(int k = 0; k < reputationByAgentType[i][j].length; k++) {
					String agentType = "[";		
					if(i == Investors.FREQUENT_USER)
						agentType += "FRE_US,";
					else
						agentType += "OCA_US,";
					if(j == Investors.GOOD_WRITER)
						agentType += "GOOD_WR,";
					else
						agentType += "BAD_WRI,";
					if(k == Investors.FRIENDLY_USER)
						agentType += "FRI";
					else
						agentType += "NOF";
					agentType += "("+(int)reputationByAgentType[i][j][k][0]+")]";
					//agentType += String.format("play:%.2f", 
					//	reputationByAgentType[i][j][k][1]/reputationByAgentType[i][j][k][0]);
					//agentType += String.format(" read:%.2f", 
					//		reputationByAgentType[i][j][k][12]/reputationByAgentType[i][j][k][0]);
					agentType += String.format(" isPlayed:%.2f", 
							reputationByAgentType[i][j][k][13]/reputationByAgentType[i][j][k][0]);
					agentType += String.format(" isRead:%.2f", 
							reputationByAgentType[i][j][k][14]/reputationByAgentType[i][j][k][0]);
					agentType += String.format(" readPro:%.2f", 
							reputationByAgentType[i][j][k][23]/reputationByAgentType[i][j][k][0]);
					agentType += String.format(" mes:%.2f", 
							reputationByAgentType[i][j][k][2]/reputationByAgentType[i][j][k][0]);
					agentType += String.format("(0:%.2f,",reputationByAgentType[i][j][k][8]
					       /reputationByAgentType[i][j][k][0]);
					agentType += String.format("1:%.2f,",reputationByAgentType[i][j][k][9]
				           /reputationByAgentType[i][j][k][0]);
					agentType += String.format("2:%.2f,",reputationByAgentType[i][j][k][10]
					       /reputationByAgentType[i][j][k][0]);
					agentType += String.format(">2:%.2f)",reputationByAgentType[i][j][k][11]
					       /reputationByAgentType[i][j][k][0]);
					agentType += String.format(" good:%.2f", 
							reputationByAgentType[i][j][k][3]/reputationByAgentType[i][j][k][0]);
					agentType += String.format(" rep mes:%.2f", 
							reputationByAgentType[i][j][k][4]/(reputationByAgentType[i][j][k][2]));
					agentType += String.format(" (rea:%.2f[", 
							reputationByAgentType[i][j][k][5]/(reputationByAgentType[i][j][k][2]));
					for(int l = 0; l < reputationByAgentType.length; l++) {
						for(int m = 0; m < reputationByAgentType[l].length; m++) {
							for(int n = 0; n < reputationByAgentType[l][m].length; n++) {
								agentType += String.format(",%.2f",
									reputationByAgentType[i][j][k][15 + l*4 + m*2 + n]
									/(reputationByAgentType[i][j][k][5]));
							}
						}
					}					
					agentType += String.format("],com:%.2f", 
							reputationByAgentType[i][j][k][6]/(reputationByAgentType[i][j][k][2]));
					agentType += String.format(",sco:%.2f)", 
							reputationByAgentType[i][j][k][7]/(reputationByAgentType[i][j][k][2]));
					System.out.println(agentType);					
				}	
			}			
		}
	}
	
	static public void printActivityStatistics(Investors investor) {
		//Se ordena por message NO POR ACTIVITY (no se tiene en cuenta amistad!!!)
		((SimulateSocialExchange)investor.getRoot()).setSortInvestorByActivity(
				Investors.sortByMessageReputation(investor.getScape()));
		
		long messageStatistics[][] = {{0,0,0,0,0,0,0}, {0,0,0,0,0,0,0}};		
		double reputationByAgentType[][][] = new double[2][2][5 + 
		((SimulateSocialExchange)investor.getRoot()).getSortInvestorByActivity().get(0).getMessageReputation().length];		                                           		
		for(int i = 0; i < ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByActivity().size(); i++) {
			Investors cell = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByActivity().get(i);
			System.out.print("  id:" + cell.getId() + cell.getAgentTypeToString() + " with messages[");
			int statistics[][] = messageStatistics(cell);
			int historyStatistics[][] = cell.historyMessageStatistics();
			for(int j = 0; j < statistics.length; j++) {
				for(int k = 0; k < statistics[j].length; k++) {
					messageStatistics[j][k] += statistics[j][k] + historyStatistics[j][k];
					System.out.print(statistics[j][k]+"-");
				}
				System.out.print(";");
			}
			System.out.print("],play:" + cell.getPlays() + ", activ rep:");
			for(int j = 0; j < cell.getActivityReputation().length; j++) {
				System.out.print(String.format("%.2f[", cell.getActivityReputation()[j]));
				int index = j/cell.getFriendReputation().length;
				System.out.print(String.format("m:%.2f,", cell.getMessageReputation()[index]));
				System.out.print(String.format("f:%.2f],", 
						cell.getFriendReputation()[j%cell.getFriendReputation().length]));				
			}				
			System.out.println("("+cell.getSizeMessageByLimit()+")");
			
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]][0]++;
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]][1] += cell.getPlays();
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]][2] += 
				cell.getNumMensajes() + cell.getMessagesHistory();
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]][3] +=
				cell.getPostProbability();
			reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]][4] +=
				cell.getGoodMessageProbability();
			for(int j = 0; j < cell.getMessageReputation().length; j++) {
				reputationByAgentType[cell.getAgentType()[1]][cell.getAgentType()[2]][j+5] += 
					cell.getMessageReputation()[j];
			}
		}
		for(int i = 0; i < reputationByAgentType.length; i++) {
			for(int j = 0; j < reputationByAgentType[i].length; j++) {
				printReputationByAgentType(i,j,reputationByAgentType[i][j]);
			}
		}				
		printMessageStatistics(messageStatistics);
	}
	
	static public void investorsStatistics(Investors investor) {
		double intelligentStatistics[][][] = new double[5][2][9]; 
		//0=IMP,1=PER,2=ANX,3=MEM,4=DIV; 0=BUY,1=SELL,2=NUM,3=ROI,4=CAP,5=LIQ,6=CapNegReturn
		double investorStatistics[][] = new double[3][13]; 
			//0=EXP_INV,1=AMA_INV,2=RA_INV; 0=BUY,1=SELL,2=NUM,3=ROI,4=CAP,5=liquidity,
			//  6=capitalWithNegativeReturn,7=buyProfibility,sellRange,X	
		for(int i = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByFinance().size()-1; i >= 0; i--) {
			Investors cell = ((SimulateSocialExchange)investor.getRoot()).getSortInvestorByFinance().get(i);			
			/* This process is done in generateReputation()
			double capital = cell.investor.getActualCapital(ibex35);
			cell.investor.addCapitalToHistory(capital);
			*/
			double capital = cell.getInvestor().getLastCapital();			
			System.out.println("  id:" + cell.getId() + cell.getAgentTypeToString() + " with financial reputation:" +
					cell.getInvestor().getFinancialReputation() );
			investorStatistics[cell.getAgentType()[0]][0] += cell.getInvestor().buys;
			investorStatistics[cell.getAgentType()[0]][1] += cell.getInvestor().sells;
			investorStatistics[cell.getAgentType()[0]][2]++;
			investorStatistics[cell.getAgentType()[0]][3] += cell.getInvestor().getFinancialReputation();					
			investorStatistics[cell.getAgentType()[0]][4] += capital;
			investorStatistics[cell.getAgentType()[0]][5] += cell.getInvestor().liquidity;
			investorStatistics[cell.getAgentType()[0]][6] += cell.getInvestor().investCapital;
			investorStatistics[cell.getAgentType()[0]][7] += cell.getInvestor().capitalWithNegativeReturn;
			investorStatistics[cell.getAgentType()[0]][8] += cell.getInvestor().sellsAll1;
			investorStatistics[cell.getAgentType()[0]][9] += cell.getInvestor().sellsAll0;
			investorStatistics[cell.getAgentType()[0]][10] += cell.getInvestor().withoutLiquidity;
			investorStatistics[cell.getAgentType()[0]][11] += cell.getInvestor().withZero;			
			if(cell.getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR) {
				investorStatistics[cell.getAgentType()[0]][12] += cell.getInvestor().rentabilityToBuy;						
			}
			else if(cell.getAgentType()[0] == Investors.AMATEUR_INVESTOR)
				investorStatistics[cell.getAgentType()[0]][12] += cell.getInvestor().sellTable[0][0];
			if(cell.getInvestor() instanceof IntelligentInvestor) {
				IntelligentInvestor intelligentCell = (IntelligentInvestor)cell.getInvestor();
				intelligentStatistics[0][intelligentCell.impulsive?1:0][0] += intelligentCell.buys;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][1] += intelligentCell.sells;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][2]++;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][3] += intelligentCell.getFinancialReputation();
				intelligentStatistics[0][intelligentCell.impulsive?1:0][4] += capital;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][5] += intelligentCell.liquidity;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][6] += intelligentCell.capitalWithNegativeReturn;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][7] += intelligentCell.withoutLiquidity;
				intelligentStatistics[0][intelligentCell.impulsive?1:0][8] += intelligentCell.withZero;
				
				intelligentStatistics[1][intelligentCell.perception?1:0][0] += intelligentCell.buys;
				intelligentStatistics[1][intelligentCell.perception?1:0][1] += intelligentCell.sells;
				intelligentStatistics[1][intelligentCell.perception?1:0][2]++;
				intelligentStatistics[1][intelligentCell.perception?1:0][3] += intelligentCell.getFinancialReputation();
				intelligentStatistics[1][intelligentCell.perception?1:0][4] += capital;
				intelligentStatistics[1][intelligentCell.perception?1:0][5] += intelligentCell.liquidity;
				intelligentStatistics[1][intelligentCell.perception?1:0][6] += intelligentCell.capitalWithNegativeReturn;
				intelligentStatistics[1][intelligentCell.perception?1:0][7] += intelligentCell.withoutLiquidity;
				intelligentStatistics[1][intelligentCell.perception?1:0][8] += intelligentCell.withZero;
				
				intelligentStatistics[2][intelligentCell.anxiety?1:0][0] += intelligentCell.buys;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][1] += intelligentCell.sells;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][2]++;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][3] += intelligentCell.getFinancialReputation();
				intelligentStatistics[2][intelligentCell.anxiety?1:0][4] += capital;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][5] += intelligentCell.liquidity;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][6] += intelligentCell.capitalWithNegativeReturn;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][7] += intelligentCell.withoutLiquidity;
				intelligentStatistics[2][intelligentCell.anxiety?1:0][8] += intelligentCell.withZero;
				
				intelligentStatistics[3][intelligentCell.memory?1:0][0] += intelligentCell.buys;
				intelligentStatistics[3][intelligentCell.memory?1:0][1] += intelligentCell.sells;				
				intelligentStatistics[3][intelligentCell.memory?1:0][2]++;
				intelligentStatistics[3][intelligentCell.memory?1:0][3] += intelligentCell.getFinancialReputation();
				intelligentStatistics[3][intelligentCell.memory?1:0][4] += capital;
				intelligentStatistics[3][intelligentCell.memory?1:0][5] += intelligentCell.liquidity;
				intelligentStatistics[3][intelligentCell.memory?1:0][6] += intelligentCell.capitalWithNegativeReturn;
				intelligentStatistics[3][intelligentCell.memory?1:0][7] += intelligentCell.withoutLiquidity;
				intelligentStatistics[3][intelligentCell.memory?1:0][8] += intelligentCell.withZero;
				
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][0] += intelligentCell.buys;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][1] += intelligentCell.sells;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][2]++;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][3] += intelligentCell.getFinancialReputation();
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][4] += capital;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][5] += intelligentCell.liquidity;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][6] += intelligentCell.capitalWithNegativeReturn;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][7] += intelligentCell.withoutLiquidity;
				intelligentStatistics[4][intelligentCell.isDiversifier?1:0][8] += intelligentCell.withZero;
			}			
		}
		printInvestorsStatistics(investorStatistics, intelligentStatistics);
	}
	
	static public void printInvestorsStatistics(double investorStatistics[][], double intelligentStatistics[][][]) {
		System.out.println(" PRU_INV("+investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+"):B:"
				+investorStatistics[Investors.EXPERIMENTED_INVESTOR][0]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",S:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][1]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",Rf:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][3]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",Ca:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][4]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",La:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][5]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",IC:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][6]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",CWN:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][7]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",RC:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][12]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",SA1:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][8]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",SA0:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][9]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",WhL:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][10]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]+
				",WZ:"+investorStatistics[Investors.EXPERIMENTED_INVESTOR][11]/investorStatistics[Investors.EXPERIMENTED_INVESTOR][2]				
		);
		System.out.println(" AMA_INV("+investorStatistics[Investors.AMATEUR_INVESTOR][2]+"):"+
				investorStatistics[Investors.AMATEUR_INVESTOR][0]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][1]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][3]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][4]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][5]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][6]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][7]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][12]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][8]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][9]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][10]/investorStatistics[Investors.AMATEUR_INVESTOR][2]+
				","+investorStatistics[Investors.AMATEUR_INVESTOR][11]/investorStatistics[Investors.AMATEUR_INVESTOR][2]
		);
		System.out.println(" RAM_INV("+investorStatistics[Investors.RANDOM_INVESTOR][2]+"):"+
				investorStatistics[Investors.RANDOM_INVESTOR][0]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][1]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][3]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][4]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][5]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][6]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][7]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][10]/investorStatistics[Investors.RANDOM_INVESTOR][2]+
				","+investorStatistics[Investors.RANDOM_INVESTOR][11]/investorStatistics[Investors.RANDOM_INVESTOR][2]
		);
		System.out.println(" IMPULSIVE -> ("+intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][0]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][1]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][3]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][4]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][5]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][6]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][7]/intelligentStatistics[0][0][2]+","+
				intelligentStatistics[0][0][8]/intelligentStatistics[0][0][2]+") vs ("+
				intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][0]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][1]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][3]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][4]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][5]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][6]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][7]/intelligentStatistics[0][1][2]+","+
				intelligentStatistics[0][1][8]/intelligentStatistics[0][1][2]+")");
		System.out.println(" PERCEPTION -> ("+intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][0]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][1]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][3]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][4]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][5]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][6]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][7]/intelligentStatistics[1][0][2]+","+
				intelligentStatistics[1][0][8]/intelligentStatistics[1][0][2]+") vs ("+
				intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][0]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][1]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][3]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][4]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][5]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][6]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][7]/intelligentStatistics[1][1][2]+","+
				intelligentStatistics[1][1][8]/intelligentStatistics[1][1][2]+")");
		System.out.println(" ANXIETY -> ("+intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][0]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][1]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][3]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][4]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][5]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][6]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][7]/intelligentStatistics[2][0][2]+","+
			intelligentStatistics[2][0][8]/intelligentStatistics[2][0][2]+") vs ("+
			intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][0]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][1]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][3]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][4]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][5]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][6]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][7]/intelligentStatistics[2][1][2]+","+
			intelligentStatistics[2][1][8]/intelligentStatistics[2][1][2]+")");
		System.out.println(" MEMORY -> ("+intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][0]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][1]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][3]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][4]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][5]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][6]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][7]/intelligentStatistics[3][0][2]+","+
			intelligentStatistics[3][0][8]/intelligentStatistics[3][0][2]+") vs ("+
			intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][0]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][1]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][3]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][4]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][5]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][6]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][7]/intelligentStatistics[3][1][2]+","+
			intelligentStatistics[3][1][8]/intelligentStatistics[3][1][2]+")");
		System.out.println(" DIVERSIFIER -> ("+intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][0]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][1]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][3]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][4]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][5]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][6]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][7]/intelligentStatistics[4][0][2]+","+
			intelligentStatistics[4][0][8]/intelligentStatistics[4][0][2]+") vs ("+
			intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][0]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][1]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][3]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][4]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][5]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][6]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][7]/intelligentStatistics[4][1][2]+","+
			intelligentStatistics[4][1][8]/intelligentStatistics[4][1][2]+")");		
	}
	
	static public void printReputationByAgentType (int activityType, int writerType, double reputation[]) {
		String agentType = "[";		
		if(activityType == Investors.FREQUENT_USER)
			agentType += "FRE_US";
		else
			agentType += "OCA_US";
		agentType += ":"+String.format("%.3f", reputation[3]/reputation[0])+",";
		if(writerType == Investors.GOOD_WRITER)
			agentType += "GOOD_WR";
		else
			agentType += "BAD_WRI";
		agentType += ":"+String.format("%.3f", reputation[4]/reputation[0])+"]";
		System.out.print("  " + agentType + " size:" + reputation[0] + " play:" +
				String.format("%.2f", reputation[1]/reputation[0]) + " mes:" +
				String.format("%.1f", reputation[2]/reputation[0]) + " activity rep:");
		for(int i = 5; i < reputation.length; i++)
			System.out.print(reputation[i]/reputation[0]+",");
		System.out.println();
	}
	
	static public void printMessageStatistics(long messageStatistics[][]) {
		String messageString = "  Good Messages:" + messageStatistics[0][0] + ", R:" + 
				messageStatistics[0][1] + ", uniqR:" +	messageStatistics[0][2];
		System.out.println(messageString);
		messageString = "  Bad Messages:" + messageStatistics[1][0] + ", R:" + 
				messageStatistics[1][1] + ", uniqR:" + 	messageStatistics[1][2];
		System.out.println(messageString);
	}
	
	static public void printIbex35Statistics(Ibex35 ibex35) {
		for(Share share : ibex35.getShares().values()) {
			if(share instanceof RandomShare) {
				RandomShare ramShare = (RandomShare) share;
				System.out.println(ramShare.getName()+": "+ramShare.getValue()+",maxR:"+ramShare.getMaxReached()
						+",minR:"+ramShare.getMinReached()+",vU:"+ramShare.variationUp+",vD:"+
						ramShare.variationDown+","+ramShare.getVariationsHistory());
			} else {
				HistoryFileShare fileShare = (HistoryFileShare) share;
				System.out.println(fileShare.getName()+": "+fileShare.getValue()+","+fileShare.getVariationsHistory());
			}
		}
	}	
	
	static public int[][] messageStatistics(Investors cell) {
		int statistics[][] = new int[2][7];
		statistics[0][0] = 0; statistics[0][1] = 0; statistics[0][2] = 0; statistics[0][3] = 0; 
		statistics[0][4] = 0; statistics[0][5] = 0; statistics[0][6] = 0;
		statistics[1][0] = 0; statistics[1][1] = 0; statistics[1][2] = 0; statistics[1][3] = 0; 
		statistics[1][4] = 0; statistics[1][5] = 0; statistics[1][6] = 0;
		for(Message message : cell.getMessages()) {
			if((cell.getIteration()-message.getDate()) < Properties.TIME_LIMIT) {
				if(message.isGood()) {
					statistics[0][0]++;
					statistics[0][1] += message.getNumReaders();
					statistics[0][2] += message.getUniqueNumReaders();
					statistics[0][3] += message.getNumFollowers();
					statistics[0][4] += message.getUniqueNumFollowers();
					statistics[0][5] += message.getScores().size();
					statistics[0][6] += message.getScore();
				} else {
					statistics[1][0]++;
					statistics[1][1] += message.getNumReaders();
					statistics[1][2] += message.getUniqueNumReaders();
					statistics[1][3] += message.getNumFollowers();
					statistics[1][4] += message.getUniqueNumFollowers();
					statistics[1][5] += message.getScores().size();
					statistics[1][6] += message.getScore();
				}
			}
		}
		return statistics;
	}
	
}

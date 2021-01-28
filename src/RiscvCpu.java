import java.util.Scanner;
import java.util.HashMap;

public class RiscvCpu {
	public HashMap<String, Integer> labels = new HashMap<>();// to find the memory locations of the labels
	
	int numberOfInstructions = 0;// we are going to use it when calculating CPI
	
	public String[] InstructionMemory = new String[4*128]; // we are now supporting 32 instructions in our instruction memory (daha çok mu yapsak???)

	public long[] DataMemory = new long[65536];   //we can decide on the size of data memory

	public int pc = -4;   //program counter(instructionFetch uses it, but instruction decode increments it by 4 at first so it starts with 0)
	public int old_pc = -4;

	public long[] registers = new long[32]; //registers[i]=xi
	

	public String IF_ID_type="nop";     //type of instruction in IF_ID initiated as nop (when pipeline is empty at first, all stages should apply nop)
	public String ID_EX_type="nop";	//type of instruction in IF_EX initiated as nop
	public String EX_MEM_type="nop";	//type of instruction in EX_MEM initiated as nop
	public String MEM_WB_type="nop";	//type of instruction in MEM_WB initiated as nop
	public String WB_END_type="nop";

	public String IF_ID_rd;  //second token of the instruction
	public String IF_ID_rs1;  //third token of the instruction
	public String IF_ID_rs2;  //last token of the instruction   (immidiate in ld and sd)
	public boolean IF_ID_flush=false;  //flushing requirement will be signaled 
	public boolean IF_ID_putStall=false;   //if true, make ID_EX_type=nop and pc remains same

	public int ID_EX_rd;  //enumeration of the second token of the instruction  (if x3->3, if x6->6)
	public long ID_EX_rs1;  //decoded value of the third token of the instruction   (if x3->registers[3] if x6->registers[6])
	public long ID_EX_rs2;  //decoded value of the last token of the instruction   (immidiate in ld and sd)
	public long ID_EX_storeData; // used only for store operation
	public int ID_EX_rs1_id; // not the value but the id of rs1 register
	public int ID_EX_rs2_id;// not the value but the id of rs2 register
	

	public int EX_MEM_rd;  //enumeration of the second token of the instruction  (if x3->3, if x6->6)
	public long EX_MEM_aluResult; // the result of the ALU operation
	public long EX_MEM_storeData; // used only for store operation

	public int MEM_WB_rd;
	public long MEM_WB_wbData; // writeback data which we put it into the rd of this stage, later

	public int WB_END_rd;
	public long WB_END_wbData;
	

	public int cycle = 0; // cycle counter

	public int executedIns = 0;

	public int stall=0;  //stall counter

	boolean endSignal=false;  //WİLL BE TRUE WHEN instruction memory comes to an end

	Executor ex;


	
	RiscvCpu(String code){
		
		//tokenizing the input
		tokenize(code);

		//Initialization of data structures
		for(int i=0;i<32;i++){
			registers[i]=0;
		}
		for(int i=0;i<65536;i++){
			DataMemory[i]=0;
		}
		//  DataMemory[0] = 3;
		//  DataMemory[1] = 6;
		//  DataMemory[2] = 3;
		//  DataMemory[10] = 105;
		//  DataMemory[6] = 7;

       
		ex=new Executor();
		
	}


	public void run(){
		while(true){
			ex.writebackOperations(this);
			ex.memoryOperations(this);
			ex.executionALU(this);
			ex.instructionDecode(this);
			ex.instructionFetch(this);
			if(IF_ID_type.equals("nop") && ID_EX_type.equals("nop") && EX_MEM_type.equals("nop") && MEM_WB_type.equals("nop") && WB_END_type.equals("nop")){
				break;
			}
			cycle++;
		}
	}

	public void showStats(){
		for(int i=0;i<32;i++){
			System.out.print(registers[i]+" ");
		}
		System.out.println("");
		for(int i=0;i<32;i++){
			System.out.print(DataMemory[i]+" ");
		}
		System.out.println("Stall: "+stall);
		System.out.println("Cycle: "+cycle);
		System.out.println("Number of Instructions: "+executedIns);
		System.out.println("CPI: "+((double)cycle/executedIns));
	}



	public void tokenize(String code){

		Scanner scanner = new Scanner(code);
		String line;
		Scanner token;
		int indexCursor = 0;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			token = new Scanner(line);
			if (!token.hasNext()) {// for empty lines
				continue;
			}
			String first = token.next().toLowerCase(); // in case if it is given as upper case
			
			// means instruction
			if(first.equals("add")|| first.equals("addi")||first.equals("sub")||first.equals("and")|| first.equals("or")||first.equals("beq")) {
				numberOfInstructions++;
				InstructionMemory[indexCursor] = first;
				String second = token.next().toLowerCase();
				InstructionMemory[indexCursor+1] = second.substring(0,second.indexOf(",")); // if no comma it can be problematic
				String third = token.next().toLowerCase();
				InstructionMemory[indexCursor+2] = third.substring(0,third.indexOf(",")); // if no comma it can be problematic
				String fourth = token.next().toLowerCase();
				InstructionMemory[indexCursor+3] = fourth; // if no comma it can be problematic
				
				indexCursor +=4;
			}else if(first.equals("ld") || first.equals("sd")) {
				numberOfInstructions++;
				InstructionMemory[indexCursor] = first;
				String second = token.next().toLowerCase();
				InstructionMemory[indexCursor+1] = second.substring(0,second.indexOf(",")); // if no comma it can be problematic
				String third = token.next().toLowerCase();
				String element1 = third.substring(0, third.indexOf('('));
				String element2 = third.substring(third.indexOf('(')+1, third.indexOf(')'));
				InstructionMemory[indexCursor+2] = element2; // which is register
				InstructionMemory[indexCursor+3] = element1; // which is immediate value
				indexCursor +=4;
			}else if(line.indexOf(":") != -1) {// it means there is a label
				line = line.trim().substring(0, line.indexOf(":"));
				labels.put(line, indexCursor);
				continue;
				
			}else {
				System.out.println("error");
				System.exit(0);
			}
			
			
			token.close();
		}
		
		scanner.close();
	}
	
	
	

}

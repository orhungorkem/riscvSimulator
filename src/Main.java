import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
public class Main {

	
	public static void main(String[] args) throws IOException{
		String code = parser("input.txt");
		
		RiscvCpu cpu = new RiscvCpu(code);   
		cpu.run();
		cpu.showStats();
		
		
	}
	
	private static String parser(String xx) throws FileNotFoundException, IOException {
		String fileAsString;
		BufferedReader br = new BufferedReader(new FileReader(xx));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			// got rid of comments in Assembly code
			if (line.indexOf(';') != -1) {
				line = line.substring(0, line.indexOf(';'));
			}
			sb.append(line).append("\n");
			line = br.readLine();
		}
		fileAsString = sb.toString(); // this is the whole assembly code as a String
		br.close();
		return fileAsString;
	}
}

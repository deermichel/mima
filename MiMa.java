import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
	Minimal Machine Emulator

	@author Micha Hanselmann
	@version 1.0.0
*/
public class MiMa {

	// accumulator
	private int accu = 0;

	// memory
	private HashMap<String, Integer> memory = new HashMap<>();


	// main method
	public static void main(String[] args) {
		try {
			new MiMa(args[0]);	// filename via arg
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MiMa(String filename) throws IOException {

		// display header
		System.out.println("MiMa Emulator (c) 2016 Micha Hanselmann");
		System.out.println("------------------------------------");
		System.out.println(" Executing " + filename);
		System.out.println("------------------------------------");

		// read file and init pointer
		String[] file = new String(Files.readAllBytes(Paths.get(filename))).replaceAll("\r", "").toUpperCase().split("\n");
		int i = -1;

		// main loop
		while (++i < file.length) {

			// get line, skip empty
			String line = file[i];
			if (line.equals("")) continue;

			// log line
			System.out.println(line);

			// get command and parameter
			String command = line.split(" ")[0];
			String parameter = "";
			if (line.split(" ").length > 1) parameter = line.split(" ")[1];

			// LDC = load constant into accumulator
			if (command.equals("LDC")) {
				accu = Integer.valueOf(parameter);
  			System.out.println(" | accu: " + accu);
			}

			// STV = store value in memory
			else if (command.equals("STV")) {
				memory.put(parameter, accu);
  			System.out.println(" | " + parameter + ": " + accu);
			}

			// LDV = load value into accumulator
      else if (command.equals("LDV")) {
        accu = memory.get(parameter);
  			System.out.println(" | accu: " + accu);
      }

			// STIV = store value in memory (indirect)
      else if (command.equals("STIV")) {
				memory.put(Integer.toString(memory.get(parameter)), accu);
  			System.out.println(" | " + memory.get(parameter) + ": " + accu);
      }

			// LDIV = load value into accumulator (indirect)
      else if (command.equals("LDIV")) {
        accu = memory.get(Integer.toString(memory.get(parameter)));
  			System.out.println(" | accu: " + accu);
      }

			// ADD = add accumulator and memory, result stored in accumulator
			else if (command.equals("ADD")) {
				accu += memory.get(parameter);
  			System.out.println(" | accu: " + accu);
			}

			// AND = do bitwise AND of accumulator and memory, result stored in accumulator
      else if (command.equals("AND")) {
        accu &= memory.get(parameter);
  			System.out.println(" | accu: " + accu);
      }

			// OR = do bitwise OR of accumulator and memory, result stored in accumulator
      else if (command.equals("OR")) {
        accu |= memory.get(parameter);
  			System.out.println(" | accu: " + accu);
      }

			// XOR = do bitwise XOR of accumulator and memory, result stored in accumulator
      else if (command.equals("XOR")) {
        accu ^= memory.get(parameter);
  			System.out.println(" | accu: " + accu);
      }

			// NOT = bitwise negation of accumulator
      else if (command.equals("NOT")) {
        accu = ~accu;
  			System.out.println(" | accu: " + accu);
      }

			// RAR = rotate bits of accumulator right by one position
      else if (command.equals("RAR")) {
        accu >>= 1;
  			System.out.println(" | accu: " + accu);
      }

			// EQL = compare accumulator with memory, -1 if same, 0 else
      else if (command.equals("EQL")) {
        accu = (accu == memory.get(parameter)) ? -1 : 0;
  			System.out.println(" | accu: " + accu);
      }

			// JMN = jump to label if accumulator is negative
			else if (command.equals("JMN")) {
				if (accu < 0) {
					for (i = 0; i < file.length; i++) {
						if (file[i].equals(":" + parameter)) {
							break;
						}
					}
				} else {
					System.out.println(" | ignore jump");
				}
				continue;
			}

			// JMP = jump to label
			else if (command.equals("JMP")) {
				for (i = 0; i < file.length; i++) {
					if (file[i].equals(":" + parameter)) {
						break;
					}
				}
				continue;
			}

		}

		// display final state
		System.out.println("------------------------------------");
		System.out.println(" Memory (final state)");
		System.out.println("------------------------------------");
		for (String key : memory.keySet()) {
			System.out.println(key + ": " + memory.get(key));
		}

	}

}

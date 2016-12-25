import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Minimal Machine Emulator
 *
 * @author Micha Hanselmann
 * @author Nico Weidmann
 * @version 1.1.0
 */
public class MiMa {

	// accumulator
	private int accu = 0;

	// memory
	private HashMap<Integer, Integer> memory = new HashMap<>();

	//constants
	private HashMap<String, Integer> constants = new HashMap<>();


	// main method
	public static void main(String[] args) {
		try {

			new MiMa(args[0]); // filename via arg

		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Please pass the path to your code file as an argument:\n"
					+ "java MiMa path/to/your/file.*");
			System.exit(1);
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
			if (line.equals(""))
				continue;

			// log line
			System.out.println(line);

			// get command and parameter
			String command = line.split(" ")[0];
			String parameterStr = "";
			int parameter = -1;

			if (line.split(" ").length > 1)
				parameterStr = line.split(" ")[1];

			// skip comments
			if (command.startsWith("//")) {
				continue;
			}

			else if (command.equals("#DEF")) {
				try {

					// constants must begin with a letter
					char firstChar = parameterStr.split("=")[0].charAt(0);
					if (firstChar >= 'A' && Character.isLetter(firstChar)) {
						constants.put(parameterStr.split("=")[0], Integer.parseInt(parameterStr.split("=")[1]));
					} else {
						System.out.println("ERROR: constants must begin with a character.");
						break;
					}

				} catch (NumberFormatException e) {
					System.out.println("ERROR: illegal constant definition."
							+ "\nExample of a proper definition:\n#DEF A=1337");
					break;
				}
				continue;
			}

			// initialize parameter by translating constant or parsing an int
			if (parameterStr.length() > 0) {

				if(constants.containsKey(parameterStr)) {
					parameter = constants.get(parameterStr);

				} else if (!(command.equals("JMP") || command.equals("JMN"))) {
					try {
						parameter = Integer.parseInt(parameterStr);
					} catch (NumberFormatException e) {
						System.out.println("ERROR: illegal argument '" + parameterStr
								+ "'.\nConstant might not have been initialized.");
						break;
					}
				}

			}

			// catch exception when trying to load an invalid address from memory
			try {

				// LDC = load constant into accumulator
				if (command.equals("LDC")) {
					accu = Integer.valueOf(parameter);
					if (accu > 0xFFFFF || accu < 0) {
						// loading illegal constant (longer than 20 bit)
						System.out.println("WARNING: trying to load illegal constant '" + accu +
								"'. This constant is out of range.\nSubsequent output might be incorrect.");
					}
					System.out.println(" | accu: " + accu);
				}

				// STV = store value in memory
				else if (command.equals("STV")) {
					memory.put(parameter, accu);
					System.out.println(" | " + parameterStr + ": " + accu);
				}

				// LDV = load value into accumulator
				else if (command.equals("LDV")) {
					accu = memory.get(parameter);
					System.out.println(" | accu: " + accu);
				}

				// STIV = store value in memory (indirect)
				else if (command.equals("STIV")) {
					int val = memory.get(parameter);
					if (val != (val & 0xFFFFF)) {
						// value longer than 20 bit
						System.out.println("ERROR: trying to load from nonexistent address '" + val + "'.");
						break;
					}
					memory.put(val, accu);
					System.out.println(" | " + parameterStr + ": " + memory.get(parameter) + ": " + accu);
				}

				// LDIV = load value into accumulator (indirect)
				else if (command.equals("LDIV")) {
					int val = memory.get(parameter);
					if (val != (val & 0xFFFFF)) {
						// value longer than 20 bit
						System.out.println("ERROR: trying to load from nonexistent address '" + val + "'.");
						break;
					}
					try {
						accu = memory.get(Integer.toString(val));
					} catch (NullPointerException e) {
						System.out.println("------------------------------------");
						System.out.println("ERROR: Illegal parameter '" + Integer.toString(val)
								+ "'.\nThere might be no value assigned to this address.");
						break;
					}
					System.out.println(" | accu: " + accu);
				}

				// ADD = add accumulator and memory, result stored in accumulator
				else if (command.equals("ADD")) {
					accu += memory.get(parameter);
					accu = conv24Bit(accu);
					System.out.println(" | accu: " + accu);
				}

				// AND = do bitwise AND of accumulator and memory, result stored in accumulator
				else if (command.equals("AND")) {
					accu &= memory.get(parameter);
					accu = conv24Bit(accu);
					System.out.println(" | accu: " + accu);
				}

				// OR = do bitwise OR of accumulator and memory, result stored in accumulator
				else if (command.equals("OR")) {
					accu |= memory.get(parameter);
					accu = conv24Bit(accu);
					System.out.println(" | accu: " + accu);
				}

				// XOR = do bitwise XOR of accumulator and memory, result stored in accumulator
				else if (command.equals("XOR")) {
					accu ^= memory.get(parameter);
					accu = conv24Bit(accu);
					System.out.println(" | accu: " + accu);
				}

				// NOT = bitwise negation of accumulator
				else if (command.equals("NOT")) {
					accu = ~accu;
					accu = conv24Bit(accu);
					System.out.println(" | accu: " + accu);
				}

				// RAR = rotate bits of accumulator right by one position
				else if (command.equals("RAR")) {
					accu = ((accu & 0xFFFFFF) >> 1) | ((accu & 1) << 23); // shift last 24 bits
					accu = conv24Bit(accu);
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
							if (file[i].equals(":" + parameterStr)) {
								break;
							}
						}
						// no label found - terminate with error
						System.out.println("------------------------------------");
						System.out.println("ERROR: label '" + parameterStr + "' does not exist.");
						break;
					} else {
						System.out.println(" | ignore jump");
					}
					continue;
				}

				// JMP = jump to label
				else if (command.equals("JMP")) {
					boolean found = false;
					for (i = 0; i < file.length; i++) {
						if (file[i].equals(":" + parameterStr)) {
							found = true;
							break;
						}
					}
					if (!found) {
						// no label found - terminate with error
						System.out.println("------------------------------------");
						System.out.println("ERROR: label '" + parameterStr + "' does not exist.");
						break;
					}
					continue;
				}

				// HALT = terminate the program
				else if (command.equals("HALT")) {
					break;
				}

			} catch (NullPointerException e) {
				// illegal call to memory occurred - show error and terminate
				System.out.println("------------------------------------");
				System.out.println("ERROR: Illegal parameter '" + Integer.toString(parameter)
						+ "'.\nThere might be no value assigned to this address.");
			}

		}

		// reached end of file

		// display final state
		System.out.println("====================================");
		System.out.println(" Memory (final state)");
		System.out.println("------------------------------------");
		System.out.println(" accu: " + accu);
		System.out.println("------------------------------------");
		for (int key : memory.keySet()) {
			String keyStr = "" + key;
			if (constants.containsValue(key)) {
				// retranslate address to constant (maybe there is a better solution :/)
				for (String con : constants.keySet()) {
					if (constants.get(con) == key) {
						keyStr = con;
						break;
					}
				}
			}
			System.out.println(" " + keyStr + ": " + memory.get(key));
		}
		System.out.println("====================================");

	}

	// 24bit conversion helper method
	private int conv24Bit(int i) {

		// cut off 8 highest bits
		i = i & 0xFFFFFF;
		if (Integer.highestOneBit(i) == Math.pow(2, 23)) {
			// new value is negative
			i |= 0xFF000000; // convert to signed 32 bit int
		}

		return i;
	}

}

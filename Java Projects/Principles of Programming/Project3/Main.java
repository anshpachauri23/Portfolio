class Main {
	public static void main(String[] args) {
		// Initialize the scanner with the source code file.
		Scanner scanner = new Scanner(args[0]);
		Parser.scanner = scanner;

		// Parse and validate the entire procedure.
		Procedure p = new Procedure();
		p.parse();
		p.validate();

		// Initialize the memory manager with the data file and execute the procedure.
		MemoryManager memory = new MemoryManager(args[1]);
		p.execute(memory);
	}
}

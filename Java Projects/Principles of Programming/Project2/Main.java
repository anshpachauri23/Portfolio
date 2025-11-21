class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner("Correct/27.code");
		Parser.scanner = scanner;
		Procedure p = new Procedure();
		p.parse();
		p.validate(); // Perform semantic checks
		p.print();
	}
}
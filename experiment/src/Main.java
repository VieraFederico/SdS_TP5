public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            RunConfig config = RunConfig.parse(args);
            if (config.help()) {
                System.out.print(RunConfig.usage());
            } else {
                ExperimentRunner.run(config);
            }
        } catch (Exception error) {
            System.err.println("TP5: " + error.getMessage());
            System.exit(1);
        }
    }
}

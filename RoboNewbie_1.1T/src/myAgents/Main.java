package myAgents;

import examples.Attacker_Agent;
import examples.GoalKeeper_Agent;

public class Main {
    public static void main(String args[]) {

        GoalKeeper_Agent goalKeeperAgent = new GoalKeeper_Agent();
        Attacker_Agent attacker = new Attacker_Agent();

        goalKeeperAgent.init();
        attacker.init();

        goalKeeperAgent.run(12);
        attacker.run(12);

        goalKeeperAgent.printlog();
        attacker.printlog();

        System.out.println("Agents stopped.");
    }
}

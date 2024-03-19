package com.ebicep.warlordsbalancer;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ebicep.warlordsbalancer.Balancer.*;

public class ExtraBalanceFeatureTest {

    @Test
    public void testSwapUnevenTeams() {
        Printer printer = new Printer(System.out::println, new Color() {});
        Map<Team, TeamBalanceInfo> teamBalanceInfos = new HashMap<>();
        teamBalanceInfos.put(Team.RED, new TeamBalanceInfo());
        teamBalanceInfos.put(Team.BLUE, new TeamBalanceInfo());
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRYOMANCER, 1.07)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRYOMANCER, 0.88)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRUSADER, 8)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRUSADER, 0.91)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRUSADER, 0.73)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.AVENGER, 0.8)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.PROTECTOR, 7.58)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.EARTHWARDEN, 0.7)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.PYROMANCER, 7.79)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRUSADER, 0.75)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.REVENANT, 0.69)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.DEFENDER, 1.85)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRYOMANCER, 3.47)));
        teamBalanceInfos.get(Team.BLUE).addPlayer(new DebuggedPlayer(new Player(Specialization.CRUSADER, 1.85)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.SPIRITGUARD, 1.25)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.PYROMANCER, 2.48)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.REVENANT, 2.48)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.EARTHWARDEN, 1.39)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.PROTECTOR, 0.84)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.AVENGER, 2.75)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.DEFENDER, 3.05)));
        teamBalanceInfos.get(Team.RED).addPlayer(new DebuggedPlayer(new Player(Specialization.EARTHWARDEN, 1.03)));
        Balancer.printBalanceInfo(printer, teamBalanceInfos);
        ExtraBalanceFeature.SWAP_UNEVEN_TEAMS.apply(printer, teamBalanceInfos);
        Balancer.printBalanceInfo(printer, teamBalanceInfos);
    }

}

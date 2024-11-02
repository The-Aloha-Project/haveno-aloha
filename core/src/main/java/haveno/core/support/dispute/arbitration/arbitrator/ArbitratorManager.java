/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Haveno.
 *
 * Haveno is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Haveno is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Haveno. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.core.support.dispute.arbitration.arbitrator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import haveno.common.config.Config;
import haveno.common.crypto.KeyRing;
import haveno.core.filter.FilterManager;
import haveno.core.support.dispute.agent.DisputeAgentManager;
import haveno.core.user.User;
import haveno.network.p2p.storage.payload.ProtectedStorageEntry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ArbitratorManager extends DisputeAgentManager<Arbitrator> {

    @Inject
    public ArbitratorManager(KeyRing keyRing,
                             ArbitratorService arbitratorService,
                             User user,
                             FilterManager filterManager) {
        super(keyRing, arbitratorService, user, filterManager);
    }

    @Override
    protected List<String> getPubKeyList() {
        switch (Config.baseCurrencyNetwork()) {
        case XMR_LOCAL:
            return List.of(
                    "027a381b5333a56e1cc3d90d3a7d07f26509adf7029ed06fc997c656621f8da1ee",
                    "024baabdba90e7cc0dc4626ef73ea9d722ea7085d1104491da8c76f28187513492",
                    "026eeec3c119dd6d537249d74e5752a642dd2c3cc5b6a9b44588eb58344f29b519");
        case XMR_STAGENET:
            return List.of(
                    "03bb559ce207a4deb51d4c705076c95b85ad8581d35936b2a422dcb504eaf7cdb0",
                    "026c581ad773d987e6bd10785ac7f7e0e64864aedeb8bce5af37046de812a37854",
                    "025b058c9f2c60d839669dbfa5578cf5a8117d60e6b70e2f0946f8a691273c6a36",
                    "036c7d3f4bf05ef39b9d1b0a5d453a18210de36220c3d83cd16e59bd6132b037ad",
                    "030f7122a10ff73cd73808bddace95be77a94189c8a0eb24586265e125ce5ce6b9",
                    "03aa23e062afa0dda465f46986f8aa8d0374ad3e3f256141b05681dcb1e39c3859",
                    "02d3beb1293ca2ca14e6d42ca8bd18089a62aac62fd6bb23923ee6ead46ac60fba",
                    "03fa0f38f27bdd324db6f933f7e57851dadf3b911e4db6b19dd0950492c4525a31",
                    "02a1a458df5acf4ab08fdca748e28f33a955a30854c8c1a831ee733dca7f0d2fcd",
                    "0374dd70f3fa6e47ec5ab97932e1cec6233e98e6ae3129036b17118650c44fd3de"
                );
        case XMR_MAINNET:
            return List.of(
                    "0259eb939d7ddd49fb5575e8c2b7dcff3ac9eee432b95d7a8ed05211c31c8f0260",
                    "0250bd83f4ef502b3cf44497f8553a9021c43278d869d06310aa843260c4733b8c",
                    "02acb5a62241f6eb2428aab090322bdc074ffe77bf656e8221f42f9ea1db34c8e6",
                    "02d96f8239f44f5b45846585eb062453f0af514d4e94324ffe783e8bdc89705b6e",
                    "03b1f75164b569227a38aa30d6711ffe702039c81a14d0fc8b87c08d2d1cd927ee",
                    "0294d8ac9e16f5c6b918e24ac8fe2c903d758290ba080b6cfef35fe60852c7940f",
                    "038e971d724bd4cfbba7e9d18d52d0cc377b30ac5aabe12298a62f85425dfcd5c6",
                    "026e457342a0af072bb87eba22a89a5cd9a6af3abdfcfe11aeeb3718c4a1a34407"
            );
        default:
            throw new RuntimeException("Unhandled base currency network: " + Config.baseCurrencyNetwork());
        }
    }

    @Override
    protected boolean isExpectedInstance(ProtectedStorageEntry data) {
        return data.getProtectedStoragePayload() instanceof Arbitrator;
    }

    @Override
    protected void addAcceptedDisputeAgentToUser(Arbitrator disputeAgent) {
        user.addAcceptedArbitrator(disputeAgent);
    }

    @Override
    protected void removeAcceptedDisputeAgentFromUser(ProtectedStorageEntry data) {
        user.removeAcceptedArbitrator((Arbitrator) data.getProtectedStoragePayload());
    }

    @Override
    protected List<Arbitrator> getAcceptedDisputeAgentsFromUser() {
        return user.getAcceptedArbitrators();
    }

    @Override
    protected void clearAcceptedDisputeAgentsAtUser() {
        user.clearAcceptedArbitrators();
    }

    @Override
    protected Arbitrator getRegisteredDisputeAgentFromUser() {
        return user.getRegisteredArbitrator();
    }

    @Override
    protected void setRegisteredDisputeAgentAtUser(Arbitrator disputeAgent) {
        user.setRegisteredArbitrator(disputeAgent);
    }
}

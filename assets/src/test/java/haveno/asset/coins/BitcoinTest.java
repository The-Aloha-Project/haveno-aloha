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

package haveno.asset.coins;

import haveno.asset.AbstractAssetTest;
import org.junit.jupiter.api.Test;

public class BitcoinTest extends AbstractAssetTest {

    public BitcoinTest() {
        super(new Bitcoin.Mainnet());
    }

    @Test
    public void testValidAddresses() {
        assertValidAddress("17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem");
        assertValidAddress("3EktnHQD7RiAE6uzMj2ZifT9YgRrkSgzQX");
        assertValidAddress("1111111111111111111114oLvT2");
        assertValidAddress("1BitcoinEaterAddressDontSendf59kuE");
        assertValidAddress("bc1qj89046x7zv6pm4n00qgqp505nvljnfp6xfznyw");
    }

    @Test
    public void testInvalidAddresses() {
        assertInvalidAddress("17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhemqq");
        assertInvalidAddress("17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYheO");
        assertInvalidAddress("17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhek#");
    }
}

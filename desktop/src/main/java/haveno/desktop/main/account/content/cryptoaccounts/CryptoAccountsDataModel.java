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

package haveno.desktop.main.account.content.cryptoaccounts;

import com.google.inject.Inject;
import haveno.common.file.CorruptedStorageFileHandler;
import haveno.common.proto.persistable.PersistenceProtoResolver;
import haveno.core.account.witness.AccountAgeWitnessService;
import haveno.core.locale.CryptoCurrency;
import haveno.core.locale.TraditionalCurrency;
import haveno.core.locale.TradeCurrency;
import haveno.core.offer.OpenOfferManager;
import haveno.core.payment.AssetAccount;
import haveno.core.payment.PaymentAccount;
import haveno.core.trade.TradeManager;
import haveno.core.user.Preferences;
import haveno.core.user.User;
import haveno.desktop.common.model.ActivatableDataModel;
import haveno.desktop.util.GUIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class CryptoAccountsDataModel extends ActivatableDataModel {

    private final User user;
    private final Preferences preferences;
    private final OpenOfferManager openOfferManager;
    private final TradeManager tradeManager;
    private final AccountAgeWitnessService accountAgeWitnessService;
    final ObservableList<PaymentAccount> paymentAccounts = FXCollections.observableArrayList();
    private final SetChangeListener<PaymentAccount> setChangeListener;
    private final String accountsFileName = "CryptoPaymentAccounts";
    private final PersistenceProtoResolver persistenceProtoResolver;
    private final CorruptedStorageFileHandler corruptedStorageFileHandler;

    @Inject
    public CryptoAccountsDataModel(User user,
                                    Preferences preferences,
                                    OpenOfferManager openOfferManager,
                                    TradeManager tradeManager,
                                    AccountAgeWitnessService accountAgeWitnessService,
                                    PersistenceProtoResolver persistenceProtoResolver,
                                    CorruptedStorageFileHandler corruptedStorageFileHandler) {
        this.user = user;
        this.preferences = preferences;
        this.openOfferManager = openOfferManager;
        this.tradeManager = tradeManager;
        this.accountAgeWitnessService = accountAgeWitnessService;
        this.persistenceProtoResolver = persistenceProtoResolver;
        this.corruptedStorageFileHandler = corruptedStorageFileHandler;
        setChangeListener = change -> fillAndSortPaymentAccounts();
    }

    @Override
    protected void activate() {
        user.getPaymentAccountsAsObservable().addListener(setChangeListener);
        fillAndSortPaymentAccounts();
    }

    private void fillAndSortPaymentAccounts() {
        if (user.getPaymentAccounts() != null) {
            paymentAccounts.setAll(user.getPaymentAccounts().stream()
                    .filter(paymentAccount -> paymentAccount.getPaymentMethod().isBlockchain())
                    .collect(Collectors.toList()));
            paymentAccounts.sort(Comparator.comparing(PaymentAccount::getAccountName));
        }
    }

    @Override
    protected void deactivate() {
        user.getPaymentAccountsAsObservable().removeListener(setChangeListener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI actions
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void onSaveNewAccount(PaymentAccount paymentAccount) {
        TradeCurrency singleTradeCurrency = paymentAccount.getSingleTradeCurrency();
        List<TradeCurrency> tradeCurrencies = paymentAccount.getTradeCurrencies();
        if (singleTradeCurrency != null) {
            if (singleTradeCurrency instanceof TraditionalCurrency)
                preferences.addTraditionalCurrency((TraditionalCurrency) singleTradeCurrency);
            else
                preferences.addCryptoCurrency((CryptoCurrency) singleTradeCurrency);
        } else if (tradeCurrencies != null && !tradeCurrencies.isEmpty()) {
            tradeCurrencies.forEach(tradeCurrency -> {
                if (tradeCurrency instanceof TraditionalCurrency)
                    preferences.addTraditionalCurrency((TraditionalCurrency) tradeCurrency);
                else
                    preferences.addCryptoCurrency((CryptoCurrency) tradeCurrency);
            });
        }

        if (paymentAccount.getAccountName() == null) throw new IllegalStateException("Account name cannot be null");
        user.addPaymentAccount(paymentAccount);
        paymentAccount.onPersistChanges();

        if (!(paymentAccount instanceof AssetAccount))
            accountAgeWitnessService.publishMyAccountAgeWitness(paymentAccount.getPaymentAccountPayload());
    }

    public void onUpdateAccount(PaymentAccount paymentAccount) {
        if (paymentAccount.getAccountName() == null) throw new IllegalStateException("Account name cannot be null");
        paymentAccount.onPersistChanges();
        user.requestPersistence();
    }

    public boolean onDeleteAccount(PaymentAccount paymentAccount) {
        boolean isPaymentAccountUsed = openOfferManager.getObservableList().stream()
                .filter(o -> o.getOffer().getMakerPaymentAccountId().equals(paymentAccount.getId()))
                .findAny()
                .isPresent();
        isPaymentAccountUsed = isPaymentAccountUsed || tradeManager.getObservableList().stream()
                .filter(t -> t.getOffer().getMakerPaymentAccountId().equals(paymentAccount.getId()) ||
                        paymentAccount.getId().equals(t.getTaker().getPaymentAccountId()))
                .findAny()
                .isPresent();
        if (!isPaymentAccountUsed)
            user.removePaymentAccount(paymentAccount);
        return isPaymentAccountUsed;
    }

    public void onSelectAccount(PaymentAccount paymentAccount) {
        user.setCurrentPaymentAccount(paymentAccount);
    }

    public void exportAccounts(Stage stage) {
        if (user.getPaymentAccounts() != null) {
            ArrayList<PaymentAccount> accounts = new ArrayList<>(user.getPaymentAccounts().stream()
                    .filter(paymentAccount -> paymentAccount instanceof AssetAccount)
                    .collect(Collectors.toList()));
            GUIUtil.exportAccounts(accounts, accountsFileName, preferences, stage, persistenceProtoResolver, corruptedStorageFileHandler);
        }
    }

    public void importAccounts(Stage stage) {
        GUIUtil.importAccounts(user, accountsFileName, preferences, stage, persistenceProtoResolver, corruptedStorageFileHandler);
    }

    public int getNumPaymentAccounts() {
        return user.getPaymentAccounts() != null ? user.getPaymentAccounts().size() : 0;
    }
}

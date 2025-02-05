/*
 * MCSR Ranked Launcher - https://github.com/RedLime/MCSR-Ranked-Launcher
 * Copyright (C) 2023 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.tabs.accounts;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.gui.dialogs.ChangeSkinDialog;
import com.atlauncher.managers.AccountManager;

/**
 * 12 / 06 / 2022
 */
public class AccountsViewModel implements IAccountsViewModel {
    @Override
    public int accountCount() {
        return AccountManager.getAccounts().size();
    }

    private List<AbstractAccount> accounts() {
        return AccountManager.getAccounts();
    }

    private Consumer<List<String>> _onAccountsChanged;

    @Override
    public void onAccountsNamesChanged(Consumer<List<String>> onAccountsChanged) {
        _onAccountsChanged = onAccountsChanged;
        pushNewAccounts();
    }

    /**
     * Update the UI with new accounts
     */
    @Override
    public void pushNewAccounts() {
        _onAccountsChanged.accept(
                accounts().stream()
                        .map(account -> account.minecraftUsername)
                        .collect(Collectors.toList()));
    }

    private Consumer<AbstractAccount> selected;
    private int selectedAccountIndex = -1;

    @Override
    public void onAccountSelected(Consumer<AbstractAccount> onAccountSelected) {
        selected = onAccountSelected;
    }

    @Override
    public void setSelectedAccount(int index) {
        selectedAccountIndex = index - 1;
        if (index == 0)
            selected.accept(null);
        else
            selected.accept(getSelectedAccount());
    }

    @NotNull
    @Override
    public AbstractAccount getSelectedAccount() {
        return accounts().get(selectedAccountIndex);
    }

    @Nullable
    @Override
    public MicrosoftAccount getSelectedAccountAs() {
        AbstractAccount account = getSelectedAccount();

        if (account instanceof MicrosoftAccount)
            return (MicrosoftAccount) account;
        return null;
    }

    @Override
    public int getSelectedIndex() {
        return selectedAccountIndex + 1;
    }

    @Override
    public boolean refreshAccessToken() {AbstractAccount abstractAccount = getSelectedAccount();
        if (abstractAccount instanceof MicrosoftAccount) {
            MicrosoftAccount account = (MicrosoftAccount) abstractAccount;
            boolean success = account
                    .refreshAccessToken(true);

            if (!success) {
                account.mustLogin = true;
            }

            AccountManager.saveAccounts();

            return success;
        }
        return false;
    }

    @Override
    public void updateUsername() {
        AbstractAccount account = getSelectedAccount();
        account.updateUsername();
        AccountManager.saveAccounts();
        pushNewAccounts();
    }

    @Override
    public void changeSkin() {
        AbstractAccount account = getSelectedAccount();

        new ChangeSkinDialog(account);
    }

    @Override
    public void updateSkin() {
        AbstractAccount account = getSelectedAccount();
        account.updateSkin();
    }

    @Override
    public void deleteAccount() {
        AccountManager.removeAccount(getSelectedAccount());
        pushNewAccounts();
    }
}

/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.CiphersuiteDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.HostnameExtensionDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ProtocolVersionDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.StarttlsDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import java.util.LinkedList;
import java.util.List;

public class PaddingOracleCommandConfig extends AttackConfig {

    public static final String ATTACK_COMMAND = "padding_oracle";

    @Parameter(names = "-block_size", description = "Block size of the to be used block cipher")
    private Integer blockSize = 16;

    @ParametersDelegate
    private ClientDelegate clientDelegate;
    @ParametersDelegate
    private HostnameExtensionDelegate hostnameExtensionDelegate;
    @ParametersDelegate
    private CiphersuiteDelegate ciphersuiteDelegate;
    @ParametersDelegate
    private ProtocolVersionDelegate protocolVersionDelegate;
    @ParametersDelegate
    private StarttlsDelegate starttlsDelegate;

    public PaddingOracleCommandConfig(GeneralDelegate delegate) {
        super(delegate);
        clientDelegate = new ClientDelegate();
        hostnameExtensionDelegate = new HostnameExtensionDelegate();
        ciphersuiteDelegate = new CiphersuiteDelegate();
        protocolVersionDelegate = new ProtocolVersionDelegate();
        starttlsDelegate = new StarttlsDelegate();
        addDelegate(clientDelegate);
        addDelegate(hostnameExtensionDelegate);
        addDelegate(ciphersuiteDelegate);
        addDelegate(protocolVersionDelegate);
        addDelegate(starttlsDelegate);
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public boolean isExecuteAttack() {
        return false;
    }

    @Override
    public Config createConfig() {
        Config config = super.createConfig();
        if (ciphersuiteDelegate.getCipherSuites() == null) {
            List<CipherSuite> cipherSuites = new LinkedList<>();
            cipherSuites.add(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
            cipherSuites.add(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA);
            cipherSuites.add(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256);
            cipherSuites.add(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256);
            config.setDefaultClientSupportedCiphersuites(cipherSuites);
        }
        for (CipherSuite suite : config.getDefaultClientSupportedCiphersuites()) {
            if (!suite.isCBC()) {
                throw new ConfigurationException("This attack only works with CBC Ciphersuites");
            }
        }
        // config.setQuickReceive(true);
        // config.setEarlyStop(true);
        return config;
    }
}

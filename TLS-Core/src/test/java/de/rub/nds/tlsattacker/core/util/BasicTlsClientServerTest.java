/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.util;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.util.FixedTimeProvider;
import de.rub.nds.tlsattacker.util.TimeHelper;
import de.rub.nds.tlsattacker.util.tests.IntegrationTests;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.operator.OperatorCreationException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * 
 * @author Lucas Hartmann <lucas.hartmann@rub.de>
 */
public class BasicTlsClientServerTest {

    private static final Logger LOGGER = LogManager.getLogger(BasicTlsClientServerTest.class);
    private static final int SERVER_PORT = 44335;

    public BasicTlsClientServerTest() {
    }

    /** Run a TLS handshake between BasicTlsClient and BasicTlsServer. */
    @Test
    @Category(IntegrationTests.class)
    public void testSimpleProxy() throws OperatorCreationException {

        try {
            TimeHelper.setProvider(new FixedTimeProvider(0));
            KeyPair k = KeyStoreGenerator.createRSAKeyPair(1024);
            KeyStore ks = null;
            ks = KeyStoreGenerator.createKeyStore(k);
            BasicTlsServer tlsServer = new BasicTlsServer(ks, KeyStoreGenerator.PASSWORD, "TLS", SERVER_PORT);

            LOGGER.info("Starting test server");
            new Thread(tlsServer).start();
            while (!tlsServer.isInitialized())
                ;

            LOGGER.info("Starting test client");
            BasicTlsClient client = new BasicTlsClient("localhost", SERVER_PORT, ProtocolVersion.TLS12,
                    CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
            client.setRetryConnect(false);
            Thread clientThread = new Thread(client);
            clientThread.start();

            TimeUnit.SECONDS.sleep(1);

            LOGGER.info("Killing client");
            clientThread.interrupt();
            LOGGER.info("Done.");

            LOGGER.info("Killing server...");
            tlsServer.shutdown();
            LOGGER.info("Done.");
        } catch (NoSuchAlgorithmException | CertificateException | IOException | InvalidKeyException
                | KeyStoreException | NoSuchProviderException | SignatureException | UnrecoverableKeyException
                | KeyManagementException | InterruptedException ex) {
            ex.printStackTrace();
            fail();
        }
    }
}

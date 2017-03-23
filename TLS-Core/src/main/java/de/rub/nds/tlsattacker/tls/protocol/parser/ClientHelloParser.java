/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol.parser;

import de.rub.nds.tlsattacker.tls.constants.HandshakeByteLength;
import de.rub.nds.tlsattacker.tls.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.tls.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.tls.protocol.message.ClientHelloMessage;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class ClientHelloParser extends HelloParser<ClientHelloMessage> {

    private static final Logger LOGGER = LogManager.getLogger("PARSER");

    public ClientHelloParser(int pointer, byte[] array, ProtocolVersion version) {
        super(pointer, array, HandshakeMessageType.CLIENT_HELLO, version);
    }

    @Override
    protected void parseHandshakeMessageContent(ClientHelloMessage msg) {
        parseProtocolVersion(msg);
        parseUnixtime(msg);
        parseRandom(msg);
        parseSessionIDLength(msg);
        parseSessionID(msg);
        parseCipherSuiteLength(msg);
        parseCipherSuites(msg);
        parseCompressionLength(msg);
        parseCompressions(msg);
        if (hasExtensionLengthField(msg)) {
            parseExtensionLength(msg);
            if (hasExtensions(msg)) {
                parseExtensionBytes(msg);
            }
        }
    }

    @Override
    protected ClientHelloMessage createHandshakeMessage() {
        return new ClientHelloMessage();
    }

    private void parseCipherSuiteLength(ClientHelloMessage msg) {
        msg.setCipherSuiteLength(parseIntField(HandshakeByteLength.CIPHER_SUITES_LENGTH));
        LOGGER.debug("CiepherSuiteLength: " + msg.getCipherSuiteLength().getValue());
    }

    private void parseCipherSuites(ClientHelloMessage msg) {
        msg.setCipherSuites(parseByteArrayField(msg.getCipherSuiteLength().getValue()));
        LOGGER.debug("CipherSuites: " + Arrays.toString(msg.getCipherSuites().getValue()));
    }

    private void parseCompressionLength(ClientHelloMessage message) {
        message.setCompressionLength(parseIntField(HandshakeByteLength.COMPRESSION_LENGTH));
        LOGGER.debug("CompressionLength: " + message.getCompressionLength().getValue());
    }

    private void parseCompressions(ClientHelloMessage message) {
        message.setCompressions(parseByteArrayField(message.getCompressionLength().getValue()));
        LOGGER.debug("Compressions: " + Arrays.toString(message.getCompressions().getValue()));
    }
}
